package com.bbinxx.onecharge.domain.model

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageDurationMs: Long
) {
    val usageDurationMinutes: Long
        get() = usageDurationMs / (1000 * 60)
}
