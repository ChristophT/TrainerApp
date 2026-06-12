package de.hardtthelen.trainerapp.presentation.screen.results

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import de.hardtthelen.trainerapp.domain.model.Training
import de.hardtthelen.trainerapp.domain.model.TrainingId
import de.hardtthelen.trainerapp.domain.repository.AthleteRepository
import de.hardtthelen.trainerapp.domain.repository.GroupRepository
import de.hardtthelen.trainerapp.domain.repository.RunRepository
import de.hardtthelen.trainerapp.domain.repository.TrainingRepository
import de.hardtthelen.trainerapp.presentation.screen.training.TrainingViewModel
import de.hardtthelen.trainerapp.presentation.theme.TrainerAppTheme
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ResultsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: TrainingViewModel
    private lateinit var trainingRepository: TrainingRepository

    @Before
    fun setup() {
        trainingRepository = mockk(relaxed = true)
        val athleteRepository = mockk<AthleteRepository>(relaxed = true)
        val runRepository = mockk<RunRepository>(relaxed = true)
        val groupRepository = mockk<GroupRepository>(relaxed = true)

        coEvery { trainingRepository.getAllTrainings() } returns flowOf(emptyList())
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(emptyList())
        coEvery { runRepository.getAllRuns() } returns flowOf(emptyList())
        coEvery { groupRepository.getAllGroups() } returns flowOf(emptyList())

        viewModel = TrainingViewModel(
            athleteRepository = athleteRepository,
            runRepository = runRepository,
            trainingRepository = trainingRepository,
            groupRepository = groupRepository
        )
    }

    @Test
    fun resultsScreen_deleteButton_showsConfirmationDialog() {
        val training = Training(
            id = TrainingId("1"),
            date = System.currentTimeMillis(),
            description = "Test Session",
            participantIds = emptyList(),
            runIds = emptyList()
        )
        coEvery { trainingRepository.getAllTrainings() } returns flowOf(listOf(training))

        // Recreate ViewModel with data
        viewModel = TrainingViewModel(
            mockk(relaxed = true), mockk(relaxed = true),
            trainingRepository, mockk(relaxed = true)
        )

        composeTestRule.setContent {
            TrainerAppTheme {
                ResultsScreen(viewModel = viewModel)
            }
        }

        // Find delete button and click it
        composeTestRule
            .onNodeWithContentDescription("Delete")
            .performClick()

        // Verify dialog is shown
        composeTestRule
            .onNodeWithText("Delete session")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Delete session \"Test Session\"?", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun resultsScreen_confirmDelete_callsViewModel() {
        val trainingId = TrainingId("1")
        val training = Training(
            id = trainingId,
            date = System.currentTimeMillis(),
            description = "Test Session",
            participantIds = emptyList(),
            runIds = emptyList()
        )
        coEvery { trainingRepository.getAllTrainings() } returns flowOf(listOf(training))

        // Recreate ViewModel with data
        viewModel = TrainingViewModel(
            mockk(relaxed = true), mockk(relaxed = true),
            trainingRepository, mockk(relaxed = true)
        )

        composeTestRule.setContent {
            TrainerAppTheme {
                ResultsScreen(viewModel = viewModel)
            }
        }

        // Open dialog
        composeTestRule
            .onNodeWithContentDescription("Delete")
            .performClick()

        // Click Delete in dialog
        // We use onAllNodesWithText and pick the one that has a click action (the button)
        // or just use onNode with multiple criteria.
        composeTestRule
            .onAllNodesWithText("Delete")
            .filterToOne(hasClickAction())
            .performClick()

        // Verify Repository was called
        coVerify { trainingRepository.deleteTraining(trainingId) }

        // Verify dialog is dismissed
        composeTestRule
            .onNodeWithText("Delete session")
            .assertDoesNotExist()
    }

    @Test
    fun resultsScreen_cancelDelete_doesNotCallViewModel() {
        val training = Training(
            id = TrainingId("1"),
            date = System.currentTimeMillis(),
            description = "Test Session",
            participantIds = emptyList(),
            runIds = emptyList()
        )
        coEvery { trainingRepository.getAllTrainings() } returns flowOf(listOf(training))

        // Recreate ViewModel with data
        viewModel = TrainingViewModel(
            mockk(relaxed = true), mockk(relaxed = true),
            trainingRepository, mockk(relaxed = true)
        )

        composeTestRule.setContent {
            TrainerAppTheme {
                ResultsScreen(viewModel = viewModel)
            }
        }

        // Open dialog
        composeTestRule
            .onNodeWithContentDescription("Delete")
            .performClick()

        // Click Cancel in dialog
        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        // Verify Repository was NOT called
        coVerify(exactly = 0) { trainingRepository.deleteTraining(any()) }

        // Verify dialog is dismissed
        composeTestRule
            .onNodeWithText("Delete session")
            .assertDoesNotExist()
    }
}
