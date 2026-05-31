package de.hardtthelen.trainerapp.data.repository

import de.hardtthelen.trainerapp.data.local.database.dao.RunDao
import de.hardtthelen.trainerapp.data.mapper.toDomain
import de.hardtthelen.trainerapp.data.mapper.toEntity
import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.Run
import de.hardtthelen.trainerapp.domain.model.RunId
import de.hardtthelen.trainerapp.domain.model.TrainingId
import de.hardtthelen.trainerapp.domain.repository.RunRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RunRepositoryImpl @Inject constructor(
    private val runDao: RunDao
) : RunRepository {

    override fun getAllRuns(): Flow<List<Run>> {
        return runDao.getAllRuns().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRunsForTraining(trainingId: TrainingId): Flow<List<Run>> {
        return runDao.getRunsForTraining(trainingId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRunsForAthlete(athleteId: AthleteId): Flow<List<Run>> {
        return runDao.getRunsForAthlete(athleteId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRunById(id: RunId): Run? {
        return runDao.getRunById(id)?.toDomain()
    }

    override suspend fun addRun(run: Run) {
        runDao.insertRun(run.toEntity())
    }

    override suspend fun updateRun(run: Run) {
        runDao.updateRun(run.toEntity())
    }

    override suspend fun deleteRun(id: RunId) {
        runDao.deleteRun(id)
    }

    override suspend fun deleteRunsForTraining(trainingId: TrainingId) {
        runDao.deleteRunsForTraining(trainingId)
    }
}
