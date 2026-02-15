package com.innotime.trainerapp.presentation.screen.training

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.innotime.trainerapp.R
import com.innotime.trainerapp.presentation.component.AthleteRunCard
import com.innotime.trainerapp.presentation.util.formatDate

@Composable
fun TrainingScreen(
    viewModel: TrainingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val currentTraining by viewModel.currentTraining.collectAsStateWithLifecycle()
    val athletes by viewModel.athletes.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val elapsedTimes by viewModel.currentElapsedTimes.collectAsStateWithLifecycle()

    var description by remember { mutableStateOf("") }
    var showAddAthlete by remember { mutableStateOf(false) }
    var showAddGroup by remember { mutableStateOf(false) }

    if (currentTraining == null) {
        // No active session - show start screen
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.training_title),
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = stringResource(R.string.training_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text(stringResource(R.string.training_description_hint)) },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    singleLine = true
                )

                Button(
                    onClick = {
                        val desc = description.ifBlank {
                            stringResource(R.string.training_default_description)
                        }
                        viewModel.startTraining(desc)
                        description = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.start_session),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    } else {
        // Active session
        val participants = athletes.filter { athlete ->
            currentTraining.participantIds.contains(athlete.id)
        }
        val nonParticipants = athletes.filter { athlete ->
            !currentTraining.participantIds.contains(athlete.id)
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTraining.description,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${formatDate(currentTraining.date)} · ${
                            stringResource(
                                if (participants.size == 1) R.string.athlete_count else R.string.athletes_count,
                                participants.size
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { viewModel.endTraining() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.end_session))
                }
            }

            // Athlete run cards
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (participants.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.no_athletes_added),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(R.string.add_athletes_or_group),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(participants, key = { it.id }) { athlete ->
                    val activeRun = viewModel.getActiveRun(athlete.id)
                    val completedRuns by viewModel.getCompletedRuns(athlete.id).collectAsStateWithLifecycle()
                    val lastRun = completedRuns.lastOrNull()

                    AthleteRunCard(
                        athleteName = athlete.name,
                        athleteId = athlete.id,
                        elapsedMs = elapsedTimes[athlete.id],
                        isActive = activeRun != null,
                        lastRun = lastRun,
                        onStartRun = { viewModel.startRun(athlete.id) },
                        onStopRun = { viewModel.stopRun(athlete.id) },
                        onUpdateNote = { note ->
                            lastRun?.let { run ->
                                viewModel.updateRunNote(run.id, note)
                            }
                        }
                    )
                }

                // Add athlete/group buttons
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showAddAthlete = !showAddAthlete
                                showAddGroup = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.add_athlete))
                        }

                        if (groups.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    showAddGroup = !showAddGroup
                                    showAddAthlete = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GroupAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.add_group))
                            }
                        }
                    }
                }

                // Add athlete picker
                item {
                    AnimatedVisibility(
                        visible = showAddAthlete,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (nonParticipants.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.all_athletes_added),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                } else {
                                    nonParticipants.forEach { athlete ->
                                        Button(
                                            onClick = {
                                                viewModel.addParticipant(athlete.id)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(athlete.name)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Add group picker
                item {
                    AnimatedVisibility(
                        visible = showAddGroup,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                groups.forEach { group ->
                                    Button(
                                        onClick = {
                                            viewModel.addGroupToTraining(group.id)
                                            showAddGroup = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GroupAdd,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            stringResource(
                                                R.string.group_with_count,
                                                group.name,
                                                group.memberIds.size
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
