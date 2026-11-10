package com.bbinxx.onecharge.data.tracker

import android.content.Context
import android.os.BatteryManager
import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.model.withLiveScreenTime
import com.bbinxx.onecharge.domain.model.ChargingSession
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import com.bbinxx.onecharge.domain.repository.UserSettingsRepository
import com.bbinxx.onecharge.data.local.PreferencesManager
import com.bbinxx.onecharge.ui.widget.refreshOneChargeWidgets
import com.bbinxx.onecharge.domain.usecase.StartNewCycleUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.CancellationException
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryCycleTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cycleRepository: BatteryCycleRepository,
    private val settingsRepository: UserSettingsRepository,
    private val screenTimeReader: ScreenTimeReader,
    private val preferencesManager: PreferencesManager,
    private val startNewCycleUseCase: StartNewCycleUseCase
) {
    private val _screenOnTimeFlow = MutableStateFlow(0L)
    val screenOnTimeFlow: StateFlow<Long> = _screenOnTimeFlow.asStateFlow()

    private val stateMutex = Mutex()
    private var initialized = false

    // All tracker entry points initialize explicitly while holding the same mutex.
    // Receivers own their short asynchronous work via goAsync instead of fire-and-forget jobs.
    private suspend fun initializeScreenState() {
        if (initialized) return
        val interactive = context.getSystemService(android.os.PowerManager::class.java).isInteractive
        val saved = preferencesManager.screenOnTimestampFlow.first()
        val timestamp = if (interactive) saved.takeIf { it > 0L } ?: System.currentTimeMillis() else 0L
        preferencesManager.setScreenOnTimestamp(timestamp)
        _screenOnTimeFlow.value = timestamp
        initialized = true
    }

    suspend fun onScreenOn() {
        val receivedAt = System.currentTimeMillis()
        stateMutex.withLock {
            initializeScreenState()
            if (_screenOnTimeFlow.value == 0L) {
                preferencesManager.setScreenOnTimestamp(receivedAt)
                _screenOnTimeFlow.value = receivedAt
            }
        }
        updateWidgets()
    }

    suspend fun onScreenOff() {
        val receivedAt = System.currentTimeMillis()
        stateMutex.withLock {
            // Read before initialization, which would clear a saved interval while non-interactive.
            val timestamp = if (initialized) _screenOnTimeFlow.value
                else preferencesManager.screenOnTimestampFlow.first()
            initialized = true
            if (timestamp > 0L) updateActiveCycleSot(timestamp, receivedAt)
            preferencesManager.setScreenOnTimestamp(0L)
            _screenOnTimeFlow.value = 0L
        }
        updateWidgets()
    }

    private suspend fun updateActiveCycleSot(screenOnSince: Long, screenOffAt: Long) {
        val activeCycle = cycleRepository.getActiveCycle() ?: return
        cycleRepository.updateCycle(activeCycle.withLiveScreenTime(screenOnSince, screenOffAt))
    }

    suspend fun syncWithUsageStats() = withContext(Dispatchers.IO) {
        stateMutex.withLock {
            initializeScreenState()
            val activeCycle = cycleRepository.getActiveCycle() ?: return@withLock
            val times = screenTimeReader.read(activeCycle.startTime, System.currentTimeMillis()) ?: return@withLock
            // The activity can reset a cycle while the platform query is in progress.
            if (cycleRepository.getActiveCycle()?.id != activeCycle.id) return@withLock
            cycleRepository.updateScreenOnTime(activeCycle.id, times.closedSeconds)
            // Restore an open interval missed while this process was absent. Persist closed
            // intervals only, so live projection and a later screen-off cannot add them twice.
            preferencesManager.setScreenOnTimestamp(times.openSince)
            _screenOnTimeFlow.value = times.openSince
        }
    }

    suspend fun onBatteryLevelChanged(currentLevel: Int) = stateMutex.withLock {
        initializeScreenState()
        val settings = settingsRepository.getUserSettings()
        val activeCycle = cycleRepository.getActiveCycle()

        if (activeCycle != null) {
            // ACTION_BATTERY_CHANGED also fires for temperature/voltage changes.
            if (currentLevel == activeCycle.currentBatteryLevel && currentLevel > settings.endBatteryLevel) return@withLock
            val updatedCycle = activeCycle.copy(currentBatteryLevel = currentLevel)
            cycleRepository.updateCycle(updatedCycle)

            if (currentLevel <= settings.endBatteryLevel) {
                cycleRepository.updateCycle(
                    updatedCycle.copy(
                        endTime = System.currentTimeMillis(),
                        endBatteryLevel = currentLevel,
                        isActive = false
                    )
                )
                val activeSession = cycleRepository.getActiveChargingSession(activeCycle.id)
                if (activeSession != null) {
                    cycleRepository.updateChargingSession(
                        activeSession.copy(
                            endTime = System.currentTimeMillis(),
                            endBattery = currentLevel
                        )
                    )
                }
            }
            updateWidgets()
        }
    }

    suspend fun onPowerConnected() = stateMutex.withLock {
        initializeScreenState()
        val activeCycle = cycleRepository.getActiveCycle() ?: return@withLock
        val currentLevel = getBatteryLevel()

        val session = ChargingSession(
            cycleId = activeCycle.id,
            startTime = System.currentTimeMillis(),
            startBattery = currentLevel
        )
        cycleRepository.insertChargingSession(session)

        cycleRepository.updateCycle(
            activeCycle.copy(
                chargingSessionsCount = activeCycle.chargingSessionsCount + 1,
                currentBatteryLevel = currentLevel
            )
        )
        updateWidgets()
    }

    suspend fun onPowerDisconnected() = stateMutex.withLock {
        initializeScreenState()
        val currentLevel = getBatteryLevel()
        val settings = settingsRepository.getUserSettings()

        val activeCycle = cycleRepository.getActiveCycle()

        if (activeCycle != null) {
            val activeSession = cycleRepository.getActiveChargingSession(activeCycle.id)
            if (activeSession != null) {
                val updatedSession = activeSession.copy(
                    endTime = System.currentTimeMillis(),
                    endBattery = currentLevel
                )
                cycleRepository.updateChargingSession(updatedSession)

                val chargeDurationSecs = updatedSession.durationMs / 1000
                cycleRepository.updateCycle(
                    activeCycle.copy(
                        chargingDurationSeconds = activeCycle.chargingDurationSeconds + chargeDurationSecs,
                        currentBatteryLevel = currentLevel
                    )
                )
            }

            if (settings.autoCycleDetection && currentLevel >= settings.fullChargeLevel) {
                startNewCycleUseCase(currentLevel, isManual = false)
            }
        } else {
            if (currentLevel >= settings.fullChargeLevel) {
                startNewCycleUseCase(currentLevel, isManual = false)
            }
        }
        updateWidgets()
    }

    private fun getBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private suspend fun updateWidgets() {
        try {
            refreshOneChargeWidgets(context)
        } catch (error: CancellationException) {
            throw error
        } catch (error: java.io.IOException) {
            Log.w("OneChargeWidget", "Unable to refresh widget state", error)
        }
    }
}
