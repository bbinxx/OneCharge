package com.bbinxx.onecharge.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

internal val WidgetRefreshRevision = longPreferencesKey("refresh_revision")

/** One schedule shared by every instance. No work remains after the last widget is removed. */
internal object WidgetRefreshScheduler {
    const val WORK_NAME = "onecharge-widget-refresh"

    fun reconcile(context: Context) {
        val widgets = AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(context, OneChargeWidgetReceiver::class.java)
        )
        val manager = WorkManager.getInstance(context)
        if (widgets.isEmpty()) {
            manager.cancelUniqueWork(WORK_NAME)
        } else {
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15, TimeUnit.MINUTES).build()
            manager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}

suspend fun refreshOneChargeWidgets(context: Context) {
    val widget = OneChargeWidget()
    for (id in GlanceAppWidgetManager(context).getGlanceIds(OneChargeWidget::class.java)) {
        updateAppWidgetState(context, id) { state ->
            state[WidgetRefreshRevision] = (state[WidgetRefreshRevision] ?: 0L) + 1L
        }
        widget.update(context, id)
    }
}

class WidgetRefreshWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        try {
            refreshOneChargeWidgets(applicationContext)
        } catch (error: java.io.IOException) {
            Log.w("OneChargeWidget", "Widget state is temporarily unavailable", error)
            return Result.retry()
        }
        return Result.success()
    }
}
