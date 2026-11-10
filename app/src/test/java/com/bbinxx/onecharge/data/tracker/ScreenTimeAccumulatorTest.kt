package com.bbinxx.onecharge.data.tracker

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenTimeAccumulatorTest {
    @Test fun openIntervalIsNotIncludedInStoredTime() {
        val times = ScreenTimeAccumulator(10_000L, 100_000L)
        times.onScreenState(20_000L, true)
        times.onScreenState(40_000L, false)
        times.onScreenState(60_000L, true)
        assertEquals(20L, times.closedSeconds)
        assertEquals(60_000L, times.openSince)
        assertEquals(60L, times.totalSeconds)
    }

    @Test fun firstScreenOffIncludesIntervalStartingAtCycleBoundary() {
        val times = ScreenTimeAccumulator(10_000L, 100_000L)
        times.onScreenState(30_000L, false)
        assertEquals(20L, times.closedSeconds)
        assertEquals(0L, times.openSince)
    }

    @Test fun repeatedScreenOnDoesNotDiscardTime() {
        val times = ScreenTimeAccumulator(10_000L, 100_000L)
        times.onScreenState(20_000L, true)
        times.onScreenState(30_000L, true)
        times.onScreenState(40_000L, false)
        assertEquals(20L, times.closedSeconds)
    }

    @Test fun restartDoesNotCountTimeWhileDeviceWasOff() {
        val times = ScreenTimeAccumulator(10_000L, 100_000L)
        times.onScreenState(20_000L, true)
        times.onDeviceStopped(30_000L)
        times.onDeviceStarted()
        times.onScreenState(70_000L, true)
        assertEquals(10L, times.closedSeconds)
        assertEquals(40L, times.totalSeconds)
    }

    @Test fun noEventsDoesNotInventScreenTime() {
        assertEquals(0L, ScreenTimeAccumulator(10_000L, 100_000L).totalSeconds)
    }
}
