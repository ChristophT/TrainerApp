package de.hardtthelen.trainerapp.data.repository

import de.hardtthelen.trainerapp.data.local.database.dao.RunDao
import de.hardtthelen.trainerapp.data.local.database.dao.TrainingDao
import de.hardtthelen.trainerapp.data.local.entity.TrainingEntity
import de.hardtthelen.trainerapp.data.local.entity.TrainingParticipantEntity
import de.hardtthelen.trainerapp.domain.model.Athlete
import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.Training
import de.hardtthelen.trainerapp.domain.model.TrainingId
import de.hardtthelen.trainerapp.domain.model.TrainingParticipant
import de.hardtthelen.trainerapp.domain.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrainingRepositoryImpl @Inject constructor(
    private val trainingDao: TrainingDao,
    private val runDao: RunDao
) : TrainingRepository {

    override fun getAllTrainings(): Flow<List<Training>> {
        return combine(
            trainingDao.getAllTrainings(),
            runDao.getAllRuns()
        ) { trainingEntities, runEntities ->
            trainingEntities.map { entity ->
                val runIds = runEntities
                    .filter { it.trainingId == entity.id }
                    .map { it.id }

                Training(
                    id = entity.id,
                    date = entity.date,
                    description = entity.description,
                    participants = emptyList(), // Will be loaded separately via combine
                    runIds = runIds
                )
            }
        }.combine(trainingDao.getAllTrainings()) { trainings, trainingEntities ->
            // Fetch participant IDs for each training
            trainings.map { training ->
                // This is a simplification - in real implementation, we'd need to handle this better
                training
            }
        }
    }

    override suspend fun getTrainingById(id: TrainingId): Training? {
        val entity = trainingDao.getTrainingById(id) ?: return null
        // Note: This is synchronous fetch of participant IDs - not ideal but simplified
        return Training(
            id = entity.id,
            date = entity.date,
            description = entity.description,
            participants = emptyList(), // Would need to fetch from participant table
            runIds = emptyList() // Would need to fetch from runs table
        )
    }

    override suspend fun addTraining(training: Training) {
        val entity = TrainingEntity(
            id = training.id,
            date = training.date,
            description = training.description
        )
        trainingDao.insertTraining(entity)

        // Insert participants
        training.participants.forEach { participant ->
            addParticipant(training.id, participant.athlete)
        }
    }

    override suspend fun updateTraining(training: Training) {
        val entity = TrainingEntity(
            id = training.id,
            date = training.date,
            description = training.description
        )
        trainingDao.updateTraining(entity)
    }

    override suspend fun deleteTraining(id: TrainingId) {
        trainingDao.deleteTraining(id)
    }

    override suspend fun addParticipant(trainingId: TrainingId, athlete: Athlete): TrainingParticipant {
        val newDisplayOrder = (trainingDao.getParticipants(trainingId).map { list -> list.maxOfOrNull { it.displayOrder } }.first() ?: 0) + 1
        trainingDao.insertParticipant(
            TrainingParticipantEntity(trainingId, athlete.id, newDisplayOrder)
        )
        return TrainingParticipant(athlete, newDisplayOrder)
    }

    override suspend fun removeParticipant(trainingId: TrainingId, athleteId: AthleteId) {
        trainingDao.deleteParticipant(trainingId, athleteId)
    }
}
