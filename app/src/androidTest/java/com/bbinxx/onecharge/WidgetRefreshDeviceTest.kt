package com.bbinxx.onecharge

import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bbinxx.onecharge.ui.widget.OneChargeWidget
import com.bbinxx.onecharge.ui.widget.WidgetRefreshWorker
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetRefreshDeviceTest {
    @Test fun refreshWorkerAdvancesInstalledWidgetState() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val ids = GlanceAppWidgetManager(context).getGlanceIds(OneChargeWidget::class.java)
        assumeTrue("Place a OneCharge widget before running this integration test", ids.isNotEmpty())
        val widget = OneChargeWidget()
        val revision = longPreferencesKey("refresh_revision")
        val before = widget.getAppWidgetState<Preferences>(context, ids.first())[revision] ?: 0L
        // Exercise the same worker as the periodic schedule without waiting fifteen minutes.
        val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build()
        val manager = WorkManager.getInstance(context)
        manager.enqueue(request)
        val result = withTimeout(30_000L) {
            manager.getWorkInfoByIdFlow(request.id).filterNotNull().first { it.state.isFinished }
        }
        assertEquals(WorkInfo.State.SUCCEEDED, result.state)
        val after = widget.getAppWidgetState<Preferences>(context, ids.first())[revision] ?: 0L
        assertTrue("Background refresh must invalidate persisted widget state", after > before)
    }
}
