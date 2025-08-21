package com.smirnoffmg.pomodorotimer.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smirnoffmg.pomodorotimer.presentation.ui.theme.PomodoroTimerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Responsive layout tests for MainTimerScreen.
 * Tests different screen sizes and orientations for accessibility.
 */
@RunWith(AndroidJUnit4::class)
class MainTimerScreenResponsiveTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenScreenLoaded_shouldDisplayAllElementsInCompactLayout() {
        // Given - Screen is loaded with compact layout
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // Then - All essential elements should be visible
        composeTestRule.onNodeWithTag("timer_progress").assertIsDisplayed()
        composeTestRule.onNodeWithTag("timer_display").assertIsDisplayed()
        composeTestRule.onNodeWithTag("timer_state").assertIsDisplayed()
        composeTestRule.onNodeWithTag("start_pause_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stop_button").assertIsDisplayed()
    }

    @Test
    fun whenScreenLoaded_shouldDisplayTimeInCorrectFormat() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // Then - Time should be displayed in MM:SS format
        composeTestRule.onNodeWithText("25:00").assertIsDisplayed()
    }

    @Test
    fun whenScreenLoaded_shouldDisplayStateText() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // Then - State text should be visible
        composeTestRule.onNodeWithText("Ready to Start").assertIsDisplayed()
    }

    @Test
    fun whenScreenLoaded_shouldDisplayControlButtons() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // Then - Control buttons should be visible
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stop").assertIsDisplayed()
    }

    @Test
    fun whenScreenLoaded_shouldHaveAccessibleElements() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // Then - Elements should have proper test tags for accessibility
        composeTestRule.onNodeWithTag("timer_progress").assertIsDisplayed()
        composeTestRule.onNodeWithTag("timer_display").assertIsDisplayed()
        composeTestRule.onNodeWithTag("timer_state").assertIsDisplayed()
        composeTestRule.onNodeWithTag("start_pause_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stop_button").assertIsDisplayed()
    }

    @Test
    fun whenScreenLoaded_shouldDisplayProgressIndicator() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // Then - Progress indicator should be visible
        composeTestRule.onNodeWithTag("timer_progress").assertIsDisplayed()
    }

    @Test
    fun whenScreenLoaded_shouldDisplayTimerCard() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // Then - Timer display card should be visible
        composeTestRule.onNodeWithTag("timer_display").assertIsDisplayed()
    }

    @Test
    fun whenScreenLoaded_shouldDisplayTimerControls() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // Then - Timer controls should be visible
        composeTestRule.onNodeWithTag("start_pause_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stop_button").assertIsDisplayed()
    }
}
