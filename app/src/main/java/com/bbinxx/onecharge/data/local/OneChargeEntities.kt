package com.bbinxx.onecharge.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.model.ChargingSession

@Entity(tableName = "battery_cycles")
data class BatteryCycleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startBatteryLevel: Int,
    val currentBatteryLevel: Int,
    val endBatteryLevel: Int?,
    val startTime: Long,
    val endTime: Long?,
    val screenOnTimeSeconds: Long,
    val chargingDurationSeconds: Long,
    val chargingSessionsCount: Int,
    val isManual: Boolean,
    val isActive: Boolean
)

@Entity(tableName = "charging_sessions")
data class ChargingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cycleId: Int,
    val startTime: Long,
    val endTime: Long?,
    val startBattery: Int,
    val endBattery: Int?
)

// Mappers
fun BatteryCycleEntity.toDomain() = BatteryCycle(
    id = id,
    startBatteryLevel = startBatteryLevel,
    currentBatteryLevel = currentBatteryLevel,
    endBatteryLevel = endBatteryLevel,
    startTime = startTime,
    endTime = endTime,
    screenOnTimeSeconds = screenOnTimeSeconds,
    chargingDurationSeconds = chargingDurationSeconds,
    chargingSessionsCount = chargingSessionsCount,
    isManual = isManual,
    isActive = isActive
)

fun BatteryCycle.toEntity() = BatteryCycleEntity(
    id = id,
    startBatteryLevel = startBatteryLevel,
    currentBatteryLevel = currentBatteryLevel,
    endBatteryLevel = endBatteryLevel,
    startTime = startTime,
    endTime = endTime,
    screenOnTimeSeconds = screenOnTimeSeconds,
    chargingDurationSeconds = chargingDurationSeconds,
    chargingSessionsCount = chargingSessionsCount,
    isManual = isManual,
    isActive = isActive
)

fun ChargingSessionEntity.toDomain() = ChargingSession(
    id = id,
    cycleId = cycleId,
    startTime = startTime,
    endTime = endTime,
    startBattery = startBattery,
    endBattery = endBattery
)

fun ChargingSession.toEntity() = ChargingSessionEntity(
    id = id,
    cycleId = cycleId,
    startTime = startTime,
    endTime = endTime,
    startBattery = startBattery,
    endBattery = endBattery
)
