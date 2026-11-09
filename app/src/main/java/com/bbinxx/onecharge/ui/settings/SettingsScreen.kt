package com.bbinxx.onecharge.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bbinxx.onecharge.domain.model.ThemeMode
import com.bbinxx.onecharge.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        containerColor = IosBg,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1.2).sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IosBg.copy(alpha = 0.6f),
                    titleContentColor = IosTextPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // 1. Battery Cycle
            Column {
                SettingsSectionHeader("BATTERY CYCLE")
                SettingsListGroup {
                    SettingsDropdownRow(
                        icon = Icons.Rounded.BatteryChargingFull,
                        iconBgColor = IosPrimary,
                        title = "Full Charge Threshold",
                        options = listOf(80, 85, 90, 95, 100),
                        selectedValue = settings.fullChargeLevel,
                        onSelected = { viewModel.updateFullChargeLevel(it) },
                        format = { "$it%" },
                        isLast = false
                    )
                    SettingsDropdownRow(
                        icon = Icons.Rounded.BatteryAlert,
                        iconBgColor = IosOrange,
                        title = "End Battery Threshold",
                        options = listOf(0, 1, 2, 5, 10, 15, 20),
                        selectedValue = settings.endBatteryLevel,
                        onSelected = { viewModel.updateEndBatteryLevel(it) },
                        format = { "$it%" },
                        isLast = false
                    )
                    SettingsToggleRow(
                        icon = Icons.Rounded.Sync,
                        iconBgColor = IosGreen,
                        title = "Automatic Cycle Detection",
                        checked = settings.autoCycleDetection,
                        onCheckedChange = { viewModel.updateAutoCycleDetection(it) },
                        isLast = true
                    )
                }
            }

            // 2. Appearance
            Column {
                SettingsSectionHeader("APPEARANCE")
                SettingsListGroup {
                    SettingsDropdownRow(
                        icon = Icons.Rounded.Palette,
                        iconBgColor = IosBlueSecondary,
                        title = "Theme Mode",
                        options = ThemeMode.values().toList(),
                        selectedValue = settings.themeMode,
                        onSelected = { viewModel.updateThemeMode(it) },
                        format = { it.name.lowercase().replaceFirstChar { char -> char.uppercase() } },
                        isLast = false
                    )
                    SettingsToggleRow(
                        icon = Icons.Rounded.Palette,
                        iconBgColor = IosPrimary,
                        title = "Dynamic Colors",
                        checked = settings.dynamicColors,
                        onCheckedChange = { viewModel.updateDynamicColors(it) },
                        isLast = true
                    )
                }
                Text(
                    "Dynamic Colors overrides iOS styles with Material You generated colors.",
                    fontSize = 12.sp, color = IosTextSecondary, modifier = Modifier.padding(top = 8.dp, start = 16.dp)
                )
            }

            // 3. Notifications & Widgets
            Column {
                SettingsSectionHeader("NOTIFICATIONS & WIDGETS")
                SettingsListGroup {
                    SettingsToggleRow(
                        icon = Icons.Rounded.Notifications,
                        iconBgColor = IosRed,
                        title = "Persistent Notification",
                        checked = settings.persistentNotification,
                        onCheckedChange = { viewModel.updatePersistentNotification(it) },
                        isLast = false
                    )
                    SettingsActionRow(
                        icon = Icons.Rounded.Notifications,
                        iconBgColor = IosSurfaceElevated,
                        title = "System Notification Settings",
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        },
                        isLast = false
                    )
                    SettingsActionRow(
                        icon = Icons.Rounded.Widgets,
                        iconBgColor = IosOrange,
                        title = "Widget Configuration",
                        onClick = { /* No-op */ },
                        isLast = true
                    )
                }
            }

            // 4. Data & About
            Column {
                SettingsSectionHeader("DATA & ABOUT")
                SettingsListGroup {
                    SettingsActionRow(
                        icon = Icons.Rounded.Download,
                        iconBgColor = IosGreen,
                        title = "Export Data (CSV/JSON)",
                        onClick = { /* No-op */ },
                        isLast = false
                    )
                    SettingsActionRow(
                        icon = Icons.Rounded.Info,
                        iconBgColor = IosBlueSecondary,
                        title = "About OneCharge",
                        value = "v1.1.0",
                        onClick = { },
                        isLast = false
                    )
                    SettingsActionRow(
                        icon = Icons.Rounded.Delete,
                        iconBgColor = IosRed,
                        title = "Clear All Data",
                        titleColor = IosRed,
                        onClick = { showClearDialog = true },
                        isLast = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // padding for bottom nav
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Erase All Data?") },
            text = { Text("This will permanently clear your completed battery cycle history and reset settings. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = IosRed)
                ) {
                    Text("Clear All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = IosPrimary)
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = IosTextSecondary,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsListGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(IosSurface)
    ) {
        content()
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLast: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingIcon(icon, iconBgColor)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 16.sp, color = IosTextPrimary, modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = IosGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = IosSurfaceElevated,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
        if (!isLast) SettingsDivider()
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    titleColor: Color = IosTextPrimary,
    value: String? = null,
    onClick: () -> Unit,
    isLast: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingIcon(icon, iconBgColor)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 16.sp, color = titleColor, modifier = Modifier.weight(1f))
            if (value != null) {
                Text(value, fontSize = 16.sp, color = IosTextSecondary)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = IosTextTertiary)
        }
        if (!isLast) SettingsDivider()
    }
}

@Composable
fun <T> SettingsDropdownRow(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    options: List<T>,
    selectedValue: T,
    onSelected: (T) -> Unit,
    format: (T) -> String,
    isLast: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingIcon(icon, iconBgColor)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 16.sp, color = IosTextPrimary, modifier = Modifier.weight(1f))

            Box {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(format(selectedValue), fontSize = 16.sp, color = IosTextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = IosTextTertiary)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(format(option)) },
                            onClick = {
                                onSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        if (!isLast) SettingsDivider()
    }
}

@Composable
fun SettingIcon(icon: ImageVector, bgColor: Color) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SettingsDivider() {
    Divider(color = IosBorder, thickness = 1.dp, modifier = Modifier.padding(start = 62.dp))
}
