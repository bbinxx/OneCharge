package com.bbinxx.onecharge.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bbinxx.onecharge.domain.model.ThemeMode
import com.bbinxx.onecharge.domain.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_FULL_CHARGE = intPreferencesKey("full_charge_level")
        val KEY_END_BATTERY = intPreferencesKey("end_battery_level")
        val KEY_AUTO_DETECT = booleanPreferencesKey("auto_cycle_detection")
        val KEY_MANUAL_START = booleanPreferencesKey("manual_start_allowed")
        val KEY_MANUAL_RESTART = booleanPreferencesKey("manual_restart_allowed")
        val KEY_TRACK_APP_USAGE = booleanPreferencesKey("track_app_usage")
        val KEY_PERSISTENT_NOTIF = booleanPreferencesKey("persistent_notification")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val KEY_SCREEN_ON_TIMESTAMP = androidx.datastore.preferences.core.longPreferencesKey("screen_on_timestamp")
    }

    val userSettingsFlow: Flow<UserSettings> = dataStore.data.map { preferences ->
        UserSettings(
            fullChargeLevel = preferences[KEY_FULL_CHARGE] ?: 100,
            endBatteryLevel = preferences[KEY_END_BATTERY] ?: 1,
            autoCycleDetection = preferences[KEY_AUTO_DETECT] ?: true,
            manualStartAllowed = preferences[KEY_MANUAL_START] ?: true,
            manualRestartAllowed = preferences[KEY_MANUAL_RESTART] ?: true,
            trackAppUsage = preferences[KEY_TRACK_APP_USAGE] ?: true,
            persistentNotification = preferences[KEY_PERSISTENT_NOTIF] ?: false,
            themeMode = ThemeMode.valueOf(preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name),
            dynamicColors = preferences[KEY_DYNAMIC_COLORS] ?: true
        )
    }

    suspend fun updateSettings(settings: UserSettings) {
        dataStore.edit { preferences ->
            preferences[KEY_FULL_CHARGE] = settings.fullChargeLevel
            preferences[KEY_END_BATTERY] = settings.endBatteryLevel
            preferences[KEY_AUTO_DETECT] = settings.autoCycleDetection
            preferences[KEY_MANUAL_START] = settings.manualStartAllowed
            preferences[KEY_MANUAL_RESTART] = settings.manualRestartAllowed
            preferences[KEY_TRACK_APP_USAGE] = settings.trackAppUsage
            preferences[KEY_PERSISTENT_NOTIF] = settings.persistentNotification
            preferences[KEY_THEME_MODE] = settings.themeMode.name
            preferences[KEY_DYNAMIC_COLORS] = settings.dynamicColors
        }
    }

    val screenOnTimestampFlow: Flow<Long> = dataStore.data.map { preferences ->
        preferences[KEY_SCREEN_ON_TIMESTAMP] ?: 0L
    }

    suspend fun setScreenOnTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_SCREEN_ON_TIMESTAMP] = timestamp
        }
    }
}
