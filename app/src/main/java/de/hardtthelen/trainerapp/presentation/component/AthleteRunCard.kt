package de.hardtthelen.trainerapp.presentation.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Square
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.hardtthelen.trainerapp.R
import de.hardtthelen.trainerapp.domain.model.Run
import de.hardtthelen.trainerapp.presentation.util.formatDuration

@Composable
fun AthleteRunCard(
    athleteName: String,
    elapsedMs: Long?,
    isActive: Boolean,
    lastRun: Run?,
    onStartRun: () -> Unit,
    onStopRun: () -> Unit,
    onUpdateNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNoteInput by remember(lastRun?.id) { mutableStateOf(false) }
    var noteText by remember(lastRun?.id) { mutableStateOf(lastRun?.note ?: "") }

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
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = athleteName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )

                    if (showNoteInput) {
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.add_note_hint),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, end = 8.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        onUpdateNote(noteText.trim())
                                        showNoteInput = false
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )
                    } else if (lastRun != null && lastRun.durationMs != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.last_run,
                                    formatDuration(lastRun.durationMs)
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (lastRun.note.isNotEmpty()) {
                                Text(
                                    text = "• ${lastRun.note}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.clickable { showNoteInput = true }
                                )
                            } else if (!isActive) {
                                Text(
                                    text = stringResource(R.string.add_note),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { showNoteInput = true }
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
                            modifier = Modifier.size(75.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Square,
                                contentDescription = stringResource(R.string.stop_run_for, athleteName),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onStartRun,
                            modifier = Modifier.size(75.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.start_run_for, athleteName),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }

        }
    }
}
