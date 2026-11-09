package com.bbinxx.onecharge.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.DpSize
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.bbinxx.onecharge.MainActivity
import com.bbinxx.onecharge.data.tracker.BatteryCycleTracker
import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun batteryCycleRepository(): BatteryCycleRepository
    fun batteryTracker(): BatteryCycleTracker
}

// iOS style colors
private val IosPrimary       = ColorProvider(Color(0xFF0A84FF))
private val IosSurface       = ColorProvider(Color(0xFF1C1C1E))
private val IosTextPrimary   = ColorProvider(Color(0xFFFFFFFF))
private val IosTextSecondary = ColorProvider(Color(0x99EBEBF5)) // 60% alpha white

class OneChargeWidget : GlanceAppWidget() {
    companion object {
        private val SMALL_SQUARE = DpSize(100.dp, 100.dp)
        private val HORIZONTAL_RECTANGLE = DpSize(250.dp, 100.dp)
        private val BIG_RECTANGLE = DpSize(250.dp, 250.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(SMALL_SQUARE, HORIZONTAL_RECTANGLE, BIG_RECTANGLE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, WidgetEntryPoint::class.java
        )
        val cycles = entryPoint.batteryCycleRepository().getActiveCycleFlow()
        val initialCycle = cycles.first()
        val screenTimes = entryPoint.batteryTracker().screenOnTimeFlow
        provideContent {
            val cycle by cycles.collectAsState(initial = initialCycle)
            val screenOnTime by screenTimes.collectAsState()
            val activeCycle = cycle?.let {
                if (screenOnTime > 0L) {
                    it.copy(screenOnTimeSeconds = it.screenOnTimeSeconds +
                        ((System.currentTimeMillis() - screenOnTime) / 1000L).coerceAtLeast(0L))
                } else it
            }
            WidgetContent(activeCycle)
        }
    }

    @Composable
    private fun WidgetContent(activeCycle: BatteryCycle?) {
        val size = LocalSize.current

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .cornerRadius(24.dp)
                .background(IosSurface)
                .padding(if (size.height <= SMALL_SQUARE.height) 8.dp else 20.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            if (activeCycle != null) {
                val hours = activeCycle.screenOnTimeSeconds / 3600
                val minutes = (activeCycle.screenOnTimeSeconds % 3600) / 60
                val sotStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                val batteryJourney = "${activeCycle.startBatteryLevel}% → ${activeCycle.currentBatteryLevel}%"

                if (size.width <= SMALL_SQUARE.width && size.height <= SMALL_SQUARE.height) {
                    SmallWidgetLayout(sotStr, batteryJourney)
                } else if (size.width <= HORIZONTAL_RECTANGLE.width && size.height <= HORIZONTAL_RECTANGLE.height) {
                    MediumWidgetLayout(activeCycle, sotStr, batteryJourney)
                } else {
                    LargeWidgetLayout(activeCycle, sotStr, batteryJourney)
                }
            } else {
                Text(
                    text = "No Active Cycle",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = IosTextPrimary)
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "Tap to start tracking",
                    style = TextStyle(fontSize = 14.sp, color = IosTextSecondary)
                )
            }
        }
    }

    @Composable
    private fun SmallWidgetLayout(sotStr: String, batteryJourney: String) {
        Text(
            text = "OneCharge",
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = IosTextSecondary)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = sotStr,
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = IosTextPrimary)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = batteryJourney,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = IosTextSecondary)
        )
    }

    @Composable
    private fun MediumWidgetLayout(cycle: BatteryCycle, sotStr: String, batteryJourney: String) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(text = "OneCharge", style = TextStyle(fontSize = 12.sp, color = IosTextSecondary))
                Text(text = sotStr, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = IosTextPrimary))
            }
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(text = batteryJourney, style = TextStyle(fontSize = 14.sp, color = IosPrimary))
                val hours = cycle.estimatedFullSotSeconds / 3600
                val minutes = (cycle.estimatedFullSotSeconds % 3600) / 60
                Text(text = "Est. ${hours}h ${minutes}m", style = TextStyle(fontSize = 12.sp, color = IosTextSecondary))
            }
        }
    }

    @Composable
    private fun LargeWidgetLayout(cycle: BatteryCycle, sotStr: String, batteryJourney: String) {
        val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        val startTimeStr = timeFormat.format(Date(cycle.startTime))

        val estHrs = cycle.estimatedFullSotSeconds / 3600
        val estMins = (cycle.estimatedFullSotSeconds % 3600) / 60
        val estStr = "${estHrs}h ${estMins}m"

        val ageHrs = cycle.durationMs / (1000 * 60 * 60)
        val ageMins = (cycle.durationMs % (1000 * 60 * 60)) / (1000 * 60)
        val ageStr = "${ageHrs}h ${ageMins}m"

        val progress = if (cycle.startBatteryLevel > 0) cycle.currentBatteryLevel.toFloat() / cycle.startBatteryLevel else 0f
        val filledBlocks = (progress * 14).toInt().coerceIn(0, 14)
        val emptyBlocks = 14 - filledBlocks
        val visualBar = "█".repeat(filledBlocks) + "░".repeat(emptyBlocks)

        Text(
            text = "Current Cycle",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = IosTextSecondary)
        )
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(
            text = sotStr,
            style = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold, color = IosTextPrimary)
        )
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(
            text = visualBar,
            style = TextStyle(fontSize = 14.sp, color = IosPrimary)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = batteryJourney,
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = IosPrimary)
        )
        Spacer(modifier = GlanceModifier.height(20.dp))
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(text = "Started", style = TextStyle(fontSize = 13.sp, color = IosTextSecondary))
                Text(text = startTimeStr, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = IosTextPrimary))
            }
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(text = "Estimated", style = TextStyle(fontSize = 13.sp, color = IosTextSecondary))
                Text(text = estStr, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = IosTextPrimary))
            }
        }
        Spacer(modifier = GlanceModifier.height(12.dp))
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(text = "Cycle Age", style = TextStyle(fontSize = 13.sp, color = IosTextSecondary))
                Text(text = ageStr, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = IosTextPrimary))
            }
        }
    }
}

class OneChargeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = OneChargeWidget()
}
