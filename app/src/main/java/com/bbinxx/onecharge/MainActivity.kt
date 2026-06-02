package com.bbinxx.onecharge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bbinxx.onecharge.domain.model.UserSettings
import com.bbinxx.onecharge.domain.repository.UserSettingsRepository
import com.bbinxx.onecharge.ui.navigation.AppNavigation
import com.bbinxx.onecharge.ui.theme.OneChargeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: UserSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsRepository.getUserSettingsFlow()
                .collectAsState(initial = UserSettings())

            OneChargeTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColors
            ) {
                AppNavigation()
            }
        }
    }
}