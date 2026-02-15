package com.innotime.trainerapp.presentation.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Square
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.innotime.trainerapp.R
import com.innotime.trainerapp.domain.model.Run
import com.innotime.trainerapp.presentation.util.formatDuration

@Composable
fun AthleteRunCard(
    athleteName: String,
    athleteId: String,
    elapsedMs: Long?,
    isActive: Boolean,
    lastRun: Run?,
    onStartRun: () -> Unit,
    onStopRun: () -> Unit,
    onUpdateNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNoteInput by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    val borderColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val backgroundColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Main row with name, timer, and button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Athlete name and last run info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = athleteName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    if (lastRun != null && !isActive && lastRun.durationMs != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.last_run, formatDuration(lastRun.durationMs)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (lastRun.note.isNotEmpty()) {
                                Text(
                                    text = "📝",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Timer and button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimerDisplay(
                        elapsedMs = elapsedMs,
                        isActive = isActive,
                        size = if (isActive) TimerSize.LARGE else TimerSize.MEDIUM
                    )

                    if (isActive) {
                        Button(
                            onClick = onStopRun,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Square,
                                contentDescription = stringResource(R.string.stop_run_for, athleteName)
                            )
                        }
                    } else {
                        Button(
                            onClick = onStartRun,
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.start_run_for, athleteName)
                            )
                        }
                    }
                }
            }

            // Note input (shown after stopping a run)
            if (!isActive && lastRun != null && lastRun.note.isEmpty() && !showNoteInput) {
                TextButton(
                    onClick = { showNoteInput = true },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.add_note))
                }
            }

            if (showNoteInput) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text(stringResource(R.string.add_note_hint)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (noteText.isNotBlank()) {
                                onUpdateNote(noteText.trim())
                                noteText = ""
                                showNoteInput = false
                            }
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}
