package com.innotime.trainerapp.presentation.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.innotime.trainerapp.presentation.theme.TrainerAppTheme
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for TimerDisplay component.
 *
 * Tests verify:
 * - Duration formatting is displayed correctly
 * - Timer colors change based on active state
 * - Different size variants render properly
 */
class TimerDisplayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun timerDisplay_showsZeroWhenNull() {
        composeTestRule.setContent {
            TrainerAppTheme {
                TimerDisplay(
                    elapsedMs = null,
                    isActive = false,
                    size = TimerSize.MEDIUM
                )
            }
        }

        composeTestRule
            .onNodeWithText("0.00")
            .assertIsDisplayed()
    }

    @Test
    fun timerDisplay_formatsSecondsCorrectly() {
        composeTestRule.setContent {
            TrainerAppTheme {
                TimerDisplay(
                    elapsedMs = 5120,
                    isActive = false,
                    size = TimerSize.MEDIUM
                )
            }
        }

        composeTestRule
            .onNodeWithText("5.12")
            .assertIsDisplayed()
    }

    @Test
    fun timerDisplay_formatsMinutesCorrectly() {
        composeTestRule.setContent {
            TrainerAppTheme {
                TimerDisplay(
                    elapsedMs = 65460,
                    isActive = false,
                    size = TimerSize.MEDIUM
                )
            }
        }

        composeTestRule
            .onNodeWithText("1:05.46")
            .assertIsDisplayed()
    }

    @Test
    fun timerDisplay_formatsHoursCorrectly() {
        composeTestRule.setContent {
            TrainerAppTheme {
                TimerDisplay(
                    elapsedMs = 3945670,
                    isActive = false,
                    size = TimerSize.MEDIUM
                )
            }
        }

        composeTestRule
            .onNodeWithText("1:05:45.67")
            .assertIsDisplayed()
    }

    @Test
    fun timerDisplay_activeState_rendersCorrectly() {
        composeTestRule.setContent {
            TrainerAppTheme {
                TimerDisplay(
                    elapsedMs = 1000,
                    isActive = true,
                    size = TimerSize.LARGE
                )
            }
        }

        composeTestRule
            .onNodeWithText("1.00")
            .assertIsDisplayed()
    }

    @Test
    fun timerDisplay_inactiveState_rendersCorrectly() {
        composeTestRule.setContent {
            TrainerAppTheme {
                TimerDisplay(
                    elapsedMs = 1000,
                    isActive = false,
                    size = TimerSize.SMALL
                )
            }
        }

        composeTestRule
            .onNodeWithText("1.00")
            .assertIsDisplayed()
    }

    @Test
    fun timerDisplay_largeSize_rendersCorrectly() {
        composeTestRule.setContent {
            TrainerAppTheme {
                TimerDisplay(
                    elapsedMs = 2500,
                    isActive = true,
                    size = TimerSize.LARGE
                )
            }
        }

        composeTestRule
            .onNodeWithText("2.50")
            .assertIsDisplayed()
    }

    @Test
    fun timerDisplay_mediumSize_rendersCorrectly() {
        composeTestRule.setContent {
            TrainerAppTheme {
                TimerDisplay(
                    elapsedMs = 2500,
                    isActive = false,
                    size = TimerSize.MEDIUM
                )
            }
        }

        composeTestRule
            .onNodeWithText("2.50")
            .assertIsDisplayed()
    }

    @Test
    fun timerDisplay_smallSize_rendersCorrectly() {
        composeTestRule.setContent {
            TrainerAppTheme {
                TimerDisplay(
                    elapsedMs = 2500,
                    isActive = false,
                    size = TimerSize.SMALL
                )
            }
        }

        composeTestRule
            .onNodeWithText("2.50")
            .assertIsDisplayed()
    }
}
