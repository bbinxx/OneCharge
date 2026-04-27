package com.bbinxx.onecharge.domain.usecase

import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import javax.inject.Inject

class StartNewCycleUseCase @Inject constructor(
    private val repository: BatteryCycleRepository
) {
    suspend operator fun invoke(startBattery: Int, isManual: Boolean): BatteryCycle {
        // 1. Complete existing active cycle if any
        val activeCycle = repository.getActiveCycle()
        if (activeCycle != null) {
            val completedCycle = activeCycle.copy(
                endTime = System.currentTimeMillis(),
                endBatteryLevel = activeCycle.currentBatteryLevel,
                isActive = false
            )
            repository.updateCycle(completedCycle)
            
            // Also complete active charging session if any
            val activeSession = repository.getActiveChargingSession(activeCycle.id)
            if (activeSession != null) {
                repository.updateChargingSession(
                    activeSession.copy(
                        endTime = System.currentTimeMillis(),
                        endBattery = activeCycle.currentBatteryLevel
                    )
                )
            }
        }

        // 2. Create and insert new active cycle
        val newCycle = BatteryCycle(
            startBatteryLevel = startBattery,
            currentBatteryLevel = startBattery,
            startTime = System.currentTimeMillis(),
            isManual = isManual,
            isActive = true
        )
        val id = repository.insertCycle(newCycle)
        return newCycle.copy(id = id.toInt())
    }
}
