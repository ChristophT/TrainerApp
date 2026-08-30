package de.hardtthelen.trainerapp.presentation.screen.training

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import de.hardtthelen.trainerapp.domain.model.Athlete
import de.hardtthelen.trainerapp.domain.model.AthleteId
import de.hardtthelen.trainerapp.domain.model.TrainingGroup
import de.hardtthelen.trainerapp.domain.model.TrainingGroupId
import de.hardtthelen.trainerapp.domain.model.TrainingId
import de.hardtthelen.trainerapp.domain.model.TrainingParticipant
import de.hardtthelen.trainerapp.domain.repository.AthleteRepository
import de.hardtthelen.trainerapp.domain.repository.GroupRepository
import de.hardtthelen.trainerapp.domain.repository.RunRepository
import de.hardtthelen.trainerapp.domain.repository.TrainingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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

        mockkStatic(android.os.SystemClock::class)
        every { android.os.SystemClock.elapsedRealtime() } returns 1000L

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
        unmockkStatic(android.os.SystemClock::class)
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
                    athlete.id.value.isNotEmpty()
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
            assertThat(training?.participants).isEmpty()
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
        val athleteId = AthleteId.newId()
        val newAthlete = Athlete(athleteId, "Test Athlete")
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(listOf(newAthlete))
        val newParticipant = TrainingParticipant(newAthlete, 1)
        coEvery { trainingRepository.addParticipant(any(), any()) } returns newParticipant

        // Recreate ViewModel to pick up athlete
        viewModel = TrainingViewModel(
            athleteRepository, runRepository, trainingRepository, groupRepository
        )

        // Start collection to make athletes available in athletes.value
        backgroundScope.launch { viewModel.athletes.collect {} }
        advanceUntilIdle()

        // Setup: Start a training session
        viewModel.startTraining("Test Session")
        advanceUntilIdle()

        val trainingId = viewModel.currentTraining.value?.id
        assertThat(trainingId).isNotNull()

        // Add participant
        viewModel.addParticipant(athleteId)
        advanceUntilIdle()

        // Verify repository was called
        coVerify {
            trainingRepository.addParticipant(trainingId!!, newAthlete)
        }

        // Verify state updated
        viewModel.currentTraining.test {
            val training = awaitItem()
            assertThat(training?.participants).contains(newParticipant)
        }
    }

    @Test
    fun `startRun creates active run for athlete`() = runTest {
        // Setup: Start training and add participant
        viewModel.startTraining("Test Session")
        advanceUntilIdle()

        val athleteId = AthleteId.newId()
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

        val athleteId = AthleteId.newId()
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

        val athleteId = AthleteId.newId()
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
        val athleteId = AthleteId.newId()
        val athlete = Athlete(id = athleteId, name = "Test Athlete")
        coEvery { athleteRepository.getAthleteById(athleteId) } returns athlete
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(listOf(athlete))
        coEvery { trainingRepository.addParticipant(any(), any()) } returns TrainingParticipant(athlete, 1)

        // Recreate ViewModel to pick up athlete
        viewModel = TrainingViewModel(
            athleteRepository, runRepository, trainingRepository, groupRepository
        )

        // Start collection
        backgroundScope.launch { viewModel.athletes.collect {} }
        advanceUntilIdle()

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
            assertThat(training?.participants?.map { it.athlete.id }).doesNotContain(athleteId)
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
        val groupId = TrainingGroupId.newId()
        val athleteId1 = AthleteId.newId()
        val athleteId2 = AthleteId.newId()
        val athleteId3 = AthleteId.newId()
        val athleteIds = listOf(athleteId1, athleteId2, athleteId3)
        val newAthlete1 = Athlete(athleteId1, "New Athlete1")
        val newAthlete2 = Athlete(athleteId2, "New Athlete2")
        val newAthlete3 = Athlete(athleteId3, "New Athlete3")
        val group = TrainingGroup(
            id = groupId,
            name = "Test Group",
            memberIds = athleteIds
        )
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(listOf(newAthlete1, newAthlete2, newAthlete3))
        coEvery { groupRepository.getAllGroups() } returns flowOf(listOf(group))

        coEvery { trainingRepository.addParticipant(any(), any()) } answers {
            TrainingParticipant(it.invocation.args[1] as Athlete, 0)
        }

        // Recreate ViewModel to pick up new groups
        viewModel = TrainingViewModel(
            athleteRepository, runRepository, trainingRepository, groupRepository
        )

        // Start collection to trigger WhileSubscribed flows
        backgroundScope.launch { viewModel.athletes.collect {} }
        backgroundScope.launch { viewModel.groups.collect {} }
        advanceUntilIdle()

        // Start training
        viewModel.startTraining("Test Session")
        advanceUntilIdle()

        // Add group to training
        viewModel.addGroupToTraining(groupId)
        advanceUntilIdle()

        // Verify state updated
        viewModel.currentTraining.test {
            val training = awaitItem()
            assertThat(training?.participants?.map { it.athlete.id }).containsExactlyElementsIn(athleteIds)
        }
    }

    @Test
    fun `updateRunNote updates active run note`() = runTest {
        // Setup: Start training, add participant, start run
        viewModel.startTraining("Test Session")
        advanceUntilIdle()

        val athleteId = AthleteId.newId()
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

    @Test
    fun `deleteGroup calls repository`() = runTest {
        val groupId = TrainingGroupId.newId()

        viewModel.deleteGroup(groupId)
        advanceUntilIdle()

        coVerify {
            groupRepository.deleteGroup(groupId)
        }
    }

    @Test
    fun `deleteFinishedTrainingSession calls repository`() = runTest {
        val trainingId = TrainingId.newId()

        viewModel.deleteFinishedTrainingSession(trainingId)
        advanceUntilIdle()

        coVerify {
            trainingRepository.deleteTraining(trainingId)
        }
    }
}
