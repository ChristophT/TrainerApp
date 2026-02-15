package com.innotime.trainerapp.presentation.screen.athletes

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
import com.innotime.trainerapp.domain.model.Athlete
import com.innotime.trainerapp.presentation.screen.training.TrainingViewModel

@Composable
fun AthletesScreen(
    viewModel: TrainingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val athletes by viewModel.athletes.collectAsStateWithLifecycle()
    var newAthleteName by remember { mutableStateOf("") }
    var editingAthleteId by remember { mutableStateOf<String?>(null) }
    var editName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = stringResource(R.string.athletes_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Add new athlete
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newAthleteName,
                onValueChange = { newAthleteName = it },
                placeholder = { Text(stringResource(R.string.new_athlete_name)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(
                onClick = {
                    if (newAthleteName.isNotBlank()) {
                        viewModel.addAthlete(newAthleteName.trim())
                        newAthleteName = ""
                    }
                },
                enabled = newAthleteName.isNotBlank(),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add_button),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Athletes list
        if (athletes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_athletes_yet),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(athletes, key = { it.id }) { athlete ->
                    AthleteItem(
                        athlete = athlete,
                        isEditing = editingAthleteId == athlete.id,
                        editName = editName,
                        onEditNameChange = { editName = it },
                        onStartEdit = {
                            editingAthleteId = athlete.id
                            editName = athlete.name
                        },
                        onSaveEdit = {
                            if (editName.isNotBlank()) {
                                viewModel.updateAthlete(athlete.id, editName.trim())
                                editingAthleteId = null
                            }
                        },
                        onCancelEdit = { editingAthleteId = null },
                        onDelete = { viewModel.deleteAthlete(athlete.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AthleteItem(
    athlete: Athlete,
    isEditing: Boolean,
    editName: String,
    onEditNameChange: (String) -> Unit,
    onStartEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditing) {
                // Edit mode
                OutlinedTextField(
                    value = editName,
                    onValueChange = onEditNameChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = onSaveEdit) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.confirm),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onCancelEdit) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cancel),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Display mode
                Text(
                    text = athlete.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onStartEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.cd_edit_button),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.cd_delete_button),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
