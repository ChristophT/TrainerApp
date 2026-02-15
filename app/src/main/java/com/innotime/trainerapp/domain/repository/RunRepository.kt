package com.innotime.trainerapp.domain.repository

import com.innotime.trainerapp.domain.model.Run
import kotlinx.coroutines.flow.Flow

interface RunRepository {
    fun getAllRuns(): Flow<List<Run>>
    fun getRunsForTraining(trainingId: String): Flow<List<Run>>
    fun getRunsForAthlete(athleteId: String): Flow<List<Run>>
    suspend fun getRunById(id: String): Run?
    suspend fun addRun(run: Run)
    suspend fun updateRun(run: Run)
    suspend fun deleteRun(id: String)
    suspend fun deleteRunsForTraining(trainingId: String)
}
