package de.hardtthelen.trainerapp.presentation.screen.groups

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import de.hardtthelen.trainerapp.domain.model.TrainingGroup
import de.hardtthelen.trainerapp.domain.model.TrainingGroupId
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

class GroupsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: TrainingViewModel
    private lateinit var groupRepository: GroupRepository

    @Before
    fun setup() {
        groupRepository = mockk(relaxed = true)
        val athleteRepository = mockk<AthleteRepository>(relaxed = true)
        val runRepository = mockk<RunRepository>(relaxed = true)
        val trainingRepository = mockk<TrainingRepository>(relaxed = true)

        coEvery { groupRepository.getAllGroups() } returns flowOf(emptyList())
        coEvery { athleteRepository.getAllAthletes() } returns flowOf(emptyList())
        coEvery { runRepository.getAllRuns() } returns flowOf(emptyList())
        coEvery { trainingRepository.getAllTrainings() } returns flowOf(emptyList())

        viewModel = TrainingViewModel(
            athleteRepository = athleteRepository,
            runRepository = runRepository,
            trainingRepository = trainingRepository,
            groupRepository = groupRepository
        )
    }

    @Test
    fun groupsScreen_confirmDelete_callsViewModel() {
        val groupId = TrainingGroupId("1")
        val group = TrainingGroup(groupId, "Elite Squad", emptyList())
        coEvery { groupRepository.getAllGroups() } returns flowOf(listOf(group))

        // Recreate ViewModel with data
        viewModel = TrainingViewModel(
            mockk(relaxed = true), mockk(relaxed = true),
            mockk(relaxed = true), groupRepository
        )

        composeTestRule.setContent {
            TrainerAppTheme {
                GroupsScreen(viewModel = viewModel)
            }
        }

        // Click delete icon
        composeTestRule
            .onNodeWithContentDescription("Delete")
            .performClick()

        // Verify dialog shown
        composeTestRule
            .onNodeWithText("Delete group")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Delete group \"Elite Squad\"?", substring = true)
            .assertIsDisplayed()

        // Click confirm Delete in dialog
        composeTestRule
            .onAllNodesWithText("Delete")
            .filterToOne(hasClickAction())
            .performClick()

        // Verify repository called
        coVerify { groupRepository.deleteGroup(groupId) }
    }
}
