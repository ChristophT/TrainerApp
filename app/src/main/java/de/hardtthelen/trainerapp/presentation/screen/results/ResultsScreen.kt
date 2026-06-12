package de.hardtthelen.trainerapp.presentation.screen.results

import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.hardtthelen.trainerapp.R
import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.TrainingId
import de.hardtthelen.trainerapp.presentation.screen.training.TrainingViewModel
import de.hardtthelen.trainerapp.presentation.util.CSVExporter
import de.hardtthelen.trainerapp.presentation.util.formatDate
import de.hardtthelen.trainerapp.presentation.util.formatDuration

@Composable
fun ResultsScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val athletes by viewModel.athletes.collectAsStateWithLifecycle()
    val trainings by viewModel.trainings.collectAsStateWithLifecycle()
    val runsPerTraining by viewModel.runsPerTraining.collectAsStateWithLifecycle()

    var selectedAthleteId by remember { mutableStateOf<AthleteId?>(null) }
    var expandedTrainingId by remember { mutableStateOf<TrainingId?>(null) }

    // Sort trainings newest first
    val sortedTrainings = remember(trainings) {
        trainings.sortedByDescending { it.date }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = stringResource(R.string.results_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Athlete filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "All" filter
            FilterChip(
                selected = selectedAthleteId == null,
                onClick = { selectedAthleteId = null },
                label = { Text(stringResource(R.string.filter_all)) }
            )

            // Athlete filters
            athletes.forEach { athlete ->
                FilterChip(
                    selected = selectedAthleteId == athlete.id,
                    onClick = { selectedAthleteId = athlete.id },
                    label = { Text(athlete.name) }
                )
            }
        }

        // Export button (when athlete selected)
        selectedAthleteId?.let { athleteId ->
            val athlete = athletes.find { it.id == athleteId }
            val athleteRuns by viewModel.getRunsForAthlete(athleteId).collectAsStateWithLifecycle()

            if (athlete != null) {
                Button(
                    onClick = {
                        CSVExporter.exportAndShare(
                            context = context,
                            athlete = athlete,
                            runs = athleteRuns,
                            trainings = trainings
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.export_csv))
                }
            }
        }

        // Training list
        if (sortedTrainings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_training_sessions),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(sortedTrainings, key = { it.id }) { training ->
                    val trainingRuns = runsPerTraining[training.id] ?: emptyList()

                    val filteredRuns = trainingRuns
                        .filter { it.durationMs != null }
                        .filter { run ->
                            selectedAthleteId == null || run.athleteId == selectedAthleteId
                        }

                    // Skip training if no runs match the filter
                    if (selectedAthleteId != null && filteredRuns.isEmpty()) {
                        return@items
                    }

                    TrainingResultItem(
                        training = training,
                        runs = filteredRuns,
                        athletes = athletes,
                        isExpanded = expandedTrainingId == training.id,
                        onToggleExpand = {
                            expandedTrainingId = if (expandedTrainingId == training.id) {
                                null
                            } else {
                                training.id
                            }
                        },
                        onDelete = {
                            viewModel.deleteFinishedTrainingSession(it)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrainingResultItem(
    training: de.hardtthelen.trainerapp.domain.model.Training,
    runs: List<de.hardtthelen.trainerapp.domain.model.Run>,
    athletes: List<de.hardtthelen.trainerapp.domain.model.Athlete>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: (TrainingId) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onToggleExpand
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = training.description,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${formatDate(training.date)} · ${
                                stringResource(
                                    if (runs.size == 1) R.string.run_count else R.string.runs_count,
                                    runs.size
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_delete_button),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    if (showDeleteDialog) {
                        DeleteConfirmationDialog(
                            title = stringResource(R.string.delete_session),
                            message = stringResource(R.string.delete_session_message, training.description),
                            onConfirm = {
                                showDeleteDialog = false
                                onDelete(training.id)
                            },
                            onDismiss = {
                                showDeleteDialog = false
                            }
                        )
                    }
                }
            }

            // Expanded runs list
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                    if (runs.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_runs_recorded),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        runs.forEach { run ->
                            val athlete = athletes.find { it.id == run.athleteId }
                            val athleteName = athlete?.name ?: stringResource(R.string.unknown_athlete)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = athleteName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Column(
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = formatDuration(run.durationMs!!),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (run.note.isNotEmpty()) {
                                            Text(
                                                text = run.note,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
}
