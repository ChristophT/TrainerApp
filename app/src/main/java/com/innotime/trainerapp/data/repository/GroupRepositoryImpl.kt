package com.innotime.trainerapp.data.repository

import com.innotime.trainerapp.data.local.database.dao.GroupDao
import com.innotime.trainerapp.data.local.entity.GroupMemberEntity
import com.innotime.trainerapp.data.mapper.toDomain
import com.innotime.trainerapp.data.mapper.toEntity
import com.innotime.trainerapp.domain.model.TrainingGroup
import com.innotime.trainerapp.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao
) : GroupRepository {

    override fun getAllGroups(): Flow<List<TrainingGroup>> {
        return groupDao.getAllGroups().flatMapLatest { entities ->
            if (entities.isEmpty()) {
                flowOf(emptyList())
            } else {
                // Combine group entities with their member IDs
                val memberFlows = entities.map { entity ->
                    groupDao.getMemberIds(entity.id).combine(flowOf(entity)) { memberIds, groupEntity ->
                        groupEntity.toDomain(memberIds)
                    }
                }

                // Combine all flows
                combine(memberFlows) { groups -> groups.toList() }
            }
        }
    }

    override suspend fun getGroupById(id: String): TrainingGroup? {
        val entity = groupDao.getGroupById(id) ?: return null
        // Note: This is a synchronous approach - simplified for now
        return entity.toDomain(emptyList())
    }

    override suspend fun addGroup(group: TrainingGroup) {
        groupDao.insertGroup(group.toEntity())

        // Insert members
        group.memberIds.forEach { athleteId ->
            groupDao.insertMember(GroupMemberEntity(group.id, athleteId))
        }
    }

    override suspend fun updateGroup(group: TrainingGroup) {
        groupDao.updateGroup(group.toEntity())
    }

    override suspend fun deleteGroup(id: String) {
        groupDao.deleteGroup(id)
    }

    override suspend fun addMember(groupId: String, athleteId: String) {
        groupDao.insertMember(GroupMemberEntity(groupId, athleteId))
    }

    override suspend fun removeMember(groupId: String, athleteId: String) {
        groupDao.deleteMember(groupId, athleteId)
    }
}
