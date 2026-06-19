package com.bbinxx.onecharge.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbinxx.onecharge.data.service.BatteryTrackerService
import com.bbinxx.onecharge.domain.model.ThemeMode
import com.bbinxx.onecharge.domain.model.UserSettings
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import com.bbinxx.onecharge.domain.usecase.GetSettingsUseCase
import com.bbinxx.onecharge.domain.usecase.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val cycleRepository: BatteryCycleRepository
) : ViewModel() {

    val settings: StateFlow<UserSettings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    fun updateFullChargeLevel(level: Int) {
        updateSettings { it.copy(fullChargeLevel = level) }
    }

    fun updateEndBatteryLevel(level: Int) {
        updateSettings { it.copy(endBatteryLevel = level) }
    }

    fun updateAutoCycleDetection(enabled: Boolean) {
        updateSettings { it.copy(autoCycleDetection = enabled) }
    }

    fun updatePersistentNotification(enabled: Boolean) {
        updateSettings { it.copy(persistentNotification = enabled) }
        
        // Start or stop service based on preference
        val intent = Intent(context, BatteryTrackerService::class.java)
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            context.stopService(intent)
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        updateSettings { it.copy(themeMode = mode) }
    }

    fun updateDynamicColors(enabled: Boolean) {
        updateSettings { it.copy(dynamicColors = enabled) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            // Stop service if running
            context.stopService(Intent(context, BatteryTrackerService::class.java))
            
            // Delete database records
            cycleRepository.clearAllHistory()
            
            // Reset settings
            saveSettingsUseCase(UserSettings())
        }
    }

    private fun updateSettings(transform: (UserSettings) -> UserSettings) {
        viewModelScope.launch {
            val current = getSettingsUseCase.getDirect()
            saveSettingsUseCase(transform(current))
        }
    }
}
