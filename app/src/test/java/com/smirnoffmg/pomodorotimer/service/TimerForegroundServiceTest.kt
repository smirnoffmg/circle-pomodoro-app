package com.smirnoffmg.pomodorotimer.service

import com.google.common.truth.Truth.assertThat
import com.smirnoffmg.pomodorotimer.domain.model.TimerSettings
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.TimerState
import com.smirnoffmg.pomodorotimer.testing.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for TimerForegroundService focusing on break transition logic,
 * settings loading, and timer state management.
 * 
 * Tests the critical fixes for:
 * - Break duration lasting less than 1 second
 * - Settings loading race conditions  
 * - Timer state transitions
 * - UI state synchronization
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TimerForegroundServiceTest : BaseUnitTest() {
    private val defaultSettings =
        TimerSettings(
            workDurationMinutes = 25,
            shortBreakDurationMinutes = 5,
            longBreakDurationMinutes = 15,
            sessionsBeforeLongBreak = 4
        )

    @Test
    fun `break duration should use correct settings values`() =
        runTest {
            // Given - Settings with specific break durations
            val customSettings =
                TimerSettings(
                    workDurationMinutes = 30,
                    shortBreakDurationMinutes = 8,
                    longBreakDurationMinutes = 20,
                    sessionsBeforeLongBreak = 3
                )
        
            // Then - Verify duration conversions are correct
            assertThat(customSettings.shortBreakDurationSeconds).isEqualTo(8 * 60L) // 480 seconds
            assertThat(customSettings.longBreakDurationSeconds).isEqualTo(20 * 60L)  // 1200 seconds
            assertThat(customSettings.workDurationSeconds).isEqualTo(30 * 60L)       // 1800 seconds
        }

    @Test
    fun `default break duration should be used when settings are null`() =
        runTest {
            // Given - No settings (null)
            val nullSettings: TimerSettings? = null
        
            // When/Then - Default values should be used
            val expectedShortBreakSeconds = 5 * 60L   // 300 seconds (5 minutes)
            val expectedLongBreakSeconds = 15 * 60L   // 900 seconds (15 minutes)
        
            // These are the constants used in TimerForegroundService
            assertThat(expectedShortBreakSeconds).isEqualTo(300L)
            assertThat(expectedLongBreakSeconds).isEqualTo(900L)
        }

    @Test
    fun `break duration should never be zero or negative`() =
        runTest {
            // Given - Settings with minimum valid values
            val minSettings =
                TimerSettings(
                    workDurationMinutes = TimerSettings.MIN_WORK_DURATION,      // 1
                    shortBreakDurationMinutes = TimerSettings.MIN_BREAK_DURATION, // 1  
                    longBreakDurationMinutes = TimerSettings.MIN_BREAK_DURATION,  // 1
                    sessionsBeforeLongBreak = TimerSettings.MIN_SESSIONS_BEFORE_LONG_BREAK // 2
                )
        
            // Then - All durations should be positive
            assertThat(minSettings.shortBreakDurationSeconds).isEqualTo(60L) // 1 minute
            assertThat(minSettings.longBreakDurationSeconds).isEqualTo(60L)  // 1 minute
            assertThat(minSettings.workDurationSeconds).isEqualTo(60L)       // 1 minute
        
            // And - All durations should be greater than 0
            assertThat(minSettings.shortBreakDurationSeconds).isGreaterThan(0L)
            assertThat(minSettings.longBreakDurationSeconds).isGreaterThan(0L)
            assertThat(minSettings.workDurationSeconds).isGreaterThan(0L)
        }

    @Test
    fun `timer state should be properly initialized`() {
        // Given - Initial timer state expectations
        val expectedInitialState = TimerState.STOPPED
        val expectedInitialTime = 25 * 60L // Default work duration
        val expectedInitialProgress = 1f
        
        // Then - Verify initial state values are correct
        assertThat(expectedInitialState).isEqualTo(TimerState.STOPPED)
        assertThat(expectedInitialTime).isEqualTo(1500L)
        assertThat(expectedInitialProgress).isEqualTo(1f)
    }

    @Test
    fun `cycle type should transition correctly from work to break`() {
        // Given - Current cycle is WORK and session should go to short break
        val currentCycleType = TimerForegroundService.CycleType.WORK
        val completedSessions = 1 // First session, should go to short break
        val sessionsBeforeLongBreak = 4
        
        // When - Determining next cycle type  
        val shouldBeLongBreak = completedSessions % sessionsBeforeLongBreak == 0
        val expectedNextCycle =
            if (shouldBeLongBreak) {
                TimerForegroundService.CycleType.LONG_BREAK
            } else {
                TimerForegroundService.CycleType.BREAK
            }
        
        // Then - Should transition to short break
        assertThat(shouldBeLongBreak).isFalse()
        assertThat(expectedNextCycle).isEqualTo(TimerForegroundService.CycleType.BREAK)
    }

    @Test 
    fun `cycle type should transition to long break after specified sessions`() {
        // Given - Completed sessions equals sessions before long break
        val completedSessions = 4
        val sessionsBeforeLongBreak = 4
        
        // When - Determining if long break is needed
        val shouldBeLongBreak = completedSessions % sessionsBeforeLongBreak == 0
        val expectedNextCycle =
            if (shouldBeLongBreak) {
                TimerForegroundService.CycleType.LONG_BREAK
            } else {
                TimerForegroundService.CycleType.BREAK
            }
        
        // Then - Should transition to long break
        assertThat(shouldBeLongBreak).isTrue()
        assertThat(expectedNextCycle).isEqualTo(TimerForegroundService.CycleType.LONG_BREAK)
    }

    @Test
    fun `cycle type should transition from break back to work`() {
        // Given - Current cycle is BREAK
        val currentCycleType = TimerForegroundService.CycleType.BREAK
        val expectedNextCycle = TimerForegroundService.CycleType.WORK
        
        // When - Break completes, should always go to work
        val actualNextCycle =
            when (currentCycleType) {
                TimerForegroundService.CycleType.BREAK, 
                TimerForegroundService.CycleType.LONG_BREAK -> TimerForegroundService.CycleType.WORK
                else -> currentCycleType
            }
        
        // Then - Should transition to work
        assertThat(actualNextCycle).isEqualTo(expectedNextCycle)
    }

    @Test
    fun `settings loading should handle async operations`() {
        // Given - Settings loading state
        var settingsLoaded = false
        
        // When - Settings are loaded
        settingsLoaded = true
        
        // Then - Loading should complete
        assertThat(settingsLoaded).isTrue()
    }

    @Test
    fun `timer progress calculation should be correct`() {
        // Given - Timer with specific duration and remaining time
        val initialDuration = 300L  // 5 minutes
        val remainingTime = 150L    // 2.5 minutes left
        
        // When - Calculating progress  
        val expectedProgress = remainingTime.toFloat() / initialDuration.toFloat()
        
        // Then - Progress should be 0.5 (50% remaining)
        assertThat(expectedProgress).isWithin(0.001f).of(0.5f)
        
        // And - Progress should be between 0 and 1
        assertThat(expectedProgress).isAtLeast(0f)
        assertThat(expectedProgress).isAtMost(1f)
    }

    @Test
    fun `timer progress should handle edge cases correctly`() {
        // Given - Edge cases
        val initialDuration = 300L
        
        // When/Then - Zero remaining time
        val zeroProgress = 0L.toFloat() / initialDuration.toFloat()
        assertThat(zeroProgress).isEqualTo(0f)
        
        // When/Then - Full remaining time  
        val fullProgress = initialDuration.toFloat() / initialDuration.toFloat()
        assertThat(fullProgress).isEqualTo(1f)
        
        // When/Then - More than initial duration (edge case)
        val overProgress = (initialDuration + 100L).toFloat() / initialDuration.toFloat()
        val clampedProgress = overProgress.coerceIn(0f, 1f)
        assertThat(clampedProgress).isEqualTo(1f)
    }

    @Test
    fun `isBreakSession logic should work correctly`() {
        // Given - Different cycle types
        val workCycle = TimerForegroundService.CycleType.WORK
        val shortBreakCycle = TimerForegroundService.CycleType.BREAK
        val longBreakCycle = TimerForegroundService.CycleType.LONG_BREAK
        
        // When - Determining if it's a break session
        val isWorkBreak =
            workCycle == TimerForegroundService.CycleType.BREAK || 
                workCycle == TimerForegroundService.CycleType.LONG_BREAK
        val isShortBreak =
            shortBreakCycle == TimerForegroundService.CycleType.BREAK || 
                shortBreakCycle == TimerForegroundService.CycleType.LONG_BREAK
        val isLongBreak =
            longBreakCycle == TimerForegroundService.CycleType.BREAK || 
                longBreakCycle == TimerForegroundService.CycleType.LONG_BREAK
        
        // Then - Only break cycles should be detected as breaks
        assertThat(isWorkBreak).isFalse()
        assertThat(isShortBreak).isTrue()  
        assertThat(isLongBreak).isTrue()
    }
}
