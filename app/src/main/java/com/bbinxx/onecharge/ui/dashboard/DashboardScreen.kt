package com.bbinxx.onecharge.ui.dashboard

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bbinxx.onecharge.domain.model.BatteryCycle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val activeCycle by viewModel.activeCycle.collectAsState()
    var showRestartDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("OneCharge", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.refreshStats() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync SOT")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val cycle = activeCycle

            if (cycle != null) {
                // Large SOT Card with Gradient Background
                SotHeroCard(cycle)

                // Detailed metrics in a clean grid-like spacing
                Text("Current Cycle Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricCard(
                        title = "Battery Used",
                        value = "${cycle.batteryUsed}%",
                        subtitle = "${cycle.startBatteryLevel}% → ${cycle.currentBatteryLevel}%",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Est. Full SOT",
                        value = formatSot(cycle.estimatedFullSotSeconds),
                        subtitle = "Based on current drain",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricCard(
                        title = "Cycle Started",
                        value = formatDateTime(cycle.startTime),
                        subtitle = if (cycle.isManual) "Started manually" else "Started automatically",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Cycle Age",
                        value = formatDuration(cycle.durationMs),
                        subtitle = "Total elapsed time",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricCard(
                        title = "Charging Sessions",
                        value = "${cycle.chargingSessionsCount}",
                        subtitle = "${cycle.chargingDurationSeconds / 60}m total charging",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Drain Speed",
                        value = String.format("%.1f%% / hr", cycle.batteryDrainPerHour),
                        subtitle = String.format("Avg %.1f min / %%", cycle.avgMinutesPerBatteryPercent),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showRestartDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Restart Monitoring Session", fontWeight = FontWeight.Bold)
                }

            } else {
                // Empty State when no active cycle is tracking
                NoActiveCycleState(onStart = { viewModel.startManualCycle() })
            }
        }
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("Restart Battery Cycle?") },
            text = { Text("This will stop the current tracking session. Would you like to save it to history?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.restartCycle(saveHistory = true)
                    showRestartDialog = false
                }) {
                    Text("Save & Restart")
                }
            },
            dismissButton = {
                androidx.compose.foundation.layout.Row {
                    TextButton(onClick = {
                        viewModel.restartCycle(saveHistory = false)
                        showRestartDialog = false
                    }) {
                        Text("Discard & Restart", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { showRestartDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun SotHeroCard(cycle: BatteryCycle) {
    val gradient = Brush.linearGradient(
        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Screen-On Time", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatSot(cycle.screenOnTimeSeconds),
                fontSize = 54.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary,
                lineHeight = 60.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Battery level: ${cycle.currentBatteryLevel}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun NoActiveCycleState(onStart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No Active Cycle Running", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Charge your device to the configured full level (e.g. 100%) or start tracking manually below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStart,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Tracking Manually")
            }
        }
    }
}

// Helpers
fun formatSot(sotSeconds: Long): String {
    val hours = sotSeconds / 3600
    val minutes = (sotSeconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

fun formatDuration(durationMs: Long): String {
    val hours = durationMs / (1000 * 60 * 60)
    val minutes = (durationMs % (1000 * 60 * 60)) / (1000 * 60)
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

fun formatDateTime(timestamp: Long): String {
    return if (DateUtils.isToday(timestamp)) {
        "Today, " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
    } else if (DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS)) {
        "Yesterday, " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
    } else {
        SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}
