package com.bbinxx.onecharge.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbinxx.onecharge.domain.model.AppUsageInfo
import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import com.bbinxx.onecharge.domain.usecase.GetAppUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AnalyticsState(
    val averageSotSecs: Long = 0L,
    val bestSotSecs: Long = 0L,
    val worstSotSecs: Long = 0L,
    val averageEfficiencyMinPerPercent: Double = 0.0,
    val averageBatteryUsed: Double = 0.0,
    val longestCycleMs: Long = 0L,
    val shortestCycleMs: Long = 0L,
    val trendSotHours: List<Float> = emptyList(),
    val trendLabels: List<String> = emptyList(),
    val weeklySotHours: List<Pair<String, Float>> = emptyList(),
    val topApps: List<AppUsageInfo> = emptyList(),
    val hasUsagePermission: Boolean = false
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val cycleRepository: BatteryCycleRepository,
    private val getAppUsageUseCase: GetAppUsageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())
    val state = _state.asStateFlow()

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            val completed = cycleRepository.getCompletedCycles()
            val active = cycleRepository.getActiveCycle()
            
            val hasPerm = getAppUsageUseCase.hasPermission()
            
            if (completed.isEmpty()) {
                // If we have an active cycle, load app usage for it
                val apps = if (hasPerm && active != null) {
                    getAppUsageUseCase(active.startTime, System.currentTimeMillis()).take(5)
                } else {
                    emptyList()
                }
                _state.value = AnalyticsState(
                    hasUsagePermission = hasPerm,
                    topApps = apps
                )
                return@launch
            }

            // Averages and Extremes
            val avgSot = completed.map { it.screenOnTimeSeconds }.average().toLong()
            val bestSot = completed.maxOf { it.screenOnTimeSeconds }
            val worstSot = completed.minOf { it.screenOnTimeSeconds }
            
            val avgEff = completed.filter { it.batteryUsed > 0 }
                .map { (it.screenOnTimeSeconds / 60.0) / it.batteryUsed }
                .average()

            val avgBattery = completed.map { it.batteryUsed }.average()
            val longestCycle = completed.maxOf { it.durationMs }
            val shortestCycle = completed.minOf { it.durationMs }

            // Line Chart: SOT Trend (Last 7 completed cycles)
            val trendList = completed.take(7).reversed()
            val trendSot = trendList.map { it.screenOnTimeSeconds / 3600f }
            val trendLabels = trendList.mapIndexed { i, _ -> "C${i+1}" }

            // Weekly SOT: group last 7 cycles by day of week
            val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            val weeklyData = completed.take(7).map { cycle ->
                val calendar = Calendar.getInstance().apply { timeInMillis = cycle.startTime }
                val dayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
                daysOfWeek[dayIndex] to (cycle.screenOnTimeSeconds / 3600f)
            }

            // Load app usage statistics for the most recent completed cycle
            val recentCycle = completed.first()
            val apps = if (hasPerm) {
                getAppUsageUseCase(recentCycle.startTime, recentCycle.endTime ?: System.currentTimeMillis()).take(5)
            } else {
                emptyList()
            }

            _state.value = AnalyticsState(
                averageSotSecs = avgSot,
                bestSotSecs = bestSot,
                worstSotSecs = worstSot,
                averageEfficiencyMinPerPercent = if (avgEff.isNaN()) 0.0 else avgEff,
                averageBatteryUsed = if (avgBattery.isNaN()) 0.0 else avgBattery,
                longestCycleMs = longestCycle,
                shortestCycleMs = shortestCycle,
                trendSotHours = trendSot,
                trendLabels = trendLabels,
                weeklySotHours = weeklyData,
                topApps = apps,
                hasUsagePermission = hasPerm
            )
        }
    }
}
