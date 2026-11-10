package com.bbinxx.onecharge.data.tracker

import android.app.usage.UsageEvents
import android.app.usage.UsageEventsQuery
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.PowerManager
import com.bbinxx.onecharge.domain.repository.AppUsageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Queries only screen/device transitions, never app-activity history or a polling loop. */
class ScreenTimeReader @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appUsageRepository: AppUsageRepository
) {
    internal suspend fun read(start: Long, end: Long): ScreenTimeAccumulator? = withContext(Dispatchers.IO) {
        if (start >= end || !appUsageRepository.hasUsagePermission()) return@withContext null
        val manager = context.getSystemService(UsageStatsManager::class.java)
        val query = UsageEventsQuery.Builder(start, end).setEventTypes(
            UsageEvents.Event.SCREEN_INTERACTIVE,
            UsageEvents.Event.SCREEN_NON_INTERACTIVE,
            UsageEvents.Event.DEVICE_SHUTDOWN,
            UsageEvents.Event.DEVICE_STARTUP
        ).build()
        val events = try {
            manager.queryEvents(query)
        } catch (_: SecurityException) {
            // Access may be revoked between checking permission and querying.
            null
        } ?: return@withContext null
        val totals = ScreenTimeAccumulator(start, end)
        val event = UsageEvents.Event()
        var hasEvents = false
        while (events.hasNextEvent()) {
            ensureActive()
            events.getNextEvent(event)
            hasEvents = true
            when (event.eventType) {
                UsageEvents.Event.SCREEN_INTERACTIVE -> totals.onScreenState(event.timeStamp, true)
                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> totals.onScreenState(event.timeStamp, false)
                UsageEvents.Event.DEVICE_SHUTDOWN -> totals.onDeviceStopped(event.timeStamp)
                UsageEvents.Event.DEVICE_STARTUP -> totals.onDeviceStarted()
            }
        }
        // A cycle can start partway through an already interactive screen session.
        if (!hasEvents && context.getSystemService(PowerManager::class.java).isInteractive) {
            totals.onScreenState(start, true)
        }
        totals
    }
}
