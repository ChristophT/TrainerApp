package de.hardtthelen.trainerapp.domain.repository

import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.Run
import de.hardtthelen.trainerapp.domain.model.RunId
import de.hardtthelen.trainerapp.domain.model.TrainingId
import kotlinx.coroutines.flow.Flow

interface RunRepository {
    fun getAllRuns(): Flow<List<Run>>
    fun getRunsForTraining(trainingId: TrainingId): Flow<List<Run>>
    fun getRunsForAthlete(athleteId: AthleteId): Flow<List<Run>>
    suspend fun getRunById(id: RunId): Run?
    suspend fun addRun(run: Run)
    suspend fun updateRun(run: Run)
    suspend fun deleteRun(id: RunId)
    suspend fun deleteRunsForTraining(trainingId: TrainingId)
}
