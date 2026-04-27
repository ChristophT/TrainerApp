package com.innotime.trainerapp.data.local.database.dao

import androidx.room.*
import com.innotime.trainerapp.data.local.entity.TrainingEntity
import com.innotime.trainerapp.data.local.entity.TrainingParticipantEntity
import com.innotime.trainerapp.domain.model.AthleteId
import com.innotime.trainerapp.domain.model.TrainingId
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {
    @Query("SELECT * FROM trainings ORDER BY date DESC")
    fun getAllTrainings(): Flow<List<TrainingEntity>>

    @Query("SELECT * FROM trainings WHERE id = :id")
    suspend fun getTrainingById(id: TrainingId): TrainingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraining(training: TrainingEntity)

    @Update
    suspend fun updateTraining(training: TrainingEntity)

    @Query("DELETE FROM trainings WHERE id = :id")
    suspend fun deleteTraining(id: TrainingId)

    @Query("SELECT athleteId FROM training_participants WHERE trainingId = :trainingId")
    fun getParticipantIds(trainingId: TrainingId): Flow<List<AthleteId>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertParticipant(participant: TrainingParticipantEntity)

    @Query("DELETE FROM training_participants WHERE trainingId = :trainingId AND athleteId = :athleteId")
    suspend fun deleteParticipant(trainingId: TrainingId, athleteId: AthleteId)
}
