package com.bbinxx.onecharge.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbinxx.onecharge.domain.model.AppUsageInfo
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import com.bbinxx.onecharge.domain.usecase.GetAppUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HeatmapData(
    val dayOfWeek: String, // e.g., "Mon"
    val intensity: Float // 0f to 1f
)

data class AnalyticsState(
    val averageSotSecs: Long = 0L,
    val bestSotSecs: Long = 0L,
    val worstSotSecs: Long = 0L,
    val averageEfficiencyMinPerPercent: Double = 0.0,
    val averageBatteryUsed: Double = 0.0,
    val longestCycleMs: Long = 0L,
    val shortestCycleMs: Long = 0L,
    val heatmapData: List<HeatmapData> = emptyList(),
    val generatedInsights: List<String> = emptyList(),
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
                val apps = if (hasPerm && active != null) {
                    getAppUsageUseCase(active.startTime, System.currentTimeMillis()).take(5)
                } else {
                    emptyList()
                }
                _state.value = AnalyticsState(
                    hasUsagePermission = hasPerm,
                    topApps = apps,
                    generatedInsights = listOf("Not enough data. Complete a battery cycle to generate insights.")
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

            // Heatmap calculation
            val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            val daySotMap = mutableMapOf<Int, MutableList<Long>>() // Day of week (1-7) to SOTs

            completed.forEach { cycle ->
                val calendar = Calendar.getInstance().apply { timeInMillis = cycle.startTime }
                val day = calendar.get(Calendar.DAY_OF_WEEK)
                daySotMap.getOrPut(day) { mutableListOf() }.add(cycle.screenOnTimeSeconds)
            }

            val heatmapData = daysOfWeek.mapIndexed { index, dayName ->
                val day = index + 1
                val sots = daySotMap[day] ?: emptyList()
                val avgSotForDay = if (sots.isNotEmpty()) sots.average().toLong() else 0L
                // Normalize intensity based on best SOT overall
                val intensity = if (bestSot > 0) (avgSotForDay.toFloat() / bestSot.toFloat()).coerceIn(0f, 1f) else 0f
                HeatmapData(dayName, intensity)
            }

            // Generate Insights
            val insights = mutableListOf<String>()

            // Insight 1: Efficiency trend
            val recentEfficiency = completed.take(3).map { if (it.batteryUsed > 0) (it.screenOnTimeSeconds / 60.0) / it.batteryUsed else 0.0 }.average()
            if (recentEfficiency > avgEff * 1.1) {
                insights.add("Your battery efficiency has improved by 10% in the last 3 cycles.")
            } else if (recentEfficiency < avgEff * 0.9) {
                insights.add("Battery drain is higher than usual recently. Check for background apps.")
            } else {
                insights.add("Your battery efficiency is stable.")
            }

            // Insight 2: Best Day
            val bestDay = heatmapData.maxByOrNull { it.intensity }
            if (bestDay != null && bestDay.intensity > 0) {
                insights.add("You typically get the longest Screen-On Time on ${bestDay.dayOfWeek}s.")
            }

            // Insight 3: App Usage
            val recentCycle = completed.first()
            val apps = if (hasPerm) {
                getAppUsageUseCase(recentCycle.startTime, recentCycle.endTime ?: System.currentTimeMillis()).take(5)
            } else {
                emptyList()
            }

            if (apps.isNotEmpty()) {
                val topApp = apps.first()
                insights.add("In your last cycle, ${topApp.appName} accounted for a significant portion of drain.")
            } else if (!hasPerm) {
                insights.add("Enable Usage Access to see which apps are draining your battery.")
            }

            _state.value = AnalyticsState(
                averageSotSecs = avgSot,
                bestSotSecs = bestSot,
                worstSotSecs = worstSot,
                averageEfficiencyMinPerPercent = if (avgEff.isNaN()) 0.0 else avgEff,
                averageBatteryUsed = if (avgBattery.isNaN()) 0.0 else avgBattery,
                longestCycleMs = longestCycle,
                shortestCycleMs = shortestCycle,
                heatmapData = heatmapData,
                generatedInsights = insights,
                topApps = apps,
                hasUsagePermission = hasPerm
            )
        }
    }
}
