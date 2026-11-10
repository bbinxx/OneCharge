package com.bbinxx.onecharge.data.tracker

/** Streaming screen-state accounting; open time must never be persisted as closed time. */
internal class ScreenTimeAccumulator(private val start: Long, private val end: Long) {
    private var closedMs = 0L
    var openSince = 0L
        private set
    private var sawState = false

    val closedSeconds: Long get() = closedMs / 1000L
    val totalSeconds: Long get() = (closedMs + if (openSince > 0L) (end - openSince).coerceAtLeast(0L) else 0L) / 1000L

    fun onScreenState(timestamp: Long, interactive: Boolean) {
        if (timestamp < start || timestamp > end) return
        if (interactive) {
            if (openSince == 0L) openSince = timestamp
        } else {
            val since = if (!sawState) start else openSince
            if (since > 0L) closedMs += (timestamp - since).coerceAtLeast(0L)
            openSince = 0L
        }
        sawState = true
    }

    fun onDeviceStopped(timestamp: Long) {
        if (openSince > 0L) onScreenState(timestamp, false)
        sawState = true
    }

    fun onDeviceStarted() {
        openSince = 0L
        sawState = true
    }
}
