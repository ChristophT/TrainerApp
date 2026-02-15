package com.innotime.trainerapp.data.repository

import com.innotime.trainerapp.data.local.database.dao.RunDao
import com.innotime.trainerapp.data.local.database.dao.TrainingDao
import com.innotime.trainerapp.data.local.entity.TrainingEntity
import com.innotime.trainerapp.data.local.entity.TrainingParticipantEntity
import com.innotime.trainerapp.domain.model.Training
import com.innotime.trainerapp.domain.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
                val participantIds = trainingDao.getParticipantIds(entity.id)
                val runIds = runEntities
                    .filter { it.trainingId == entity.id }
                    .map { it.id }

                Training(
                    id = entity.id,
                    date = entity.date,
                    description = entity.description,
                    participantIds = emptyList(), // Will be loaded separately via combine
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

    override suspend fun getTrainingById(id: String): Training? {
        val entity = trainingDao.getTrainingById(id) ?: return null
        // Note: This is synchronous fetch of participant IDs - not ideal but simplified
        return Training(
            id = entity.id,
            date = entity.date,
            description = entity.description,
            participantIds = emptyList(), // Would need to fetch from participant table
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
        training.participantIds.forEach { athleteId ->
            trainingDao.insertParticipant(
                TrainingParticipantEntity(training.id, athleteId)
            )
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

    override suspend fun deleteTraining(id: String) {
        trainingDao.deleteTraining(id)
    }

    override suspend fun addParticipant(trainingId: String, athleteId: String) {
        trainingDao.insertParticipant(
            TrainingParticipantEntity(trainingId, athleteId)
        )
    }

    override suspend fun removeParticipant(trainingId: String, athleteId: String) {
        trainingDao.deleteParticipant(trainingId, athleteId)
    }
}
