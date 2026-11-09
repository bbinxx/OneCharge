package com.bbinxx.onecharge

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import com.bbinxx.onecharge.data.receiver.BatteryReceiver
import com.bbinxx.onecharge.data.receiver.ScreenStateReceiver
import dagger.hilt.android.HiltAndroidApp
import androidx.core.content.ContextCompat

@HiltAndroidApp
class OneChargeApp : Application() {

    // BroadcastReceivers annotated with @AndroidEntryPoint cannot be injected
    // into other Hilt components. Instantiate them normally — Hilt will inject
    // their own @Inject fields when onReceive is dispatched by the OS.
    private val batteryReceiver = BatteryReceiver()
    private val screenStateReceiver = ScreenStateReceiver()

    override fun onCreate() {
        super.onCreate()

        // Register battery receiver dynamically
        val batteryFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        ContextCompat.registerReceiver(this, batteryReceiver, batteryFilter, ContextCompat.RECEIVER_NOT_EXPORTED)

        // Register screen state receiver dynamically
        val screenFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        ContextCompat.registerReceiver(this, screenStateReceiver, screenFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onTerminate() {
        super.onTerminate()
        try {
            unregisterReceiver(batteryReceiver)
            unregisterReceiver(screenStateReceiver)
        } catch (_: IllegalArgumentException) {
            // Already unregistered
        }
    }
}
