package com.innotime.trainerapp.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.innotime.trainerapp.presentation.theme.TimerActive
import com.innotime.trainerapp.presentation.theme.TimerStopped
import com.innotime.trainerapp.presentation.util.formatDuration

enum class TimerSize {
    SMALL,
    MEDIUM,
    LARGE
}

@Composable
fun TimerDisplay(
    elapsedMs: Long?,
    isActive: Boolean,
    size: TimerSize = TimerSize.MEDIUM,
    modifier: Modifier = Modifier
) {
    val fontSize = when (size) {
        TimerSize.SMALL -> 16.sp
        TimerSize.MEDIUM -> 24.sp
        TimerSize.LARGE -> 32.sp
    }

    val color = if (isActive) TimerActive else TimerStopped

    Text(
        text = if (elapsedMs != null) formatDuration(elapsedMs) else "0.00",
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = color,
        modifier = modifier.padding(horizontal = 8.dp)
    )
}
