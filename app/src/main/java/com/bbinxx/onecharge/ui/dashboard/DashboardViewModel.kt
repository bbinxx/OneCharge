package com.bbinxx.onecharge.ui.dashboard

import android.content.Context
import android.os.BatteryManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbinxx.onecharge.data.tracker.BatteryCycleTracker
import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.model.withLiveScreenTime
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import com.bbinxx.onecharge.domain.usecase.GetActiveCycleUseCase
import com.bbinxx.onecharge.domain.usecase.StartNewCycleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bbinxx.onecharge.ui.widget.refreshOneChargeWidgets
import kotlinx.coroutines.CancellationException
import android.util.Log

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getActiveCycleUseCase: GetActiveCycleUseCase,
    private val startNewCycleUseCase: StartNewCycleUseCase,
    private val cycleRepository: BatteryCycleRepository,
    private val tracker: BatteryCycleTracker
) : ViewModel() {

    private val ticker = kotlinx.coroutines.flow.flow {
        while (true) {
            emit(System.currentTimeMillis())
            kotlinx.coroutines.delay(1000)
        }
    }

    val activeCycle: StateFlow<BatteryCycle?> = kotlinx.coroutines.flow.combine(
        getActiveCycleUseCase(),
        tracker.screenOnTimeFlow,
        ticker
    ) { cycle, screenOnTime, now ->
        cycle?.withLiveScreenTime(screenOnTime, now)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            tracker.syncWithUsageStats()
        }
    }

    fun startManualCycle() {
        viewModelScope.launch {
            val currentLevel = getBatteryLevel()
            startNewCycleUseCase(currentLevel, isManual = true)
            refreshWidgets()
        }
    }

    fun restartCycle(saveHistory: Boolean) {
        viewModelScope.launch {
            val active = activeCycle.value
            val currentLevel = getBatteryLevel()
            if (active != null) {
                if (saveHistory) {
                    cycleRepository.updateCycle(
                        active.copy(
                            endTime = System.currentTimeMillis(),
                            endBatteryLevel = currentLevel,
                            isActive = false
                        )
                    )
                    val activeSession = cycleRepository.getActiveChargingSession(active.id)
                    if (activeSession != null) {
                        cycleRepository.updateChargingSession(
                            activeSession.copy(
                                endTime = System.currentTimeMillis(),
                                endBattery = currentLevel
                            )
                        )
                    }
                } else {
                    cycleRepository.deleteCycle(active)
                }
            }
            startNewCycleUseCase(currentLevel, isManual = true)
            refreshWidgets()
        }
    }

    private suspend fun refreshWidgets() {
        try {
            refreshOneChargeWidgets(context)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.e("OneChargeWidget", "Unable to refresh cycle", error)
        }
    }

    private fun getBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}
