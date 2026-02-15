package com.innotime.trainerapp.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.innotime.trainerapp.data.local.database.TrainerDatabase
import com.innotime.trainerapp.domain.model.Athlete
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for AthleteRepositoryImpl with Room database.
 *
 * These tests use an in-memory database that is created and destroyed for each test.
 */
class AthleteRepositoryImplTest {

    private lateinit var database: TrainerDatabase
    private lateinit var repository: AthleteRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            TrainerDatabase::class.java
        )
            .allowMainThreadQueries() // Only for testing
            .build()

        // Create repository with real DAO
        repository = AthleteRepositoryImpl(database.athleteDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getAllAthletes_initiallyEmpty() = runTest {
        repository.getAllAthletes().test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun addAthlete_athleteIsRetrievable() = runTest {
        val athlete = Athlete(id = "1", name = "John Doe")

        repository.addAthlete(athlete)

        repository.getAllAthletes().test {
            val athletes = awaitItem()
            assertThat(athletes).hasSize(1)
            assertThat(athletes[0]).isEqualTo(athlete)
        }
    }

    @Test
    fun addAthlete_replacesExistingAthleteWithSameId() = runTest {
        val athlete1 = Athlete(id = "1", name = "John Doe")
        val athlete2 = Athlete(id = "1", name = "Jane Doe")

        repository.addAthlete(athlete1)
        repository.addAthlete(athlete2)

        repository.getAllAthletes().test {
            val athletes = awaitItem()
            assertThat(athletes).hasSize(1)
            assertThat(athletes[0].name).isEqualTo("Jane Doe")
        }
    }

    @Test
    fun getAthleteById_returnsCorrectAthlete() = runTest {
        val athlete1 = Athlete(id = "1", name = "John Doe")
        val athlete2 = Athlete(id = "2", name = "Jane Smith")

        repository.addAthlete(athlete1)
        repository.addAthlete(athlete2)

        val retrieved = repository.getAthleteById("2")

        assertThat(retrieved).isEqualTo(athlete2)
    }

    @Test
    fun getAthleteById_returnsNullWhenNotFound() = runTest {
        val retrieved = repository.getAthleteById("nonexistent")

        assertThat(retrieved).isNull()
    }

    @Test
    fun updateAthlete_updatesExistingAthlete() = runTest {
        val athlete = Athlete(id = "1", name = "John Doe")
        repository.addAthlete(athlete)

        val updated = athlete.copy(name = "John Updated")
        repository.updateAthlete(updated)

        repository.getAllAthletes().test {
            val athletes = awaitItem()
            assertThat(athletes[0].name).isEqualTo("John Updated")
        }
    }

    @Test
    fun deleteAthlete_removesAthlete() = runTest {
        val athlete = Athlete(id = "1", name = "John Doe")
        repository.addAthlete(athlete)

        repository.deleteAthlete("1")

        repository.getAllAthletes().test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun deleteAthlete_doesNotAffectOtherAthletes() = runTest {
        val athlete1 = Athlete(id = "1", name = "John Doe")
        val athlete2 = Athlete(id = "2", name = "Jane Smith")

        repository.addAthlete(athlete1)
        repository.addAthlete(athlete2)

        repository.deleteAthlete("1")

        repository.getAllAthletes().test {
            val athletes = awaitItem()
            assertThat(athletes).hasSize(1)
            assertThat(athletes[0]).isEqualTo(athlete2)
        }
    }

    @Test
    fun getAllAthletes_returnsSortedByName() = runTest {
        val athletes = listOf(
            Athlete(id = "1", name = "Charlie"),
            Athlete(id = "2", name = "Alice"),
            Athlete(id = "3", name = "Bob")
        )

        athletes.forEach { repository.addAthlete(it) }

        repository.getAllAthletes().test {
            val retrieved = awaitItem()
            assertThat(retrieved.map { it.name }).containsExactly("Alice", "Bob", "Charlie").inOrder()
        }
    }

    @Test
    fun addMultipleAthletes_allRetrievable() = runTest {
        val athletes = (1..10).map { Athlete(id = "$it", name = "Athlete $it") }

        athletes.forEach { repository.addAthlete(it) }

        repository.getAllAthletes().test {
            assertThat(awaitItem()).hasSize(10)
        }
    }

    @Test
    fun dataPersistedAcrossFlowCollections() = runTest {
        val athlete = Athlete(id = "1", name = "John Doe")
        repository.addAthlete(athlete)

        // First collection
        repository.getAllAthletes().test {
            assertThat(awaitItem()).hasSize(1)
        }

        // Second collection (data should still be there)
        repository.getAllAthletes().test {
            assertThat(awaitItem()).hasSize(1)
        }
    }
}
