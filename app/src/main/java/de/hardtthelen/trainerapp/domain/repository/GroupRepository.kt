package de.hardtthelen.trainerapp.domain.repository

import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.TrainingGroup
import de.hardtthelen.trainerapp.domain.model.TrainingGroupId
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getAllGroups(): Flow<List<TrainingGroup>>
    suspend fun getGroupById(id: TrainingGroupId): TrainingGroup?
    suspend fun addGroup(group: TrainingGroup)
    suspend fun updateGroup(group: TrainingGroup)
    suspend fun deleteGroup(id: TrainingGroupId)
    suspend fun addMember(groupId: TrainingGroupId, athleteId: AthleteId)
    suspend fun removeMember(groupId: TrainingGroupId, athleteId: AthleteId)
}
