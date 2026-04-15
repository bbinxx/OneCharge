package com.bbinxx.onecharge.domain.model

enum class ThemeMode {
    SYSTEM, LIGHT, DARK, AMOLED
}

data class UserSettings(
    val fullChargeLevel: Int = 100,
    val endBatteryLevel: Int = 1,
    val autoCycleDetection: Boolean = true,
    val manualStartAllowed: Boolean = true,
    val manualRestartAllowed: Boolean = true,
    val trackAppUsage: Boolean = true,
    val persistentNotification: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColors: Boolean = true
)
