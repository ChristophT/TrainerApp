package com.innotime.trainerapp.presentation.screen.athletes

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.innotime.trainerapp.domain.model.Athlete
import com.innotime.trainerapp.domain.repository.AthleteRepository
import com.innotime.trainerapp.domain.repository.GroupRepository
import com.innotime.trainerapp.domain.repository.RunRepository
import com.innotime.trainerapp.domain.repository.TrainingRepository
import com.innotime.trainerapp.presentation.screen.training.TrainingViewModel
import com.innotime.trainerapp.presentation.theme.TrainerAppTheme
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for AthletesScreen.
 *
 * Tests demonstrate:
 * - Screen rendering with empty state
 * - Adding new athletes
 * - Editing existing athletes
 * - Deleting athletes
 * - User interaction flows
 */
class AthletesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: TrainingViewModel
    private lateinit var athleteRepository: AthleteRepository

    @Before
    fun setup() {
        // Create mock repositories
        athleteRepository = mockk(relaxed = true)
        val runRepository = mockk<RunRepository>(relaxed = true)
        val trainingRepository = mockk<TrainingRepository>(relaxed = true)
        val groupRepository = mockk<GroupRepository>(relaxed = true)

        // Setup default behaviors
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(emptyList())
        coEvery { runRepository.getAllRuns() } returns flowOf(emptyList())
        coEvery { trainingRepository.getAllTrainings() } returns flowOf(emptyList())
        coEvery { groupRepository.getAllGroups() } returns flowOf(emptyList())

        viewModel = TrainingViewModel(
            athleteRepository = athleteRepository,
            runRepository = runRepository,
            trainingRepository = trainingRepository,
            groupRepository = groupRepository
        )
    }

    @Test
    fun athletesScreen_emptyState_showsMessage() {
        composeTestRule.setContent {
            TrainerAppTheme {
                AthletesScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithText("No athletes yet. Add one above.", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun athletesScreen_withAthletes_displaysAll() {
        // Setup athletes
        val athletes = listOf(
            Athlete("1", "John Doe"),
            Athlete("2", "Jane Smith")
        )
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(athletes)

        // Recreate ViewModel with new data
        viewModel = TrainingViewModel(
            athleteRepository, mockk(relaxed = true),
            mockk(relaxed = true), mockk(relaxed = true)
        )

        composeTestRule.setContent {
            TrainerAppTheme {
                AthletesScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithText("John Doe")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Jane Smith")
            .assertIsDisplayed()
    }

    @Test
    fun athletesScreen_addButton_isDisplayed() {
        composeTestRule.setContent {
            TrainerAppTheme {
                AthletesScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Add")
            .assertIsDisplayed()
    }

    @Test
    fun athletesScreen_inputField_acceptsText() {
        composeTestRule.setContent {
            TrainerAppTheme {
                AthletesScreen(viewModel = viewModel)
            }
        }

        // Find and type in input field
        composeTestRule
            .onNodeWithText("New athlete name")
            .performTextInput("Test Athlete")

        // Verify text is displayed
        composeTestRule
            .onNodeWithText("Test Athlete")
            .assertIsDisplayed()
    }

    @Test
    fun athletesScreen_addButton_callsViewModel() {
        composeTestRule.setContent {
            TrainerAppTheme {
                AthletesScreen(viewModel = viewModel)
            }
        }

        // Type athlete name
        composeTestRule
            .onNodeWithText("New athlete name")
            .performTextInput("New Athlete")

        // Click add button
        composeTestRule
            .onNodeWithContentDescription("Add")
            .performClick()

        // Verify ViewModel was called
        coVerify { viewModel.addAthlete("New Athlete") }
    }

    @Test
    fun athletesScreen_editButton_showsEditMode() {
        // Setup with one athlete
        val athletes = listOf(Athlete("1", "John Doe"))
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(athletes)

        viewModel = TrainingViewModel(
            athleteRepository, mockk(relaxed = true),
            mockk(relaxed = true), mockk(relaxed = true)
        )

        composeTestRule.setContent {
            TrainerAppTheme {
                AthletesScreen(viewModel = viewModel)
            }
        }

        // Click edit button
        composeTestRule
            .onNodeWithContentDescription("Edit")
            .performClick()

        // Verify confirm/cancel buttons appear
        composeTestRule
            .onNodeWithContentDescription("Confirm")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Cancel")
            .assertIsDisplayed()
    }

    @Test
    fun athletesScreen_deleteButton_isDisplayed() {
        // Setup with one athlete
        val athletes = listOf(Athlete("1", "John Doe"))
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(athletes)

        viewModel = TrainingViewModel(
            athleteRepository, mockk(relaxed = true),
            mockk(relaxed = true), mockk(relaxed = true)
        )

        composeTestRule.setContent {
            TrainerAppTheme {
                AthletesScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Delete")
            .assertIsDisplayed()
    }

    @Test
    fun athletesScreen_title_isDisplayed() {
        composeTestRule.setContent {
            TrainerAppTheme {
                AthletesScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithText("Athletes")
            .assertIsDisplayed()
    }

    @Test
    fun athletesScreen_multipleAthletes_allVisible() {
        // Setup multiple athletes
        val athletes = (1..5).map { Athlete("$it", "Athlete $it") }
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(athletes)

        viewModel = TrainingViewModel(
            athleteRepository, mockk(relaxed = true),
            mockk(relaxed = true), mockk(relaxed = true)
        )

        composeTestRule.setContent {
            TrainerAppTheme {
                AthletesScreen(viewModel = viewModel)
            }
        }

        // Verify all athletes are displayed
        athletes.forEach { athlete ->
            composeTestRule
                .onNodeWithText(athlete.name)
                .assertIsDisplayed()
        }
    }
}
