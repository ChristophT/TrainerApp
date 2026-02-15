package com.innotime.trainerapp.data.local.database.dao

import androidx.room.*
import com.innotime.trainerapp.data.local.entity.RunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Query("SELECT * FROM runs ORDER BY startedAt DESC")
    fun getAllRuns(): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE trainingId = :trainingId ORDER BY startedAt DESC")
    fun getRunsForTraining(trainingId: String): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE athleteId = :athleteId ORDER BY startedAt DESC")
    fun getRunsForAthlete(athleteId: String): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE id = :id")
    suspend fun getRunById(id: String): RunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: RunEntity)

    @Update
    suspend fun updateRun(run: RunEntity)

    @Query("DELETE FROM runs WHERE id = :id")
    suspend fun deleteRun(id: String)

    @Query("DELETE FROM runs WHERE trainingId = :trainingId")
    suspend fun deleteRunsForTraining(trainingId: String)
}
