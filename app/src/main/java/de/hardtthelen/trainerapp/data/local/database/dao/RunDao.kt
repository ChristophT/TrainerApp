package de.hardtthelen.trainerapp.data.local.database.dao

import androidx.room.*
import de.hardtthelen.trainerapp.data.local.entity.RunEntity
import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.RunId
import de.hardtthelen.trainerapp.domain.model.TrainingId
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Query("SELECT * FROM runs ORDER BY startedAt DESC")
    fun getAllRuns(): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE trainingId = :trainingId ORDER BY startedAt DESC")
    fun getRunsForTraining(trainingId: TrainingId): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE athleteId = :athleteId ORDER BY startedAt DESC")
    fun getRunsForAthlete(athleteId: AthleteId): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE id = :id")
    suspend fun getRunById(id: RunId): RunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: RunEntity)

    @Update
    suspend fun updateRun(run: RunEntity)

    @Query("DELETE FROM runs WHERE id = :id")
    suspend fun deleteRun(id: RunId)

    @Query("DELETE FROM runs WHERE trainingId = :trainingId")
    suspend fun deleteRunsForTraining(trainingId: TrainingId)
}
