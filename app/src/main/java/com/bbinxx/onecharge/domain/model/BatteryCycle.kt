package com.bbinxx.onecharge.domain.model

data class BatteryCycle(
    val id: Int = 0,
    val startBatteryLevel: Int,
    val currentBatteryLevel: Int,
    val endBatteryLevel: Int? = null,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val screenOnTimeSeconds: Long = 0L,
    val chargingDurationSeconds: Long = 0L,
    val chargingSessionsCount: Int = 0,
    val isManual: Boolean = false,
    val isActive: Boolean = true
) {
    val batteryUsed: Int
        get() = startBatteryLevel - currentBatteryLevel

    val estimatedFullSotSeconds: Long
        get() {
            val drop = batteryUsed
            return if (drop > 0) {
                (screenOnTimeSeconds * 100) / drop
            } else {
                0L
            }
        }

    val durationMs: Long
        get() {
            val end = endTime ?: System.currentTimeMillis()
            return end - startTime
        }

    val avgMinutesPerBatteryPercent: Double
        get() {
            val drop = batteryUsed
            return if (drop > 0) {
                (screenOnTimeSeconds / 60.0) / drop
            } else {
                0.0
            }
        }

    val batteryDrainPerHour: Double
        get() {
            val hrs = durationMs / (1000.0 * 60.0 * 60.0)
            val drop = batteryUsed
            return if (hrs > 0.0) {
                drop / hrs
            } else {
                0.0
            }
        }
}

/** Project the live interval without changing the persisted cycle. */
fun BatteryCycle.withLiveScreenTime(screenOnSince: Long, now: Long): BatteryCycle {
    if (!isActive || screenOnSince <= 0L) return this
    val since = maxOf(startTime, screenOnSince)
    val elapsed = (now - since).coerceAtLeast(0L) / 1000L
    return copy(screenOnTimeSeconds = screenOnTimeSeconds + elapsed)
}
