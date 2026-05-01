package com.bbinxx.onecharge.data.repository

import com.bbinxx.onecharge.data.local.BatteryCycleDao
import com.bbinxx.onecharge.data.local.ChargingSessionDao
import com.bbinxx.onecharge.data.local.toDomain
import com.bbinxx.onecharge.data.local.toEntity
import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.model.ChargingSession
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryCycleRepositoryImpl @Inject constructor(
    private val batteryCycleDao: BatteryCycleDao,
    private val chargingSessionDao: ChargingSessionDao
) : BatteryCycleRepository {
    override fun getActiveCycleFlow(): Flow<BatteryCycle?> =
        batteryCycleDao.getActiveCycleFlow().map { it?.toDomain() }

    override suspend fun getActiveCycle(): BatteryCycle? =
        batteryCycleDao.getActiveCycle()?.toDomain()

    override suspend fun insertCycle(cycle: BatteryCycle): Long =
        batteryCycleDao.insert(cycle.toEntity())

    override suspend fun updateCycle(cycle: BatteryCycle) =
        batteryCycleDao.update(cycle.toEntity())

    override fun getCompletedCyclesFlow(): Flow<List<BatteryCycle>> =
        batteryCycleDao.getCompletedCyclesFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun getCompletedCycles(): List<BatteryCycle> =
        batteryCycleDao.getCompletedCycles().map { it.toDomain() }

    override suspend fun deleteCycle(cycle: BatteryCycle) =
        batteryCycleDao.delete(cycle.toEntity())

    override suspend fun clearAllHistory() {
        batteryCycleDao.clearAll()
        chargingSessionDao.clearAll()
    }

    override suspend fun insertChargingSession(session: ChargingSession): Long =
        chargingSessionDao.insert(session.toEntity())

    override suspend fun updateChargingSession(session: ChargingSession) =
        chargingSessionDao.update(session.toEntity())

    override suspend fun getActiveChargingSession(cycleId: Int): ChargingSession? =
        chargingSessionDao.getActiveSession(cycleId)?.toDomain()

    override suspend fun getChargingSessions(cycleId: Int): List<ChargingSession> =
        chargingSessionDao.getSessions(cycleId).map { it.toDomain() }

    override fun getChargingSessionsFlow(cycleId: Int): Flow<List<ChargingSession>> =
        chargingSessionDao.getSessionsFlow(cycleId).map { list -> list.map { it.toDomain() } }
}
