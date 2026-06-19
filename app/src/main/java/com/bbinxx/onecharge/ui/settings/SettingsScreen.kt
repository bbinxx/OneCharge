package com.bbinxx.onecharge.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bbinxx.onecharge.domain.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section: Battery Tracking
            SettingsSectionHeader("Battery Configuration")
            
            // Full Charge Threshold Selection
            SettingsDropdownRow(
                title = "Full Charge Threshold",
                subtitle = "Active cycle resets when battery exceeds this level and charger is unplugged.",
                options = listOf(80, 85, 90, 95, 100),
                selectedValue = settings.fullChargeLevel,
                onSelected = { viewModel.updateFullChargeLevel(it) },
                format = { "$it%" }
            )

            // End Battery Threshold Selection
            SettingsDropdownRow(
                title = "End Battery Threshold",
                subtitle = "Completed cycle ends and saves when battery drops to this level.",
                options = listOf(0, 1, 2, 5),
                selectedValue = settings.endBatteryLevel,
                onSelected = { viewModel.updateEndBatteryLevel(it) },
                format = { "$it%" }
            )

            // Auto cycle toggle
            SettingsToggleRow(
                title = "Automatic Cycle Detection",
                subtitle = "Start a new cycle automatically based on thresholds.",
                checked = settings.autoCycleDetection,
                onCheckedChange = { viewModel.updateAutoCycleDetection(it) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Section: Theme & Styling
            SettingsSectionHeader("Appearance")

            SettingsDropdownRow(
                title = "Theme Mode",
                subtitle = "Select system default or customized theme colors.",
                options = ThemeMode.values().toList(),
                selectedValue = settings.themeMode,
                onSelected = { viewModel.updateThemeMode(it) },
                format = { it.name.lowercase().replaceFirstChar { char -> char.uppercase() } }
            )

            SettingsToggleRow(
                title = "Dynamic Colors",
                subtitle = "Use Material You dynamic color accents on supported devices.",
                checked = settings.dynamicColors,
                onCheckedChange = { viewModel.updateDynamicColors(it) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Section: Background & Notifications
            SettingsSectionHeader("Services & Notifications")

            SettingsToggleRow(
                title = "Persistent Notification",
                subtitle = "Keep tracking active in background with a notification showing live stats.",
                checked = settings.persistentNotification,
                onCheckedChange = { viewModel.updatePersistentNotification(it) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Section: Data & Actions
            SettingsSectionHeader("Danger Zone")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showClearDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Clear All Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text("Deletes history, settings, and active cycle tracking. This cannot be undone.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
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
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun <T> SettingsDropdownRow(
    title: String,
    subtitle: String,
    options: List<T>,
    selectedValue: T,
    onSelected: (T) -> Unit,
    format: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(format(selectedValue))
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
}
