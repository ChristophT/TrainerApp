package com.innotime.trainerapp.presentation.screen.training

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.innotime.trainerapp.domain.model.Athlete
import com.innotime.trainerapp.domain.model.Training
import com.innotime.trainerapp.domain.model.TrainingGroup
import com.innotime.trainerapp.domain.repository.AthleteRepository
import com.innotime.trainerapp.domain.repository.GroupRepository
import com.innotime.trainerapp.domain.repository.RunRepository
import com.innotime.trainerapp.domain.repository.TrainingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TrainingViewModel.
 *
 * These tests use MockK for mocking repositories and Turbine for testing Flows.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TrainingViewModelTest {

    private lateinit var viewModel: TrainingViewModel
    private lateinit var athleteRepository: AthleteRepository
    private lateinit var runRepository: RunRepository
    private lateinit var trainingRepository: TrainingRepository
    private lateinit var groupRepository: GroupRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Create mock repositories
        athleteRepository = mockk(relaxed = true)
        runRepository = mockk(relaxed = true)
        trainingRepository = mockk(relaxed = true)
        groupRepository = mockk(relaxed = true)

        // Setup default repository behaviors
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(emptyList())
        coEvery { runRepository.getAllRuns() } returns flowOf(emptyList())
        coEvery { trainingRepository.getAllTrainings() } returns flowOf(emptyList())
        coEvery { groupRepository.getAllGroups() } returns flowOf(emptyList())

        // Create ViewModel
        viewModel = TrainingViewModel(
            athleteRepository = athleteRepository,
            runRepository = runRepository,
            trainingRepository = trainingRepository,
            groupRepository = groupRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no current training`() = runTest {
        viewModel.currentTraining.test {
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun `initial state has empty active runs`() = runTest {
        viewModel.activeRuns.test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `addAthlete calls repository with correct data`() = runTest {
        val athleteName = "John Doe"

        viewModel.addAthlete(athleteName)
        advanceUntilIdle()

        coVerify {
            athleteRepository.addAthlete(
                match { athlete ->
                    athlete.name == athleteName &&
                    athlete.id.isNotEmpty()
                }
            )
        }
    }

    @Test
    fun `startTraining creates new training session`() = runTest {
        val description = "Morning Training"

        viewModel.startTraining(description)
        advanceUntilIdle()

        viewModel.currentTraining.test {
            val training = awaitItem()
            assertThat(training).isNotNull()
            assertThat(training?.description).isEqualTo(description)
            assertThat(training?.participantIds).isEmpty()
            assertThat(training?.runIds).isEmpty()
        }

        coVerify {
            trainingRepository.addTraining(
                match { it.description == description }
            )
        }
    }

    @Test
    fun `endTraining clears current training`() = runTest {
        // Start a training session first
        viewModel.startTraining("Test Session")
        advanceUntilIdle()

        // Verify training is active
        assertThat(viewModel.currentTraining.value).isNotNull()

        // End the training
        viewModel.endTraining()
        advanceUntilIdle()

        // Verify training is cleared
        viewModel.currentTraining.test {
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun `addParticipant adds athlete to current training`() = runTest {
        // Setup: Start a training session
        viewModel.startTraining("Test Session")
        advanceUntilIdle()

        val trainingId = viewModel.currentTraining.value?.id
        assertThat(trainingId).isNotNull()

        val athleteId = "athlete-123"

        // Add participant
        viewModel.addParticipant(athleteId)
        advanceUntilIdle()

        // Verify repository was called
        coVerify {
            trainingRepository.addParticipant(trainingId!!, athleteId)
        }

        // Verify state updated
        viewModel.currentTraining.test {
            val training = awaitItem()
            assertThat(training?.participantIds).contains(athleteId)
        }
    }

    @Test
    fun `startRun creates active run for athlete`() = runTest {
        // Setup: Start training and add participant
        viewModel.startTraining("Test Session")
        advanceUntilIdle()

        val athleteId = "athlete-123"
        viewModel.addParticipant(athleteId)
        advanceUntilIdle()

        // Start run
        viewModel.startRun(athleteId)

        // Verify active run created
        viewModel.activeRuns.test {
            val activeRuns = awaitItem()
            assertThat(activeRuns).hasSize(1)
            assertThat(activeRuns[0].athleteId).isEqualTo(athleteId)
        }
    }

    @Test
    fun `startRun does not create duplicate active runs`() = runTest {
        // Setup: Start training and add participant
        viewModel.startTraining("Test Session")
        advanceUntilIdle()

        val athleteId = "athlete-123"
        viewModel.addParticipant(athleteId)
        advanceUntilIdle()

        // Start run twice
        viewModel.startRun(athleteId)
        viewModel.startRun(athleteId)  // Should be ignored

        // Verify only one active run
        viewModel.activeRuns.test {
            val activeRuns = awaitItem()
            assertThat(activeRuns).hasSize(1)
        }
    }

    @Test
    fun `stopRun persists run and removes from active runs`() = runTest {
        // Setup: Start training, add participant, start run
        viewModel.startTraining("Test Session")
        advanceUntilIdle()

        val athleteId = "athlete-123"
        viewModel.addParticipant(athleteId)
        advanceUntilIdle()

        viewModel.startRun(athleteId)

        // Stop run
        viewModel.stopRun(athleteId)
        advanceUntilIdle()

        // Verify run persisted
        coVerify {
            runRepository.addRun(
                match { run ->
                    run.athleteId == athleteId &&
                    run.durationMs != null &&
                    run.durationMs >= 0
                }
            )
        }

        // Verify removed from active runs
        viewModel.activeRuns.test {
            val activeRuns = awaitItem()
            assertThat(activeRuns).isEmpty()
        }
    }

    @Test
    fun `deleteAthlete removes from current training participants`() = runTest {
        // Setup mock to return athlete
        val athleteId = "athlete-123"
        val athlete = Athlete(id = athleteId, name = "Test Athlete")
        coEvery { athleteRepository.getAthleteById(athleteId) } returns athlete

        // Start training and add participant
        viewModel.startTraining("Test Session")
        advanceUntilIdle()

        viewModel.addParticipant(athleteId)
        advanceUntilIdle()

        // Delete athlete
        viewModel.deleteAthlete(athleteId)
        advanceUntilIdle()

        // Verify athlete deleted
        coVerify { athleteRepository.deleteAthlete(athleteId) }

        // Verify removed from training
        viewModel.currentTraining.test {
            val training = awaitItem()
            assertThat(training?.participantIds).doesNotContain(athleteId)
        }
    }

    @Test
    fun `addGroup creates new group in repository`() = runTest {
        val groupName = "Elite Group"

        viewModel.addGroup(groupName)
        advanceUntilIdle()

        coVerify {
            groupRepository.addGroup(
                match { group ->
                    group.name == groupName &&
                    group.memberIds.isEmpty()
                }
            )
        }
    }

    @Test
    fun `addGroupToTraining adds all group members as participants`() = runTest {
        // Setup: Group with members
        val groupId = "group-123"
        val athleteIds = listOf("athlete-1", "athlete-2", "athlete-3")
        val group = TrainingGroup(
            id = groupId,
            name = "Test Group",
            memberIds = athleteIds
        )

        coEvery { groupRepository.getAllGroups() } returns flowOf(listOf(group))

        // Recreate ViewModel to pick up new groups
        viewModel = TrainingViewModel(
            athleteRepository, runRepository, trainingRepository, groupRepository
        )

        // Start training
        viewModel.startTraining("Test Session")
        advanceUntilIdle()

        // Add group to training
        viewModel.addGroupToTraining(groupId)
        advanceUntilIdle()

        // Verify all athletes added
        athleteIds.forEach { athleteId ->
            coVerify { trainingRepository.addParticipant(any(), athleteId) }
        }

        // Verify state updated
        viewModel.currentTraining.test {
            val training = awaitItem()
            assertThat(training?.participantIds).containsExactlyElementsIn(athleteIds)
        }
    }

    @Test
    fun `updateRunNote updates active run note`() = runTest {
        // Setup: Start training, add participant, start run
        viewModel.startTraining("Test Session")
        advanceUntilIdle()

        val athleteId = "athlete-123"
        viewModel.addParticipant(athleteId)
        advanceUntilIdle()

        viewModel.startRun(athleteId)

        val runId = viewModel.activeRuns.value[0].id
        val note = "Great form!"

        // Update note
        viewModel.updateRunNote(runId, note)
        advanceUntilIdle()

        // Verify note updated in active run
        viewModel.activeRuns.test {
            val activeRuns = awaitItem()
            assertThat(activeRuns[0].note).isEqualTo(note)
        }
    }
}
