package com.bbinxx.onecharge.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.bbinxx.onecharge.domain.model.ThemeMode

private val PremiumDarkColorScheme = darkColorScheme(
    primary = IosPrimary,
    onPrimary = IosTextPrimary,
    background = IosBg,
    onBackground = IosTextPrimary,
    surface = IosSurface,
    onSurface = IosTextPrimary,
    surfaceVariant = IosSurfaceElevated,
    onSurfaceVariant = IosTextSecondary,
    outline = IosBorder,
    error = IosRed
)

@Composable
fun OneChargeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM, // Ignored in premium theme, forces dark mode
    dynamicColor: Boolean = true, // Ignored in premium theme
    content: @Composable () -> Unit
) {
    val colorScheme = PremiumDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = IosBg.toArgb()
            window.navigationBarColor = IosBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
