package de.hardtthelen.trainerapp.data.repository

import de.hardtthelen.trainerapp.data.local.database.dao.AthleteDao
import de.hardtthelen.trainerapp.data.mapper.toDomain
import de.hardtthelen.trainerapp.data.mapper.toEntity
import de.hardtthelen.trainerapp.domain.model.Athlete
import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.repository.AthleteRepository
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

    override suspend fun getAthleteById(id: AthleteId): Athlete? {
        return athleteDao.getAthleteById(id)?.toDomain()
    }

    override suspend fun addAthlete(athlete: Athlete) {
        athleteDao.insertAthlete(athlete.toEntity())
    }

    override suspend fun updateAthlete(athlete: Athlete) {
        athleteDao.updateAthlete(athlete.toEntity())
    }

    override suspend fun deleteAthlete(id: AthleteId) {
        athleteDao.deleteAthlete(id)
    }
}
