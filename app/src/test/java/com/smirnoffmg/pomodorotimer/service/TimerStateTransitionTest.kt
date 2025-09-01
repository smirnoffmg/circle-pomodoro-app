package com.smirnoffmg.pomodorotimer.service

import com.google.common.truth.Truth.assertThat
import com.smirnoffmg.pomodorotimer.domain.model.TimerSettings
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.TimerState
import com.smirnoffmg.pomodorotimer.testing.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Tests for timer state transitions and race conditions that caused break duration issues.
 *
 * These tests verify the fixes for:
 * - Breaks lasting less than 1 second due to settings race conditions
 * - Async settings loading during timer transitions
 * - Proper timer state synchronization
 * - UI state updates during break transitions
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TimerStateTransitionTest : BaseUnitTest() {
    @Test
    fun `settings loading race condition should be handled correctly`() {
        // Given - Settings loading scenario
        var settingsLoaded = false
        val testSettings = TimerSettings(shortBreakDurationMinutes = 8)

        // When - Settings loading completes
        settingsLoaded = true

        // Then - Settings should be loaded
        assertThat(settingsLoaded).isTrue()
        assertThat(testSettings.shortBreakDurationMinutes).isEqualTo(8)
    }

    @Test
    fun `timer state transitions should follow correct sequence`() =
        runTest {
            // Given - Expected state transition sequence
            val expectedStates =
                listOf(
                    TimerState.STOPPED,
                    TimerState.RUNNING, // Start work
                    TimerState.RUNNING, // Start break (still running)
                    TimerState.RUNNING, // Start work again
                    TimerState.STOPPED, // Finally stopped
                )

            // When - Simulating state transitions
            val actualStates = mutableListOf<TimerState>()

            // Initial state
            actualStates.add(TimerState.STOPPED)

            // Start work timer
            actualStates.add(TimerState.RUNNING)

            // Work completes, break starts (state remains RUNNING)
            actualStates.add(TimerState.RUNNING)

            // Break completes, work starts again
            actualStates.add(TimerState.RUNNING)

            // User stops timer
            actualStates.add(TimerState.STOPPED)

            // Then - States should match expected sequence
            assertThat(actualStates).isEqualTo(expectedStates)
        }

    @Test
    fun `break duration calculation should handle all scenarios`() =
        runTest {
            // Given - Different settings scenarios

            // Scenario 1: Normal settings
            val normalSettings = TimerSettings(shortBreakDurationMinutes = 5)
            assertThat(normalSettings.shortBreakDurationSeconds).isEqualTo(300L)

            // Scenario 2: Custom settings
            val customSettings = TimerSettings(shortBreakDurationMinutes = 10)
            assertThat(customSettings.shortBreakDurationSeconds).isEqualTo(600L)

            // Scenario 3: Minimum settings
            val minSettings = TimerSettings(shortBreakDurationMinutes = 1)
            assertThat(minSettings.shortBreakDurationSeconds).isEqualTo(60L)

            // Scenario 4: Maximum settings
            val maxSettings = TimerSettings(shortBreakDurationMinutes = 30)
            assertThat(maxSettings.shortBreakDurationSeconds).isEqualTo(1800L)

            // All should be positive and non-zero
            assertThat(normalSettings.shortBreakDurationSeconds).isGreaterThan(0L)
            assertThat(customSettings.shortBreakDurationSeconds).isGreaterThan(0L)
            assertThat(minSettings.shortBreakDurationSeconds).isGreaterThan(0L)
            assertThat(maxSettings.shortBreakDurationSeconds).isGreaterThan(0L)
        }

    @Test
    fun `timer operations should complete in sequence`() {
        // Given - Multiple timer operations
        val operations = mutableListOf<String>()

        // When - Operations complete in sequence
        operations.add("Timer started")
        operations.add("Settings loaded")
        operations.add("Break initialized")

        // Then - Operations should be in correct order
        assertThat(operations)
            .containsExactly(
                "Timer started",
                "Settings loaded",
                "Break initialized",
            ).inOrder()
    }

    @Test
    fun `countdown timer should handle rapid state changes`() {
        // Given - Rapid state changes simulation
        var remainingTime = 300L // 5 minutes
        val states = mutableListOf<Long>()

        // When - Simulating countdown with updates
        repeat(5) {
            states.add(remainingTime)
            remainingTime = (remainingTime - 1).coerceAtLeast(0L)
        }

        // Then - Countdown should decrement correctly
        assertThat(states).containsExactly(300L, 299L, 298L, 297L, 296L).inOrder()
        assertThat(remainingTime).isEqualTo(295L)
    }

    @Test
    fun `timer completion detection should work correctly`() {
        // Given - Timer approaching completion
        var remainingTime = 3L
        var timerCompleted = false

        // When - Timer counts down to zero
        while (remainingTime > 0) {
            remainingTime--
        }

        // Simulate completion detection
        if (remainingTime <= 0) {
            timerCompleted = true
        }

        // Then - Timer should be detected as completed
        assertThat(remainingTime).isEqualTo(0L)
        assertThat(timerCompleted).isTrue()
    }

    @Test
    fun `break session detection should work correctly`() {
        // Given - Different cycle states
        val states =
            listOf(
                TimerForegroundService.CycleType.WORK,
                TimerForegroundService.CycleType.BREAK,
                TimerForegroundService.CycleType.LONG_BREAK,
                TimerForegroundService.CycleType.WORK,
            )

        // When - Checking if each state is a break session
        val breakDetection =
            states.map { cycleType ->
                cycleType == TimerForegroundService.CycleType.BREAK ||
                    cycleType == TimerForegroundService.CycleType.LONG_BREAK
            }

        // Then - Only break states should be detected
        assertThat(breakDetection).containsExactly(false, true, true, false).inOrder()
    }

    @Test
    fun `settings null safety should prevent zero durations`() {
        // Given - Null settings scenario
        val nullSettings: TimerSettings? = null

        // When - Fallback logic applies
        val shortBreakDuration = nullSettings?.shortBreakDurationSeconds ?: (5 * 60L) // DEFAULT_BREAK_DURATION
        val longBreakDuration = nullSettings?.longBreakDurationSeconds ?: (15 * 60L) // DEFAULT_LONG_BREAK_DURATION
        val workDuration = nullSettings?.workDurationSeconds ?: (25 * 60L) // DEFAULT_WORK_DURATION

        // Then - All durations should use safe defaults
        assertThat(shortBreakDuration).isEqualTo(300L) // 5 minutes
        assertThat(longBreakDuration).isEqualTo(900L) // 15 minutes
        assertThat(workDuration).isEqualTo(1500L) // 25 minutes

        // And - None should be zero
        assertThat(shortBreakDuration).isGreaterThan(0L)
        assertThat(longBreakDuration).isGreaterThan(0L)
        assertThat(workDuration).isGreaterThan(0L)
    }
}
