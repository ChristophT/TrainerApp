package de.hardtthelen.trainerapp.domain.repository

import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.Training
import de.hardtthelen.trainerapp.domain.model.TrainingId
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
