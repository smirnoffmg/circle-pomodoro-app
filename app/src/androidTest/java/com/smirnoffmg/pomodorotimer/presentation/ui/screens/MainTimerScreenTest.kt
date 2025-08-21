package com.smirnoffmg.pomodorotimer.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smirnoffmg.pomodorotimer.presentation.ui.theme.PomodoroTimerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for MainTimerScreen following TDD principles.
 * Tests accessibility, screen sizes, and user interactions.
 */
@RunWith(AndroidJUnit4::class)
class MainTimerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenScreenLoaded_shouldDisplayTimerElements() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // Then - Timer elements should be visible
        composeTestRule.onNodeWithTag("timer_progress").assertIsDisplayed()
        composeTestRule.onNodeWithTag("timer_display").assertIsDisplayed()
        composeTestRule.onNodeWithTag("timer_state").assertIsDisplayed()
        composeTestRule.onNodeWithTag("start_pause_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stop_button").assertIsDisplayed()
    }

    @Test
    fun whenScreenLoaded_shouldDisplayInitialTime() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // Then - Should display 25:00 (25 minutes)
        composeTestRule.onNodeWithText("25:00").assertIsDisplayed()
    }

    @Test
    fun whenScreenLoaded_shouldDisplayInitialState() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // Then - Should display "Ready to Start" state
        composeTestRule.onNodeWithText("Ready to Start").assertIsDisplayed()
    }

    @Test
    fun whenScreenLoaded_shouldDisplayStartButton() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // Then - Start button should be visible and enabled
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
        composeTestRule.onNodeWithTag("start_pause_button").assertIsEnabled()
    }

    @Test
    fun whenScreenLoaded_shouldDisplayStopButton() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // Then - Stop button should be visible but disabled initially
        composeTestRule.onNodeWithText("Stop").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stop_button").assertIsNotEnabled()
    }

    @Test
    fun whenStartButtonClicked_shouldChangeToPauseButton() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // When - Start button is clicked
        composeTestRule.onNodeWithTag("start_pause_button").performClick()

        // Then - Button should change to "Pause"
        composeTestRule.onNodeWithText("Pause").assertIsDisplayed()
    }

    @Test
    fun whenStartButtonClicked_shouldEnableStopButton() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // When - Start button is clicked
        composeTestRule.onNodeWithTag("start_pause_button").performClick()

        // Then - Stop button should be enabled
        composeTestRule.onNodeWithTag("stop_button").assertIsEnabled()
    }

    @Test
    fun whenStartButtonClicked_shouldChangeStateToRunning() {
        // Given - Screen is loaded
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }

        // When - Start button is clicked
        composeTestRule.onNodeWithTag("start_pause_button").performClick()

        // Then - State should change to "Focus Time"
        composeTestRule.onNodeWithText("Focus Time").assertIsDisplayed()
    }

    @Test
    fun whenPauseButtonClicked_shouldChangeStateToPaused() {
        // Given - Timer is running
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }
        composeTestRule.onNodeWithTag("start_pause_button").performClick()

        // When - Pause button is clicked
        composeTestRule.onNodeWithTag("start_pause_button").performClick()

        // Then - State should change to "Paused"
        composeTestRule.onNodeWithText("Paused").assertIsDisplayed()
    }

    @Test
    fun whenStopButtonClicked_shouldResetToInitialState() {
        // Given - Timer is running
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }
        composeTestRule.onNodeWithTag("start_pause_button").performClick()

        // When - Stop button is clicked
        composeTestRule.onNodeWithTag("stop_button").performClick()

        // Then - Should reset to initial state
        composeTestRule.onNodeWithText("Ready to Start").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stop_button").assertIsNotEnabled()
    }

    @Test
    fun whenStopButtonClicked_shouldResetTimeTo25Minutes() {
        // Given - Timer is running
        composeTestRule.setContent {
            PomodoroTimerTheme {
                MainTimerScreen()
            }
        }
        composeTestRule.onNodeWithTag("start_pause_button").performClick()

        // When - Stop button is clicked
        composeTestRule.onNodeWithTag("stop_button").performClick()

        // Then - Time should reset to 25:00
        composeTestRule.onNodeWithText("25:00").assertIsDisplayed()
    }
}
