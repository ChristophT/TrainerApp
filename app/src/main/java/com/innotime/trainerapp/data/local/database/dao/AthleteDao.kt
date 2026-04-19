package com.innotime.trainerapp.data.local.database.dao

import androidx.room.*
import com.innotime.trainerapp.data.local.entity.AthleteEntity
import com.innotime.trainerapp.domain.model.AthleteId
import kotlinx.coroutines.flow.Flow

@Dao
interface AthleteDao {
    @Query("SELECT * FROM athletes ORDER BY name ASC")
    fun getAllAthletes(): Flow<List<AthleteEntity>>

    @Query("SELECT * FROM athletes WHERE id = :id")
    suspend fun getAthleteById(id: AthleteId): AthleteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAthlete(athlete: AthleteEntity)

    @Update
    suspend fun updateAthlete(athlete: AthleteEntity)

    @Query("DELETE FROM athletes WHERE id = :id")
    suspend fun deleteAthlete(id: AthleteId)
}
