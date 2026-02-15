package com.innotime.trainerapp.di

import android.content.Context
import androidx.room.Room
import com.innotime.trainerapp.data.local.database.TrainerDatabase
import com.innotime.trainerapp.data.local.database.dao.AthleteDao
import com.innotime.trainerapp.data.local.database.dao.GroupDao
import com.innotime.trainerapp.data.local.database.dao.RunDao
import com.innotime.trainerapp.data.local.database.dao.TrainingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTrainerDatabase(
        @ApplicationContext context: Context
    ): TrainerDatabase {
        return Room.databaseBuilder(
            context,
            TrainerDatabase::class.java,
            TrainerDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAthleteDao(database: TrainerDatabase): AthleteDao {
        return database.athleteDao()
    }

    @Provides
    @Singleton
    fun provideRunDao(database: TrainerDatabase): RunDao {
        return database.runDao()
    }

    @Provides
    @Singleton
    fun provideTrainingDao(database: TrainerDatabase): TrainingDao {
        return database.trainingDao()
    }

    @Provides
    @Singleton
    fun provideGroupDao(database: TrainerDatabase): GroupDao {
        return database.groupDao()
    }
}
