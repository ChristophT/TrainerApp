package com.innotime.trainerapp.domain.repository

import com.innotime.trainerapp.domain.model.Training
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    fun getAllTrainings(): Flow<List<Training>>
    suspend fun getTrainingById(id: String): Training?
    suspend fun addTraining(training: Training)
    suspend fun updateTraining(training: Training)
    suspend fun deleteTraining(id: String)
    suspend fun addParticipant(trainingId: String, athleteId: String)
    suspend fun removeParticipant(trainingId: String, athleteId: String)
}
