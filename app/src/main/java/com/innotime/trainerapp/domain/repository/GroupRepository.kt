package com.innotime.trainerapp.domain.repository

import com.innotime.trainerapp.domain.model.TrainingGroup
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getAllGroups(): Flow<List<TrainingGroup>>
    suspend fun getGroupById(id: String): TrainingGroup?
    suspend fun addGroup(group: TrainingGroup)
    suspend fun updateGroup(group: TrainingGroup)
    suspend fun deleteGroup(id: String)
    suspend fun addMember(groupId: String, athleteId: String)
    suspend fun removeMember(groupId: String, athleteId: String)
}
