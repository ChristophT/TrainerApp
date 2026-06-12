package de.hardtthelen.trainerapp.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import de.hardtthelen.trainerapp.data.local.database.TrainerDatabase
import de.hardtthelen.trainerapp.domain.model.Training
import de.hardtthelen.trainerapp.domain.model.TrainingId
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class TrainingRepositoryImplTest {

    private lateinit var database: TrainerDatabase
    private lateinit var repository: TrainingRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            TrainerDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = TrainingRepositoryImpl(database.trainingDao(), database.runDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun deleteTraining_removesTraining() = runTest {
        val training = Training(
            id = TrainingId("1"),
            date = System.currentTimeMillis(),
            description = "Test Session",
            participantIds = emptyList(),
            runIds = emptyList()
        )
        repository.addTraining(training)

        repository.deleteTraining(TrainingId("1"))

        repository.getAllTrainings().test {
            assertThat(awaitItem()).isEmpty()
        }
    }
}
