package com.bbinxx.onecharge.domain.model

data class ChargingSession(
    val id: Int = 0,
    val cycleId: Int,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val startBattery: Int,
    val endBattery: Int? = null
) {
    val durationMs: Long
        get() {
            val end = endTime ?: System.currentTimeMillis()
            return end - startTime
        }

    val batteryAdded: Int
        get() {
            val end = endBattery ?: startBattery
            return end - startBattery
        }
}
