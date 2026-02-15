package com.innotime.trainerapp.di

import com.innotime.trainerapp.data.repository.AthleteRepositoryImpl
import com.innotime.trainerapp.data.repository.GroupRepositoryImpl
import com.innotime.trainerapp.data.repository.RunRepositoryImpl
import com.innotime.trainerapp.data.repository.TrainingRepositoryImpl
import com.innotime.trainerapp.domain.repository.AthleteRepository
import com.innotime.trainerapp.domain.repository.GroupRepository
import com.innotime.trainerapp.domain.repository.RunRepository
import com.innotime.trainerapp.domain.repository.TrainingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAthleteRepository(
        athleteRepositoryImpl: AthleteRepositoryImpl
    ): AthleteRepository

    @Binds
    @Singleton
    abstract fun bindRunRepository(
        runRepositoryImpl: RunRepositoryImpl
    ): RunRepository

    @Binds
    @Singleton
    abstract fun bindTrainingRepository(
        trainingRepositoryImpl: TrainingRepositoryImpl
    ): TrainingRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(
        groupRepositoryImpl: GroupRepositoryImpl
    ): GroupRepository
}
