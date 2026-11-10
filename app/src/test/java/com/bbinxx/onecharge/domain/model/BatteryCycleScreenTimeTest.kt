package com.bbinxx.onecharge.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryCycleScreenTimeTest {
    private val cycle = BatteryCycle(startBatteryLevel = 100, currentBatteryLevel = 90,
        startTime = 60_000L, screenOnTimeSeconds = 20)

    @Test fun cycleStartedWhileScreenOnExcludesEarlierTime() {
        assertEquals(50L, cycle.withLiveScreenTime(10_000L, 90_000L).screenOnTimeSeconds)
    }

    @Test fun futureScreenTimestampDoesNotSubtractStoredTime() {
        assertEquals(20L, cycle.withLiveScreenTime(100_000L, 90_000L).screenOnTimeSeconds)
    }

    @Test fun completedCycleNeverAccruesLiveTime() {
        assertEquals(20L, cycle.copy(isActive = false, endTime = 80_000L)
            .withLiveScreenTime(60_000L, 90_000L).screenOnTimeSeconds)
    }

    @Test fun screenOffKeepsStoredTime() {
        assertEquals(20L, cycle.withLiveScreenTime(0, 90_000L).screenOnTimeSeconds)
    }
}
