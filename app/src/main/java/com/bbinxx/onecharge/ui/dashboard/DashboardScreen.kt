package com.bbinxx.onecharge.ui.dashboard

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Battery3Bar
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val activeCycle by viewModel.activeCycle.collectAsStateWithLifecycle()
    var showRestartDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = IosBg,
        topBar = {
            TopAppBar(
                title = { Text("OneCharge", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1.2).sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IosBg.copy(alpha = 0.6f),
                    titleContentColor = IosTextPrimary,
                    actionIconContentColor = IosPrimary
                ),
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
                .padding(bottom = 120.dp), // padding for bottom nav
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val cycle = activeCycle

            if (cycle != null) {
                // Large iOS Hero Card
                HeroCard(cycle = cycle)

                // 2-Column Grid
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        GridCard(
                            icon = Icons.Rounded.Battery3Bar,
                            label = "Battery",
                            value = "${cycle.currentBatteryLevel}%",
                            progress = cycle.currentBatteryLevel / 100f,
                            progressColor = IosPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        GridCard(
                            icon = Icons.Rounded.Schedule,
                            label = "Est. Full SOT",
                            value = formatSot(cycle.estimatedFullSotSeconds),
                            progress = (cycle.estimatedFullSotSeconds / (12f * 3600f)).coerceIn(0f, 1f),
                            progressColor = IosPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        GridCard(
                            icon = Icons.Rounded.Speed,
                            label = "Efficiency",
                            value = String.format("%.1f", cycle.avgMinutesPerBatteryPercent.toDouble()),
                            unit = "min/%",
                            progress = (cycle.avgMinutesPerBatteryPercent / 20.0).coerceIn(0.0, 1.0).toFloat(),
                            progressColor = IosPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        GridCard(
                            icon = Icons.Rounded.TrendingDown,
                            label = "Drain Rate",
                            value = String.format("%.1f", cycle.batteryDrainPerHour.toDouble()),
                            unit = "%/hr",
                            progress = (cycle.batteryDrainPerHour / 20.0).coerceIn(0.0, 1.0).toFloat(),
                            progressColor = IosPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        GridCard(
                            icon = Icons.Rounded.Power,
                            label = "Sessions",
                            value = "${cycle.chargingSessionsCount}",
                            progress = (cycle.chargingSessionsCount / 10f).coerceIn(0f, 1f),
                            progressColor = IosRed,
                            modifier = Modifier.weight(1f)
                        )
                        GridCard(
                            icon = Icons.Rounded.Update,
                            label = "Cycle Age",
                            value = formatDuration(cycle.durationMs),
                            progress = (cycle.durationMs / (48f * 3600f * 1000f)).coerceIn(0f, 1f),
                            progressColor = IosGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showRestartDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IosSurfaceElevated, contentColor = IosRed),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("End Cycle Manually", fontWeight = FontWeight.Bold)
                }

            } else {
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
                Row {
                    TextButton(onClick = {
                        viewModel.restartCycle(saveHistory = false)
                        showRestartDialog = false
                    }) {
                        Text("Discard & Restart", color = IosRed)
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
fun HeroCard(cycle: BatteryCycle) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF0A84FF), Color(0xFF5E5CE6))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .padding(28.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURRENT CYCLE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f),
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formatSot(cycle.screenOnTimeSeconds),
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                lineHeight = 64.sp,
                letterSpacing = (-2.5).sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${cycle.startBatteryLevel}% → ${cycle.currentBatteryLevel}%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Text(
                    text = "${formatDateTimeRelative(cycle.startTime)}, ${formatTimeOnly(cycle.startTime)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun GridCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String? = null,
    progress: Float,
    progressColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = IosSurface)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = IosTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IosTextSecondary
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = IosTextPrimary
                )
                if (unit != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = IosTextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // Spark Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(IosSurfaceElevated)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(progressColor)
                )
            }
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
        colors = CardDefaults.cardColors(containerColor = IosSurface)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No Active Cycle", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = IosTextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Charge your device to start automatic tracking.",
                style = MaterialTheme.typography.bodyMedium,
                color = IosTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStart,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IosPrimary)
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

fun formatDateTimeRelative(timestamp: Long): String {
    return if (DateUtils.isToday(timestamp)) {
        "Today"
    } else if (DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS)) {
        "Yesterday"
    } else {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}

fun formatTimeOnly(timestamp: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}
