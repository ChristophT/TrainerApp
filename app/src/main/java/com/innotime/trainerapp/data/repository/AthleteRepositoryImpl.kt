package com.innotime.trainerapp.data.repository

import com.innotime.trainerapp.data.local.database.dao.AthleteDao
import com.innotime.trainerapp.data.mapper.toDomain
import com.innotime.trainerapp.data.mapper.toEntity
import com.innotime.trainerapp.domain.model.Athlete
import com.innotime.trainerapp.domain.repository.AthleteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AthleteRepositoryImpl @Inject constructor(
    private val athleteDao: AthleteDao
) : AthleteRepository {

    override fun getAllAthletes(): Flow<List<Athlete>> {
        return athleteDao.getAllAthletes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAthleteById(id: String): Athlete? {
        return athleteDao.getAthleteById(id)?.toDomain()
    }

    override suspend fun addAthlete(athlete: Athlete) {
        athleteDao.insertAthlete(athlete.toEntity())
    }

    override suspend fun updateAthlete(athlete: Athlete) {
        athleteDao.updateAthlete(athlete.toEntity())
    }

    override suspend fun deleteAthlete(id: String) {
        athleteDao.deleteAthlete(id)
    }
}
