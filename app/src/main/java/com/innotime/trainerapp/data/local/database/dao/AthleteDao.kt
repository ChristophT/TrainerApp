package com.innotime.trainerapp.data.local.database.dao

import androidx.room.*
import com.innotime.trainerapp.data.local.entity.AthleteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AthleteDao {
    @Query("SELECT * FROM athletes ORDER BY name ASC")
    fun getAllAthletes(): Flow<List<AthleteEntity>>

    @Query("SELECT * FROM athletes WHERE id = :id")
    suspend fun getAthleteById(id: String): AthleteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAthlete(athlete: AthleteEntity)

    @Update
    suspend fun updateAthlete(athlete: AthleteEntity)

    @Query("DELETE FROM athletes WHERE id = :id")
    suspend fun deleteAthlete(id: String)
}
