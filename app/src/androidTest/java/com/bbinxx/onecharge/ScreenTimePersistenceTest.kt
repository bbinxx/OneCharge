package com.bbinxx.onecharge

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bbinxx.onecharge.data.local.OneChargeDatabase
import com.bbinxx.onecharge.data.local.toEntity
import com.bbinxx.onecharge.domain.model.BatteryCycle
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenTimePersistenceTest {
    @Test fun recoveredSotPreservesBatteryFieldsAndNeverDecreasesTime() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, OneChargeDatabase::class.java).build()
        try {
            val dao = db.batteryCycleDao()
            val id = dao.insert(BatteryCycle(startBatteryLevel = 100, currentBatteryLevel = 75,
                startTime = 1000L, screenOnTimeSeconds = 20L).toEntity()).toInt()
            dao.updateScreenOnTime(id, 60L)
            dao.updateScreenOnTime(id, 10L)
            val cycle = dao.getActiveCycle()!!
            assertEquals(60L, cycle.screenOnTimeSeconds)
            assertEquals(75, cycle.currentBatteryLevel)
        } finally {
            db.close()
        }
    }

    @Test fun recoveryCannotModifyCompletedCycle() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, OneChargeDatabase::class.java).build()
        try {
            val dao = db.batteryCycleDao()
            val id = dao.insert(BatteryCycle(startBatteryLevel = 100, currentBatteryLevel = 10,
                startTime = 1000L, endTime = 2000L, screenOnTimeSeconds = 20L, isActive = false).toEntity()).toInt()
            dao.updateScreenOnTime(id, 60L)
            val cycle = dao.getCompletedCycles().single()
            assertEquals(20L, cycle.screenOnTimeSeconds)
            assertEquals(2000L, cycle.endTime)
        } finally {
            db.close()
        }
    }
}
