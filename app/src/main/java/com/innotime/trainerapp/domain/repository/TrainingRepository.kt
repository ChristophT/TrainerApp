package com.innotime.trainerapp.domain.repository

import com.innotime.trainerapp.domain.model.AthleteId
import com.innotime.trainerapp.domain.model.Training
import com.innotime.trainerapp.domain.model.TrainingId
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    fun getAllTrainings(): Flow<List<Training>>
    suspend fun getTrainingById(id: TrainingId): Training?
    suspend fun addTraining(training: Training)
    suspend fun updateTraining(training: Training)
    suspend fun deleteTraining(id: TrainingId)
    suspend fun addParticipant(trainingId: TrainingId, athleteId: AthleteId)
    suspend fun removeParticipant(trainingId: TrainingId, athleteId: AthleteId)
}
