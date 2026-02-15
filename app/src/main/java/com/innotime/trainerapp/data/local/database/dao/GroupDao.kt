package com.innotime.trainerapp.data.local.database.dao

import androidx.room.*
import com.innotime.trainerapp.data.local.entity.TrainingGroupEntity
import com.innotime.trainerapp.data.local.entity.GroupMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM training_groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<TrainingGroupEntity>>

    @Query("SELECT * FROM training_groups WHERE id = :id")
    suspend fun getGroupById(id: String): TrainingGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: TrainingGroupEntity)

    @Update
    suspend fun updateGroup(group: TrainingGroupEntity)

    @Query("DELETE FROM training_groups WHERE id = :id")
    suspend fun deleteGroup(id: String)

    @Query("SELECT athleteId FROM group_members WHERE groupId = :groupId")
    fun getMemberIds(groupId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMember(member: GroupMemberEntity)

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND athleteId = :athleteId")
    suspend fun deleteMember(groupId: String, athleteId: String)
}
