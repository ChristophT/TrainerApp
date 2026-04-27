package com.innotime.trainerapp.domain.repository

import com.innotime.trainerapp.domain.model.AthleteId
import com.innotime.trainerapp.domain.model.Run
import com.innotime.trainerapp.domain.model.RunId
import kotlinx.coroutines.flow.Flow

interface RunRepository {
    fun getAllRuns(): Flow<List<Run>>
    fun getRunsForTraining(trainingId: String): Flow<List<Run>>
    fun getRunsForAthlete(athleteId: AthleteId): Flow<List<Run>>
    suspend fun getRunById(id: RunId): Run?
    suspend fun addRun(run: Run)
    suspend fun updateRun(run: Run)
    suspend fun deleteRun(id: RunId)
    suspend fun deleteRunsForTraining(trainingId: String)
}
