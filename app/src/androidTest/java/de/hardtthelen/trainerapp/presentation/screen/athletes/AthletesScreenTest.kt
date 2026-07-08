package de.hardtthelen.trainerapp.presentation.screen.athletes

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import de.hardtthelen.trainerapp.R
import de.hardtthelen.trainerapp.domain.model.Athlete
import de.hardtthelen.trainerapp.domain.model.AthleteId
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

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

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
            .onNodeWithText(context.getString(R.string.no_athletes_yet), substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun athletesScreen_withAthletes_displaysAll() {
        // Setup athletes
        val athletes = listOf(
            Athlete(AthleteId("1"), "John Doe"),
            Athlete(AthleteId("2"), "Jane Smith")
        )
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(athletes)

        // Recreate ViewModel with new data
        viewModel = TrainingViewModel(
            athleteRepository = athleteRepository,
            runRepository = mockk(relaxed = true),
            trainingRepository = mockk(relaxed = true),
            groupRepository = mockk(relaxed = true)
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
            .onNodeWithContentDescription(context.getString(R.string.cd_add_button))
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
            .onNodeWithText(context.getString(R.string.new_athlete_name))
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
            .onNodeWithText(context.getString(R.string.new_athlete_name))
            .performTextInput("New Athlete")

        // Click add button
        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.cd_add_button))
            .performClick()

        // Verify Repository was called
        coVerify { athleteRepository.addAthlete(match { it.name == "New Athlete" }) }
    }

    @Test
    fun athletesScreen_editButton_showsEditMode() {
        // Setup with one athlete
        val athletes = listOf(Athlete(AthleteId("1"), "John Doe"))
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(athletes)

        viewModel = TrainingViewModel(
            athleteRepository = athleteRepository,
            runRepository = mockk(relaxed = true),
            trainingRepository = mockk(relaxed = true),
            groupRepository = mockk(relaxed = true)
        )

        composeTestRule.setContent {
            TrainerAppTheme {
                AthletesScreen(viewModel = viewModel)
            }
        }

        // Click edit button
        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.cd_edit_button))
            .performClick()

        // Verify confirm/cancel buttons appear
        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.confirm))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.cancel))
            .assertIsDisplayed()
    }

    @Test
    fun athletesScreen_deleteButton_isDisplayed() {
        // Setup with one athlete
        val athletes = listOf(Athlete(AthleteId("1"), "John Doe"))
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(athletes)

        viewModel = TrainingViewModel(
            athleteRepository = athleteRepository,
            runRepository = mockk(relaxed = true),
            trainingRepository = mockk(relaxed = true),
            groupRepository = mockk(relaxed = true)
        )

        composeTestRule.setContent {
            TrainerAppTheme {
                AthletesScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.cd_delete_button))
            .assertIsDisplayed()
    }

    @Test
    fun athletesScreen_confirmDelete_callsViewModel() {
        // Setup with one athlete
        val athleteId = AthleteId("1")
        val athletes = listOf(Athlete(athleteId, "John Doe"))
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(athletes)

        viewModel = TrainingViewModel(
            athleteRepository = athleteRepository,
            runRepository = mockk(relaxed = true),
            trainingRepository = mockk(relaxed = true),
            groupRepository = mockk(relaxed = true)
        )

        composeTestRule.setContent {
            TrainerAppTheme {
                AthletesScreen(viewModel = viewModel)
            }
        }

        // Click delete icon
        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.cd_delete_button))
            .performClick()

        // Verify dialog shown
        composeTestRule
            .onNodeWithText(context.getString(R.string.delete_athlete))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.getString(R.string.delete_athlete_confirm, "John Doe"), substring = true)
            .assertIsDisplayed()

        // Click confirm Delete in dialog
        composeTestRule
            .onAllNodesWithText(context.getString(R.string.delete))
            .filterToOne(hasClickAction())
            .performClick()

        // Verify repository called
        coVerify { athleteRepository.deleteAthlete(athleteId) }
    }

    @Test
    fun athletesScreen_title_isDisplayed() {
        composeTestRule.setContent {
            TrainerAppTheme {
                AthletesScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.athletes_title))
            .assertIsDisplayed()
    }

    @Test
    fun athletesScreen_multipleAthletes_allVisible() {
        // Setup multiple athletes
        val athletes = (1..5).map { Athlete(AthleteId("$it"), "Athlete $it") }
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(athletes)

        viewModel = TrainingViewModel(
            athleteRepository = athleteRepository,
            runRepository = mockk(relaxed = true),
            trainingRepository = mockk(relaxed = true),
            groupRepository = mockk(relaxed = true)
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
