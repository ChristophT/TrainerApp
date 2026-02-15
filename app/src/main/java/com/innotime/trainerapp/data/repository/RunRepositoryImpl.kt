package com.innotime.trainerapp.data.repository

import com.innotime.trainerapp.data.local.database.dao.RunDao
import com.innotime.trainerapp.data.mapper.toDomain
import com.innotime.trainerapp.data.mapper.toEntity
import com.innotime.trainerapp.domain.model.Run
import com.innotime.trainerapp.domain.repository.RunRepository
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

    override fun getRunsForTraining(trainingId: String): Flow<List<Run>> {
        return runDao.getRunsForTraining(trainingId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRunsForAthlete(athleteId: String): Flow<List<Run>> {
        return runDao.getRunsForAthlete(athleteId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRunById(id: String): Run? {
        return runDao.getRunById(id)?.toDomain()
    }

    override suspend fun addRun(run: Run) {
        runDao.insertRun(run.toEntity())
    }

    override suspend fun updateRun(run: Run) {
        runDao.updateRun(run.toEntity())
    }

    override suspend fun deleteRun(id: String) {
        runDao.deleteRun(id)
    }

    override suspend fun deleteRunsForTraining(trainingId: String) {
        runDao.deleteRunsForTraining(trainingId)
    }
}
