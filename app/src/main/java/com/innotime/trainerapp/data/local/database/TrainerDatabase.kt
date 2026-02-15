package com.innotime.trainerapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.innotime.trainerapp.data.local.database.dao.AthleteDao
import com.innotime.trainerapp.data.local.database.dao.GroupDao
import com.innotime.trainerapp.data.local.database.dao.RunDao
import com.innotime.trainerapp.data.local.database.dao.TrainingDao
import com.innotime.trainerapp.data.local.entity.AthleteEntity
import com.innotime.trainerapp.data.local.entity.GroupMemberEntity
import com.innotime.trainerapp.data.local.entity.RunEntity
import com.innotime.trainerapp.data.local.entity.TrainingEntity
import com.innotime.trainerapp.data.local.entity.TrainingGroupEntity
import com.innotime.trainerapp.data.local.entity.TrainingParticipantEntity

@Database(
    entities = [
        AthleteEntity::class,
        RunEntity::class,
        TrainingEntity::class,
        TrainingParticipantEntity::class,
        TrainingGroupEntity::class,
        GroupMemberEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class TrainerDatabase : RoomDatabase() {
    abstract fun athleteDao(): AthleteDao
    abstract fun runDao(): RunDao
    abstract fun trainingDao(): TrainingDao
    abstract fun groupDao(): GroupDao

    companion object {
        const val DATABASE_NAME = "trainer_database"
    }
}
