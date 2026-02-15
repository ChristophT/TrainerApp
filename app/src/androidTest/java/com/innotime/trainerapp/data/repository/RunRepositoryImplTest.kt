package com.innotime.trainerapp.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.innotime.trainerapp.data.local.database.TrainerDatabase
import com.innotime.trainerapp.data.local.entity.AthleteEntity
import com.innotime.trainerapp.data.local.entity.TrainingEntity
import com.innotime.trainerapp.domain.model.Run
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for RunRepositoryImpl with Room database.
 *
 * Tests demonstrate:
 * - CRUD operations for runs
 * - Foreign key relationships
 * - Cascade delete behavior
 * - Filtering by athlete and training
 */
class RunRepositoryImplTest {

    private lateinit var database: TrainerDatabase
    private lateinit var repository: RunRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            TrainerDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = RunRepositoryImpl(database.runDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getAllRuns_initiallyEmpty() = runTest {
        repository.getAllRuns().test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun addRun_runIsRetrievable() = runTest {
        // Setup: Create required foreign key entities
        val athlete = AthleteEntity(id = "athlete-1", name = "John Doe")
        val training = TrainingEntity(id = "training-1", date = System.currentTimeMillis(), description = "Test")
        database.athleteDao().insertAthlete(athlete)
        database.trainingDao().insertTraining(training)

        val run = Run(
            id = "run-1",
            athleteId = "athlete-1",
            trainingId = "training-1",
            startedAt = 1000L,
            finishedAt = 2000L,
            durationMs = 1000L,
            note = "Great run!"
        )

        repository.addRun(run)

        repository.getAllRuns().test {
            val runs = awaitItem()
            assertThat(runs).hasSize(1)
            assertThat(runs[0]).isEqualTo(run)
        }
    }

    @Test
    fun getRunsForTraining_filtersCorrectly() = runTest {
        // Setup entities
        val athlete = AthleteEntity(id = "athlete-1", name = "John")
        val training1 = TrainingEntity(id = "training-1", date = 1000L, description = "Morning")
        val training2 = TrainingEntity(id = "training-2", date = 2000L, description = "Evening")
        database.athleteDao().insertAthlete(athlete)
        database.trainingDao().insertTraining(training1)
        database.trainingDao().insertTraining(training2)

        // Add runs for different trainings
        val run1 = Run("run-1", "athlete-1", "training-1", 1000L, 2000L, 1000L, "")
        val run2 = Run("run-2", "athlete-1", "training-1", 2000L, 3000L, 1000L, "")
        val run3 = Run("run-3", "athlete-1", "training-2", 3000L, 4000L, 1000L, "")

        repository.addRun(run1)
        repository.addRun(run2)
        repository.addRun(run3)

        // Test filtering
        repository.getRunsForTraining("training-1").test {
            val runs = awaitItem()
            assertThat(runs).hasSize(2)
            assertThat(runs.map { it.id }).containsExactly("run-1", "run-2")
        }
    }

    @Test
    fun getRunsForAthlete_filtersCorrectly() = runTest {
        // Setup entities
        val athlete1 = AthleteEntity(id = "athlete-1", name = "John")
        val athlete2 = AthleteEntity(id = "athlete-2", name = "Jane")
        val training = TrainingEntity(id = "training-1", date = 1000L, description = "Test")
        database.athleteDao().insertAthlete(athlete1)
        database.athleteDao().insertAthlete(athlete2)
        database.trainingDao().insertTraining(training)

        // Add runs for different athletes
        val run1 = Run("run-1", "athlete-1", "training-1", 1000L, 2000L, 1000L, "")
        val run2 = Run("run-2", "athlete-2", "training-1", 2000L, 3000L, 1000L, "")
        val run3 = Run("run-3", "athlete-1", "training-1", 3000L, 4000L, 1000L, "")

        repository.addRun(run1)
        repository.addRun(run2)
        repository.addRun(run3)

        // Test filtering
        repository.getRunsForAthlete("athlete-1").test {
            val runs = awaitItem()
            assertThat(runs).hasSize(2)
            assertThat(runs.map { it.id }).containsExactly("run-1", "run-3")
        }
    }

    @Test
    fun updateRun_updatesExistingRun() = runTest {
        // Setup
        val athlete = AthleteEntity(id = "athlete-1", name = "John")
        val training = TrainingEntity(id = "training-1", date = 1000L, description = "Test")
        database.athleteDao().insertAthlete(athlete)
        database.trainingDao().insertTraining(training)

        val run = Run("run-1", "athlete-1", "training-1", 1000L, 2000L, 1000L, "Original note")
        repository.addRun(run)

        // Update
        val updated = run.copy(note = "Updated note")
        repository.updateRun(updated)

        // Verify
        repository.getAllRuns().test {
            val runs = awaitItem()
            assertThat(runs[0].note).isEqualTo("Updated note")
        }
    }

    @Test
    fun deleteRun_removesRun() = runTest {
        // Setup
        val athlete = AthleteEntity(id = "athlete-1", name = "John")
        val training = TrainingEntity(id = "training-1", date = 1000L, description = "Test")
        database.athleteDao().insertAthlete(athlete)
        database.trainingDao().insertTraining(training)

        val run = Run("run-1", "athlete-1", "training-1", 1000L, 2000L, 1000L, "")
        repository.addRun(run)

        // Delete
        repository.deleteRun("run-1")

        // Verify
        repository.getAllRuns().test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun deleteRunsForTraining_removesAllRunsForTraining() = runTest {
        // Setup
        val athlete = AthleteEntity(id = "athlete-1", name = "John")
        val training1 = TrainingEntity(id = "training-1", date = 1000L, description = "Morning")
        val training2 = TrainingEntity(id = "training-2", date = 2000L, description = "Evening")
        database.athleteDao().insertAthlete(athlete)
        database.trainingDao().insertTraining(training1)
        database.trainingDao().insertTraining(training2)

        val run1 = Run("run-1", "athlete-1", "training-1", 1000L, 2000L, 1000L, "")
        val run2 = Run("run-2", "athlete-1", "training-1", 2000L, 3000L, 1000L, "")
        val run3 = Run("run-3", "athlete-1", "training-2", 3000L, 4000L, 1000L, "")

        repository.addRun(run1)
        repository.addRun(run2)
        repository.addRun(run3)

        // Delete all runs for training-1
        repository.deleteRunsForTraining("training-1")

        // Verify only run3 remains
        repository.getAllRuns().test {
            val runs = awaitItem()
            assertThat(runs).hasSize(1)
            assertThat(runs[0].id).isEqualTo("run-3")
        }
    }

    @Test
    fun cascadeDelete_athleteDeleteRemovesRuns() = runTest {
        // Setup
        val athlete = AthleteEntity(id = "athlete-1", name = "John")
        val training = TrainingEntity(id = "training-1", date = 1000L, description = "Test")
        database.athleteDao().insertAthlete(athlete)
        database.trainingDao().insertTraining(training)

        val run = Run("run-1", "athlete-1", "training-1", 1000L, 2000L, 1000L, "")
        repository.addRun(run)

        // Delete athlete (should cascade to runs)
        database.athleteDao().deleteAthlete("athlete-1")

        // Verify run was deleted
        repository.getAllRuns().test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun cascadeDelete_trainingDeleteRemovesRuns() = runTest {
        // Setup
        val athlete = AthleteEntity(id = "athlete-1", name = "John")
        val training = TrainingEntity(id = "training-1", date = 1000L, description = "Test")
        database.athleteDao().insertAthlete(athlete)
        database.trainingDao().insertTraining(training)

        val run = Run("run-1", "athlete-1", "training-1", 1000L, 2000L, 1000L, "")
        repository.addRun(run)

        // Delete training (should cascade to runs)
        database.trainingDao().deleteTraining("training-1")

        // Verify run was deleted
        repository.getAllRuns().test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun getAllRuns_sortedByNewestFirst() = runTest {
        // Setup
        val athlete = AthleteEntity(id = "athlete-1", name = "John")
        val training = TrainingEntity(id = "training-1", date = 1000L, description = "Test")
        database.athleteDao().insertAthlete(athlete)
        database.trainingDao().insertTraining(training)

        // Add runs with different start times
        val run1 = Run("run-1", "athlete-1", "training-1", 1000L, 2000L, 1000L, "")
        val run2 = Run("run-2", "athlete-1", "training-1", 3000L, 4000L, 1000L, "")
        val run3 = Run("run-3", "athlete-1", "training-1", 2000L, 3000L, 1000L, "")

        repository.addRun(run1)
        repository.addRun(run2)
        repository.addRun(run3)

        // Verify sorted descending by startedAt
        repository.getAllRuns().test {
            val runs = awaitItem()
            assertThat(runs.map { it.startedAt }).containsExactly(3000L, 2000L, 1000L).inOrder()
        }
    }

    @Test
    fun runWithNullDuration_canBeStored() = runTest {
        // Setup
        val athlete = AthleteEntity(id = "athlete-1", name = "John")
        val training = TrainingEntity(id = "training-1", date = 1000L, description = "Test")
        database.athleteDao().insertAthlete(athlete)
        database.trainingDao().insertTraining(training)

        // Add run with null duration (not yet completed)
        val run = Run("run-1", "athlete-1", "training-1", 1000L, null, null, "")
        repository.addRun(run)

        // Verify
        repository.getAllRuns().test {
            val runs = awaitItem()
            assertThat(runs[0].durationMs).isNull()
            assertThat(runs[0].finishedAt).isNull()
        }
    }
}
