package com.bbinxx.onecharge.data.tracker

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.BatteryManager
import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.model.ChargingSession
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import com.bbinxx.onecharge.domain.repository.UserSettingsRepository
import com.bbinxx.onecharge.domain.repository.AppUsageRepository
import com.bbinxx.onecharge.data.local.PreferencesManager
import androidx.glance.appwidget.updateAll
import com.bbinxx.onecharge.ui.widget.OneChargeWidget
import com.bbinxx.onecharge.domain.usecase.StartNewCycleUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryCycleTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cycleRepository: BatteryCycleRepository,
    private val settingsRepository: UserSettingsRepository,
    private val appUsageRepository: AppUsageRepository,
    private val preferencesManager: PreferencesManager,
    private val startNewCycleUseCase: StartNewCycleUseCase
) {
    private val _screenOnTimeFlow = MutableStateFlow(0L)
    val screenOnTimeFlow: StateFlow<Long> = _screenOnTimeFlow.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            if (powerManager.isInteractive) {
                val currentTimestamp = preferencesManager.screenOnTimestampFlow.first()
                if (currentTimestamp == 0L) {
                    val now = System.currentTimeMillis()
                    preferencesManager.setScreenOnTimestamp(now)
                    _screenOnTimeFlow.value = now
                } else {
                    _screenOnTimeFlow.value = currentTimestamp
                }
            } else {
                preferencesManager.setScreenOnTimestamp(0L)
            }
        }
    }

    fun onScreenOn() {
        val now = System.currentTimeMillis()
        _screenOnTimeFlow.value = now
        scope.launch {
            preferencesManager.setScreenOnTimestamp(now)
            updateWidgets()
        }
    }

    fun onScreenOff() {
        _screenOnTimeFlow.value = 0L
        scope.launch {
            val timestamp = preferencesManager.screenOnTimestampFlow.first()
            if (timestamp > 0L) {
                val elapsedSecs = (System.currentTimeMillis() - timestamp) / 1000
                preferencesManager.setScreenOnTimestamp(0L)
                updateActiveCycleSot(elapsedSecs)
                updateWidgets()
            }
        }
    }

    private suspend fun updateActiveCycleSot(additionalSotSeconds: Long) {
        val activeCycle = cycleRepository.getActiveCycle() ?: return
        val updated = activeCycle.copy(
            screenOnTimeSeconds = activeCycle.screenOnTimeSeconds + additionalSotSeconds
        )
        cycleRepository.updateCycle(updated)
        // Removed heavy syncWithUsageStats() call to prevent battery drain on every screen-off.
        // Incremental tracking math is perfectly sufficient here.
    }

    suspend fun syncWithUsageStats() = withContext(Dispatchers.IO) {
        val activeCycle = cycleRepository.getActiveCycle() ?: return@withContext
        if (!appUsageRepository.hasUsagePermission()) return@withContext

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(activeCycle.startTime, now)

        var totalSotMs = 0L
        var lastInteractiveTime = 0L
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.SCREEN_INTERACTIVE) {
                lastInteractiveTime = event.timeStamp
            } else if (event.eventType == UsageEvents.Event.SCREEN_NON_INTERACTIVE) {
                if (lastInteractiveTime > 0L) {
                    totalSotMs += event.timeStamp - lastInteractiveTime
                    lastInteractiveTime = 0L
                }
            }
        }

        if (lastInteractiveTime > 0L) {
            totalSotMs += now - lastInteractiveTime
        }

        val calculatedSotSecs = totalSotMs / 1000
        if (calculatedSotSecs > activeCycle.screenOnTimeSeconds) {
            cycleRepository.updateCycle(
                activeCycle.copy(screenOnTimeSeconds = calculatedSotSecs)
            )
        }
    }

    suspend fun onBatteryLevelChanged(currentLevel: Int) {
        val settings = settingsRepository.getUserSettings()
        val activeCycle = cycleRepository.getActiveCycle()

        if (activeCycle != null) {
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

    suspend fun onPowerConnected() {
        val activeCycle = cycleRepository.getActiveCycle() ?: return
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

    suspend fun onPowerDisconnected() {
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
            OneChargeWidget().updateAll(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
