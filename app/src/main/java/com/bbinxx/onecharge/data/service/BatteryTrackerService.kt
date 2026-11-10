package com.bbinxx.onecharge.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bbinxx.onecharge.MainActivity
import com.bbinxx.onecharge.domain.model.BatteryCycle
import com.bbinxx.onecharge.domain.model.withLiveScreenTime
import com.bbinxx.onecharge.domain.repository.BatteryCycleRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@AndroidEntryPoint
class BatteryTrackerService : Service() {

    @Inject lateinit var cycleRepository: BatteryCycleRepository
    @Inject lateinit var tracker: com.bbinxx.onecharge.data.tracker.BatteryCycleTracker

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "onecharge_tracking_channel"

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val ticker = tracker.screenOnTimeFlow.flatMapLatest { screenOnSince ->
            kotlinx.coroutines.flow.flow {
                emit(System.currentTimeMillis())
                if (screenOnSince > 0L) {
                    while (true) {
                        kotlinx.coroutines.delay(60000)
                        emit(System.currentTimeMillis())
                    }
                }
            }
        }

        kotlinx.coroutines.flow.combine(
            cycleRepository.getActiveCycleFlow(),
            tracker.screenOnTimeFlow,
            ticker
        ) { cycle, screenOnTime, now ->
            cycle?.withLiveScreenTime(screenOnTime, now)
        }
        .onEach { activeCycle ->
            updateNotification(activeCycle)
        }
        .launchIn(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification(null)
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Battery Tracking Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live Screen-On Time for the active battery cycle."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(cycle: BatteryCycle?): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (cycle != null) {
            val hours = cycle.screenOnTimeSeconds / 3600
            val minutes = (cycle.screenOnTimeSeconds % 3600) / 60
            val sotStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

            val estHrs = cycle.estimatedFullSotSeconds / 3600
            val estMins = (cycle.estimatedFullSotSeconds % 3600) / 60
            val estStr = if (estHrs > 0) "${estHrs}h ${estMins}m" else "${estMins}m"

            val title = "SOT: $sotStr  |  ${cycle.currentBatteryLevel}%"
            val text = "Estimated SOT: $estStr (Used ${cycle.batteryUsed}%)"

            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        } else {
            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("OneCharge Tracker")
                .setContentText("No active battery cycle tracking.")
                .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        }
    }

    private fun updateNotification(cycle: BatteryCycle?) {
        val notification = createNotification(cycle)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}
