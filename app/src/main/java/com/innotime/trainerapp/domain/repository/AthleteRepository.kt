package com.innotime.trainerapp.domain.repository

import com.innotime.trainerapp.domain.model.Athlete
import kotlinx.coroutines.flow.Flow

interface AthleteRepository {
    fun getAllAthletes(): Flow<List<Athlete>>
    suspend fun getAthleteById(id: String): Athlete?
    suspend fun addAthlete(athlete: Athlete)
    suspend fun updateAthlete(athlete: Athlete)
    suspend fun deleteAthlete(id: String)
}
