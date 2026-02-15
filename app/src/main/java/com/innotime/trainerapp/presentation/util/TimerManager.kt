package com.innotime.trainerapp.presentation.util

import android.os.SystemClock

/**
 * Manages high-precision timing for athlete runs.
 *
 * Uses SystemClock.elapsedRealtime() which provides millisecond precision
 * and continues counting even during device sleep (unlike uptimeMillis).
 */
object TimerManager {
    /**
     * Returns the current time in milliseconds.
     * This is a monotonic clock that continues to tick during deep sleep.
     *
     * @return Current time in milliseconds since boot
     */
    fun now(): Long = SystemClock.elapsedRealtime()

    /**
     * Returns the current wall-clock time in milliseconds since epoch.
     * Use this for timestamps that need to correspond to real-world time.
     *
     * @return Current time in milliseconds since Unix epoch
     */
    fun wallClockNow(): Long = System.currentTimeMillis()
}
