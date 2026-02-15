package com.innotime.trainerapp.presentation.util

import kotlin.math.abs

/**
 * Formats a duration in milliseconds to a human-readable string.
 *
 * Format rules:
 * - Under 1 minute: "5.12" (seconds with 2 decimal places)
 * - Under 1 hour: "1:05.46" (minutes:seconds.centiseconds)
 * - 1 hour or more: "1:05:45.67" (hours:minutes:seconds.centiseconds)
 *
 * All formats show 2 decimal places for sub-second precision.
 *
 * @param durationMs Duration in milliseconds
 * @return Formatted duration string
 */
fun formatDuration(durationMs: Long): String {
    val absDuration = abs(durationMs)
    val sign = if (durationMs < 0) "-" else ""

    val totalSeconds = absDuration / 1000.0
    val hours = (absDuration / 3600000).toInt()
    val minutes = ((absDuration % 3600000) / 60000).toInt()
    val seconds = ((absDuration % 60000) / 1000).toInt()
    val centiseconds = ((absDuration % 1000) / 10).toInt()

    return when {
        hours > 0 -> {
            // Format: H:MM:SS.CC
            String.format(
                "%s%d:%02d:%02d.%02d",
                sign, hours, minutes, seconds, centiseconds
            )
        }
        minutes > 0 -> {
            // Format: M:SS.CC
            String.format(
                "%s%d:%02d.%02d",
                sign, minutes, seconds, centiseconds
            )
        }
        else -> {
            // Format: S.CC
            String.format(
                "%s%d.%02d",
                sign, seconds, centiseconds
            )
        }
    }
}

/**
 * Formats a timestamp in milliseconds to a date string.
 *
 * @param timestampMs Timestamp in milliseconds since epoch
 * @return Formatted date string (e.g., "15.02.2026")
 */
fun formatDate(timestampMs: Long): String {
    val date = java.util.Date(timestampMs)
    val format = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
    return format.format(date)
}

/**
 * Formats a timestamp in milliseconds to a date and time string.
 *
 * @param timestampMs Timestamp in milliseconds since epoch
 * @return Formatted date-time string (e.g., "15.02.2026 14:30")
 */
fun formatDateTime(timestampMs: Long): String {
    val date = java.util.Date(timestampMs)
    val format = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
    return format.format(date)
}
