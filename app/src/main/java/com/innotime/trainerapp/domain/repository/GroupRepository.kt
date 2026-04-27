package com.innotime.trainerapp.domain.repository

import com.innotime.trainerapp.domain.model.AthleteId
import com.innotime.trainerapp.domain.model.TrainingGroup
import com.innotime.trainerapp.domain.model.TrainingGroupId
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
