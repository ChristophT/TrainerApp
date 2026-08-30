package de.hardtthelen.trainerapp.presentation.screen.training

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.hardtthelen.trainerapp.R
import de.hardtthelen.trainerapp.domain.model.Athlete
import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.Run
import de.hardtthelen.trainerapp.domain.model.RunId
import de.hardtthelen.trainerapp.domain.model.Training
import de.hardtthelen.trainerapp.domain.model.TrainingGroup
import de.hardtthelen.trainerapp.domain.model.TrainingGroupId
import de.hardtthelen.trainerapp.domain.model.TrainingId
import de.hardtthelen.trainerapp.domain.model.TrainingParticipant
import de.hardtthelen.trainerapp.presentation.component.AthleteRunCard
import de.hardtthelen.trainerapp.presentation.util.formatDate
import java.util.UUID

@Composable
fun TrainingScreen(
    onNavigateToAthletes: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrainingViewModel = hiltViewModel()
) {
    val currentTraining by viewModel.currentTraining.collectAsStateWithLifecycle()
    val athletes by viewModel.athletes.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val elapsedTimes by viewModel.currentElapsedTimes.collectAsStateWithLifecycle()
    val completedRunsByAthlete by viewModel.completedRunsByAthlete.collectAsStateWithLifecycle()

    TrainingContent(
        currentTraining = currentTraining,
        athletes = athletes,
        groups = groups,
        elapsedTimes = elapsedTimes,
        completedRunsByAthlete = completedRunsByAthlete,
        isAthleteActive = { athleteId -> viewModel.getActiveRun(athleteId) != null },
        onStartTraining = viewModel::startTraining,
        onEndTraining = viewModel::endTraining,
        onStartRun = viewModel::startRun,
        onStopRun = viewModel::stopRun,
        onUpdateRunNote = viewModel::updateRunNote,
        onAddParticipant = viewModel::addParticipant,
        onAddGroupToTraining = viewModel::addGroupToTraining,
        onNavigateToAthletes = onNavigateToAthletes,
        modifier = modifier
    )
}

@Composable
fun TrainingContent(
    currentTraining: Training?,
    athletes: List<Athlete>,
    groups: List<TrainingGroup>,
    elapsedTimes: Map<AthleteId, Long>,
    completedRunsByAthlete: Map<AthleteId, List<Run>>,
    isAthleteActive: (AthleteId) -> Boolean,
    onStartTraining: (String) -> Unit,
    onEndTraining: () -> Unit,
    onStartRun: (AthleteId) -> Unit,
    onStopRun: (AthleteId) -> Unit,
    onUpdateRunNote: (RunId, String) -> Unit,
    onAddParticipant: (AthleteId) -> Unit,
    onAddGroupToTraining: (TrainingGroupId) -> Unit,
    onNavigateToAthletes: () -> Unit,
    modifier: Modifier = Modifier
) {
    var description by remember { mutableStateOf("") }
    var showAddAthlete by remember { mutableStateOf(false) }
    var showAddGroup by remember { mutableStateOf(false) }
    var showEndSessionDialog by remember { mutableStateOf(false) }

    val altDesc = stringResource(R.string.training_default_description)

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
                        val desc = description.ifBlank { altDesc }
                        onStartTraining(desc)
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
        val participants = currentTraining.participantsByDisplayOrder()
        val nonParticipants = athletes.filter { athlete ->
            !currentTraining.participantIds().contains(athlete.id)
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

                items(participants, key = { it.athlete.id }) { participant ->
                    val athlete = participant.athlete
                    val isActive = isAthleteActive(athlete.id)
                    val lastRun = completedRunsByAthlete[athlete.id]?.firstOrNull()

                    AthleteRunCard(
                        athleteName = athlete.name,
                        elapsedMs = elapsedTimes[athlete.id],
                        isActive = isActive,
                        lastRun = lastRun,
                        onStartRun = { onStartRun(athlete.id) },
                        onStopRun = { onStopRun(athlete.id) },
                        onUpdateNote = { note ->
                            lastRun?.let { run ->
                                onUpdateRunNote(run.id, note)
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
                        if (athletes.isNotEmpty()) {
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
                        } else {
                            OutlinedButton(
                                onClick = onNavigateToAthletes,
                                modifier = Modifier.weight(1f)
                            ) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.enter_athletes))
                            }

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
                                                onAddParticipant(athlete.id)
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
                                            onAddGroupToTraining(group.id)
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
                item {
                    Button(
                        onClick = { showEndSessionDialog = true },
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

                    if (showEndSessionDialog) {
                        EndSessionDialog(
                            title = stringResource(R.string.end_session),
                            message = stringResource(R.string.end_session_message),
                            onConfirm = {
                                showEndSessionDialog = false
                                onEndTraining()
                            },
                            onDismiss = { showEndSessionDialog = false }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrainingScreenEmptyPreview() {
    MaterialTheme {
        TrainingContent(
            currentTraining = null,
            athletes = emptyList(),
            groups = emptyList(),
            elapsedTimes = emptyMap(),
            completedRunsByAthlete = emptyMap(),
            isAthleteActive = { false },
            onStartTraining = {},
            onEndTraining = {},
            onStartRun = {},
            onStopRun = {},
            onUpdateRunNote = { _, _ -> },
            onAddParticipant = {},
            onAddGroupToTraining = {},
            onNavigateToAthletes = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrainingScreenActivePreview() {
    val athlete1 = Athlete(id = AthleteId(UUID.randomUUID().toString()), name = "Max Mustermann")
    val athlete2 = Athlete(id = AthleteId(UUID.randomUUID().toString()), name = "Erika Mustermann")
    val trainingId = TrainingId(UUID.randomUUID().toString())
    
    val training = Training(
        id = trainingId,
        date = System.currentTimeMillis(),
        description = "Sprints",
        participants = listOf(TrainingParticipant(athlete1, 3),
            TrainingParticipant(athlete2, 2)),
        runIds = emptyList()
    )

    MaterialTheme {
        TrainingContent(
            currentTraining = training,
            athletes = listOf(athlete1, athlete2),
            groups = emptyList(),
            elapsedTimes = mapOf(athlete1.id to 45200L),
            completedRunsByAthlete = emptyMap(),
            isAthleteActive = { it == athlete1.id },
            onStartTraining = {},
            onEndTraining = {},
            onStartRun = {},
            onStopRun = {},
            onUpdateRunNote = { _, _ -> },
            onAddParticipant = {},
            onAddGroupToTraining = {},
            onNavigateToAthletes = {}
        )
    }
}

