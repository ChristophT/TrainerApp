package com.innotime.trainerapp.presentation.screen.groups

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
import com.innotime.trainerapp.domain.model.Athlete
import com.innotime.trainerapp.domain.model.TrainingGroup
import com.innotime.trainerapp.presentation.screen.training.TrainingViewModel

@Composable
fun GroupsScreen(
    viewModel: TrainingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val athletes by viewModel.athletes.collectAsStateWithLifecycle()
    var newGroupName by remember { mutableStateOf("") }
    var editingGroupId by remember { mutableStateOf<String?>(null) }
    var editName by remember { mutableStateOf("") }
    var expandedGroupId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = stringResource(R.string.groups_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Add new group
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newGroupName,
                onValueChange = { newGroupName = it },
                placeholder = { Text(stringResource(R.string.new_group_name)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(
                onClick = {
                    if (newGroupName.isNotBlank()) {
                        viewModel.addGroup(newGroupName.trim())
                        newGroupName = ""
                    }
                },
                enabled = newGroupName.isNotBlank(),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add_button),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Groups list
        if (groups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_groups_yet),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(groups, key = { it.id }) { group ->
                    GroupItem(
                        group = group,
                        athletes = athletes,
                        isExpanded = expandedGroupId == group.id,
                        isEditing = editingGroupId == group.id,
                        editName = editName,
                        onEditNameChange = { editName = it },
                        onToggleExpand = {
                            expandedGroupId = if (expandedGroupId == group.id) null else group.id
                        },
                        onStartEdit = {
                            editingGroupId = group.id
                            editName = group.name
                        },
                        onSaveEdit = {
                            if (editName.isNotBlank()) {
                                viewModel.updateGroup(group.id, editName.trim())
                                editingGroupId = null
                            }
                        },
                        onCancelEdit = { editingGroupId = null },
                        onDelete = { viewModel.deleteGroup(group.id) },
                        onToggleMember = { athleteId ->
                            viewModel.toggleGroupMember(group.id, athleteId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupItem(
    group: TrainingGroup,
    athletes: List<Athlete>,
    isExpanded: Boolean,
    isEditing: Boolean,
    editName: String,
    onEditNameChange: (String) -> Unit,
    onToggleExpand: () -> Unit,
    onStartEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleMember: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
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
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onToggleExpand) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                contentDescription = if (isExpanded) "Collapse" else "Expand"
                            )
                        }
                        Column {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(
                                    if (group.memberIds.size == 1) R.string.member_count else R.string.members_count,
                                    group.memberIds.size
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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

            // Expanded members list
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
                    Divider(modifier = Modifier.padding(bottom = 12.dp))

                    if (athletes.isEmpty()) {
                        Text(
                            text = stringResource(R.string.add_athletes_first),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        athletes.forEach { athlete ->
                            val isMember = group.memberIds.contains(athlete.id)
                            Button(
                                onClick = { onToggleMember(athlete.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isMember) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    }
                                )
                            ) {
                                Icon(
                                    imageVector = if (isMember) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
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
}
