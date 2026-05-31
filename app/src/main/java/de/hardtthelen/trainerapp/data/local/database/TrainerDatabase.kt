package de.hardtthelen.trainerapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.hardtthelen.trainerapp.data.local.database.dao.AthleteDao
import de.hardtthelen.trainerapp.data.local.database.dao.GroupDao
import de.hardtthelen.trainerapp.data.local.database.dao.RunDao
import de.hardtthelen.trainerapp.data.local.database.dao.TrainingDao
import de.hardtthelen.trainerapp.data.local.entity.AthleteEntity
import de.hardtthelen.trainerapp.data.local.entity.GroupMemberEntity
import de.hardtthelen.trainerapp.data.local.entity.RunEntity
import de.hardtthelen.trainerapp.data.local.entity.TrainingEntity
import de.hardtthelen.trainerapp.data.local.entity.TrainingGroupEntity
import de.hardtthelen.trainerapp.data.local.entity.TrainingParticipantEntity

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
@TypeConverters(Converters::class)
abstract class TrainerDatabase : RoomDatabase() {
    abstract fun athleteDao(): AthleteDao
    abstract fun runDao(): RunDao
    abstract fun trainingDao(): TrainingDao
    abstract fun groupDao(): GroupDao

    companion object {
        const val DATABASE_NAME = "trainer_database"
    }
}
