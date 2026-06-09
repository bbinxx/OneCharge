package com.bbinxx.onecharge.ui.analytics

import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bbinxx.onecharge.ui.components.AppUsageBarChart
import com.bbinxx.onecharge.ui.components.SotLineChart
import com.bbinxx.onecharge.ui.components.WeeklyBarChart
import com.bbinxx.onecharge.ui.dashboard.formatDuration
import com.bbinxx.onecharge.ui.dashboard.formatSot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Refresh when screen is displayed to check permission updates
    LaunchedEffect(Unit) {
        viewModel.loadAnalytics()
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) }
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
            // Stats Summary Grid
            Text("General Averages", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryStatCard(
                    title = "Avg SOT",
                    value = formatSot(state.averageSotSecs),
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Avg Battery Used",
                    value = String.format("%.1f%%", state.averageBatteryUsed),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryStatCard(
                    title = "Avg Efficiency",
                    value = String.format("%.1f min/%%", state.averageEfficiencyMinPerPercent),
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Best SOT",
                    value = formatSot(state.bestSotSecs),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryStatCard(
                    title = "Longest Cycle",
                    value = formatDuration(state.longestCycleMs),
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Shortest Cycle",
                    value = formatDuration(state.shortestCycleMs),
                    modifier = Modifier.weight(1f)
                )
            }

            // SOT Trend Chart Card
            if (state.trendSotHours.isNotEmpty()) {
                Text("SOT Trend (Last 7 Cycles)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        SotLineChart(
                            data = state.trendSotHours,
                            labels = state.trendLabels
                        )
                    }
                }
            }

            // Weekly SOT Chart Card
            if (state.weeklySotHours.isNotEmpty()) {
                Text("Weekly SOT Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        WeeklyBarChart(
                            data = state.weeklySotHours
                        )
                    }
                }
            }

            // App Screen Time Usage Section
            Text("Top App Usage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (state.hasUsagePermission) {
                if (state.topApps.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                            AppUsageBarChart(
                                data = state.topApps.map { it.appName to it.usageDurationMinutes.toFloat() }
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No usage stats recorded for active/recent cycle.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                UsagePermissionCard(context)
            }
        }
    }
}

@Composable
fun SummaryStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun UsagePermissionCard(context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("App Usage Stats Required", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            Text(
                "We need Usage Stats Access to retrieve Screen time of individual applications during your battery cycle.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Grant Permission")
            }
        }
    }
}
