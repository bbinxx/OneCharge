package com.bbinxx.onecharge.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryCycleDao {
    @Query("SELECT * FROM battery_cycles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveCycle(): BatteryCycleEntity?

    @Query("SELECT * FROM battery_cycles WHERE isActive = 1 LIMIT 1")
    fun getActiveCycleFlow(): Flow<BatteryCycleEntity?>

    @Query("SELECT * FROM battery_cycles WHERE isActive = 0 ORDER BY startTime DESC")
    suspend fun getCompletedCycles(): List<BatteryCycleEntity>

    @Query("SELECT * FROM battery_cycles WHERE isActive = 0 ORDER BY startTime DESC")
    fun getCompletedCyclesFlow(): Flow<List<BatteryCycleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cycle: BatteryCycleEntity): Long

    @Update
    suspend fun update(cycle: BatteryCycleEntity)

    @Delete
    suspend fun delete(cycle: BatteryCycleEntity)

    @Query("DELETE FROM battery_cycles")
    suspend fun clearAll()
}

@Dao
interface ChargingSessionDao {
    @Query("SELECT * FROM charging_sessions WHERE cycleId = :cycleId AND endTime IS NULL LIMIT 1")
    suspend fun getActiveSession(cycleId: Int): ChargingSessionEntity?

    @Query("SELECT * FROM charging_sessions WHERE cycleId = :cycleId ORDER BY startTime ASC")
    suspend fun getSessions(cycleId: Int): List<ChargingSessionEntity>

    @Query("SELECT * FROM charging_sessions WHERE cycleId = :cycleId ORDER BY startTime ASC")
    fun getSessionsFlow(cycleId: Int): Flow<List<ChargingSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ChargingSessionEntity): Long

    @Update
    suspend fun update(session: ChargingSessionEntity)

    @Query("DELETE FROM charging_sessions")
    suspend fun clearAll()
}
