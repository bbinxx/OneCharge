package com.bbinxx.onecharge.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.ToggleableStateKey
import androidx.glance.appwidget.action.actionRunCallback
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
import kotlinx.coroutines.runBlocking

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun batteryCycleRepository(): BatteryCycleRepository
    fun batteryTracker(): BatteryCycleTracker
}

class OneChargeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent(context)
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val repository = entryPoint.batteryCycleRepository()
        val tracker = entryPoint.batteryTracker()
        
        // Fetch active cycle synchronously for widget composition
        var activeCycle = runBlocking { repository.getActiveCycleFlow().first() }
        val screenOnTime = tracker.screenOnTimeFlow.value
        if (activeCycle != null && screenOnTime > 0L) {
            val deltaSecs = (System.currentTimeMillis() - screenOnTime) / 1000L
            activeCycle = activeCycle.copy(screenOnTimeSeconds = activeCycle.screenOnTimeSeconds + deltaSecs)
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
                .background(ImageProvider(android.R.drawable.dialog_holo_dark_frame)) // fallback frame or background
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            if (activeCycle != null) {
                val hours = activeCycle.screenOnTimeSeconds / 3600
                val minutes = (activeCycle.screenOnTimeSeconds % 3600) / 60
                val sotStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

                Text(
                    text = "OneCharge SOT",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(android.R.color.white)
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = sotStr,
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(android.R.color.holo_blue_light)
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "Battery: ${activeCycle.currentBatteryLevel}% (Used: ${activeCycle.batteryUsed}%)",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = ColorProvider(android.R.color.darker_gray)
                    )
                )
            } else {
                Text(
                    text = "No Active Cycle",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(android.R.color.white)
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "Tap to start tracking",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(android.R.color.darker_gray)
                    )
                )
            }
        }
    }
}

class OneChargeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = OneChargeWidget()
}
