package com.bbinxx.onecharge.domain.repository

import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.model.ChargingSession
import kotlinx.coroutines.flow.Flow

interface BatteryCycleRepository {
    fun getActiveCycleFlow(): Flow<BatteryCycle?>
    suspend fun getActiveCycle(): BatteryCycle?
    suspend fun insertCycle(cycle: BatteryCycle): Long
    suspend fun updateCycle(cycle: BatteryCycle)
    suspend fun updateScreenOnTime(cycleId: Int, closedSeconds: Long)
    fun getCompletedCyclesFlow(): Flow<List<BatteryCycle>>
    suspend fun getCompletedCycles(): List<BatteryCycle>
    suspend fun deleteCycle(cycle: BatteryCycle)
    suspend fun clearAllHistory()
    
    // Charging Sessions
    suspend fun insertChargingSession(session: ChargingSession): Long
    suspend fun updateChargingSession(session: ChargingSession)
    suspend fun getActiveChargingSession(cycleId: Int): ChargingSession?
    suspend fun getChargingSessions(cycleId: Int): List<ChargingSession>
    fun getChargingSessionsFlow(cycleId: Int): Flow<List<ChargingSession>>
}
