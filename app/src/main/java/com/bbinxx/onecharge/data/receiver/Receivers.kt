package com.bbinxx.onecharge.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.bbinxx.onecharge.data.tracker.BatteryCycleTracker
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import android.util.Log
import javax.inject.Inject
import com.bbinxx.onecharge.domain.repository.UserSettingsRepository
import com.bbinxx.onecharge.data.service.BatteryTrackerService

// Entry point to retrieve BatteryCycleTracker from the Hilt graph for
// dynamically registered receivers (which don't support @AndroidEntryPoint injection)
@EntryPoint
@InstallIn(SingletonComponent::class)
interface TrackerEntryPoint {
    fun tracker(): BatteryCycleTracker
}

private fun Context.tracker(): BatteryCycleTracker =
    EntryPointAccessors.fromApplication(applicationContext, TrackerEntryPoint::class.java).tracker()

// BatteryReceiver is registered dynamically in OneChargeApp — NOT in the manifest.
// Dynamic receivers do NOT support @AndroidEntryPoint member injection.
// We use EntryPointAccessors to obtain the tracker from the Hilt singleton component.
class BatteryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val tracker = context.tracker()
        runAsync {
            when (action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    if (level != -1) {
                        tracker.onBatteryLevelChanged(level)
                    }
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    tracker.onPowerConnected()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    tracker.onPowerDisconnected()
                }
            }
        }
    }
}

class ScreenStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val tracker = context.tracker()
        when (action) {
            Intent.ACTION_SCREEN_ON -> tracker.onScreenOn()
            Intent.ACTION_SCREEN_OFF -> tracker.onScreenOff()
        }
    }
}

// BootReceiver IS declared in the manifest, so @AndroidEntryPoint works correctly here.
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var tracker: BatteryCycleTracker
    @Inject lateinit var settingsRepository: UserSettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            runAsync {
                tracker.syncWithUsageStats()

                val settings = settingsRepository.getUserSettings()
                if (settings.persistentNotification) {
                    val serviceIntent = Intent(context, BatteryTrackerService::class.java)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
            }
        }
    }
}

// Keep the process eligible to finish short receiver work after onReceive returns.
private fun BroadcastReceiver.runAsync(block: suspend () -> Unit) {
    val pendingResult = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            block()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.e("OneChargeReceiver", "Unable to process broadcast", error)
        } finally {
            pendingResult.finish()
        }
    }
}
