package com.innotime.trainerapp.presentation.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for duration and date formatting utilities.
 */
class FormatUtilTest {

    @Test
    fun `formatDuration with seconds only shows two decimal places`() {
        // Under 1 minute: "S.CC" format
        assertThat(formatDuration(0)).isEqualTo("0.00")
        assertThat(formatDuration(1000)).isEqualTo("1.00")
        assertThat(formatDuration(1234)).isEqualTo("1.23")
        assertThat(formatDuration(5129)).isEqualTo("5.12")
        assertThat(formatDuration(59999)).isEqualTo("59.99")
    }

    @Test
    fun `formatDuration with minutes shows M colon SS dot CC format`() {
        // 1 minute to 1 hour: "M:SS.CC" format
        assertThat(formatDuration(60_000)).isEqualTo("1:00.00")
        assertThat(formatDuration(65_460)).isEqualTo("1:05.46")
        assertThat(formatDuration(125_340)).isEqualTo("2:05.34")
        assertThat(formatDuration(3_599_990)).isEqualTo("59:59.99")
    }

    @Test
    fun `formatDuration with hours shows H colon MM colon SS dot CC format`() {
        // 1 hour or more: "H:MM:SS.CC" format
        assertThat(formatDuration(3_600_000)).isEqualTo("1:00:00.00")
        assertThat(formatDuration(3_945_670)).isEqualTo("1:05:45.67")
        assertThat(formatDuration(7_384_120)).isEqualTo("2:03:04.12")
    }

    @Test
    fun `formatDuration handles negative durations`() {
        assertThat(formatDuration(-1234)).isEqualTo("-1.23")
        assertThat(formatDuration(-65_460)).isEqualTo("-1:05.46")
        assertThat(formatDuration(-3_945_670)).isEqualTo("-1:05:45.67")
    }

    @Test
    fun `formatDuration handles millisecond precision correctly`() {
        // Test rounding behavior
        assertThat(formatDuration(1004)).isEqualTo("1.00")  // 4ms rounds down
        assertThat(formatDuration(1005)).isEqualTo("1.00")  // 5ms rounds down (int division)
        assertThat(formatDuration(1010)).isEqualTo("1.01")  // 10ms = 1 centisecond
        assertThat(formatDuration(1099)).isEqualTo("1.09")  // 99ms = 9 centiseconds
    }

    @Test
    fun `formatDuration pads zeros correctly`() {
        assertThat(formatDuration(60_050)).isEqualTo("1:00.05")  // Zero-padded seconds
        assertThat(formatDuration(3_605_070)).isEqualTo("1:00:05.07")  // Zero-padded minutes and seconds
    }

    @Test
    fun `formatDate formats timestamps correctly`() {
        // Note: This test depends on system locale settings
        // Testing with known timestamp: 2026-02-15 12:00:00 UTC
        val timestamp = 1770840000000L
        val formatted = formatDate(timestamp)

        // Should contain year, month, and day (format may vary by locale)
        assertThat(formatted).contains("2026")
        assertThat(formatted).contains("02")
        assertThat(formatted).contains("15")
    }

    @Test
    fun `formatDateTime includes both date and time`() {
        // Testing with known timestamp: 2026-02-15 14:30:00 UTC
        val timestamp = 1770849000000L
        val formatted = formatDateTime(timestamp)

        // Should contain date and time components
        assertThat(formatted).contains("2026")
        assertThat(formatted).contains("14")
        assertThat(formatted).contains("30")
    }
}
