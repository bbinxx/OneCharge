package com.bbinxx.onecharge.ui.analytics

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bbinxx.onecharge.ui.components.AppUsageBarChart
import com.bbinxx.onecharge.ui.dashboard.formatDuration
import com.bbinxx.onecharge.ui.dashboard.formatSot
import com.bbinxx.onecharge.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadAnalytics()
    }

    Scaffold(
        containerColor = IosBg,
        topBar = {
            TopAppBar(
                title = { Text("Insights", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1.2).sp) },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Weekly Bar Chart
            if (state.heatmapData.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(IosSurface)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text("Screen Time", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = IosTextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("This Week", fontSize = 14.sp, color = IosTextSecondary)
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(formatSot(state.averageSotSecs), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = IosTextPrimary, letterSpacing = (-0.5).sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("avg", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = IosTextSecondary, modifier = Modifier.padding(bottom = 3.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Custom Bar Chart UI
                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxIntensity = state.heatmapData.maxOfOrNull { it.intensity }?.coerceAtLeast(0.1f) ?: 1f
                        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

                        state.heatmapData.forEach { dayData ->
                            // Attempt to match Java Calendar day to string for highlighting today
                            val isToday = dayData.dayOfWeek.startsWith(
                                when (today) {
                                    Calendar.MONDAY -> "M"
                                    Calendar.TUESDAY -> "T" // T or Tu
                                    Calendar.WEDNESDAY -> "W"
                                    Calendar.THURSDAY -> "T" // T or Th
                                    Calendar.FRIDAY -> "F"
                                    Calendar.SATURDAY -> "S" // S or Sa
                                    Calendar.SUNDAY -> "S" // S or Su
                                    else -> ""
                                }
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                                val heightDp = (100 * (dayData.intensity / maxIntensity)).coerceAtLeast(4f)
                                Box(
                                    modifier = Modifier
                                        .width(32.dp)
                                        .height(heightDp.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isToday) IosPrimary else IosSurfaceElevated)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = dayData.dayOfWeek.take(1),
                                    fontSize = 12.sp,
                                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isToday) IosTextPrimary else IosTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Stats Summary Grid (2-column iOS list groups)
            Text("General Averages", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = IosTextPrimary, modifier = Modifier.padding(start = 8.dp, top = 8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(IosSurface)
            ) {
                StatListItem("Avg SOT", formatSot(state.averageSotSecs), true)
                StatListItem("Avg Battery Used", String.format("%.1f%%", state.averageBatteryUsed), true)
                StatListItem("Avg Efficiency", String.format("%.1f min/%%", state.averageEfficiencyMinPerPercent), true)
                StatListItem("Best SOT", formatSot(state.bestSotSecs), true)
                StatListItem("Longest Cycle", formatDuration(state.longestCycleMs), true)
                StatListItem("Shortest Cycle", formatDuration(state.shortestCycleMs), false)
            }

            // App Screen Time Usage Section
            Text("Top App Usage", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = IosTextPrimary, modifier = Modifier.padding(start = 8.dp, top = 8.dp))

            if (state.hasUsagePermission) {
                if (state.topApps.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(IosSurface)
                            .padding(20.dp)
                            .height(260.dp)
                    ) {
                        AppUsageBarChart(
                            data = state.topApps.map { it.appName to it.usageDurationMinutes.toFloat() }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(IosSurface)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No usage stats recorded for active/recent cycle.", color = IosTextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                UsagePermissionCard(context)
            }

            Spacer(modifier = Modifier.height(100.dp)) // Nav bar padding
        }
    }
}

@Composable
fun StatListItem(
    title: String,
    value: String,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = IosTextPrimary)
            Text(value, fontSize = 16.sp, color = IosTextSecondary)
        }
        if (showDivider) {
            Divider(color = IosBorder, thickness = 1.dp, modifier = Modifier.padding(start = 20.dp))
        }
    }
}

@Composable
fun UsagePermissionCard(context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(IosSurface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("App Usage Required", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = IosRed)
        Text(
            "We need Usage Stats Access to retrieve Screen time of individual applications during your battery cycle.",
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = IosTextSecondary
        )
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IosPrimary)
        ) {
            Text("Grant Permission", fontWeight = FontWeight.SemiBold)
        }
    }
}
