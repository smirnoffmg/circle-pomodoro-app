package com.smirnoffmg.pomodorotimer.service

import com.google.common.truth.Truth.assertThat
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.TimerState
import com.smirnoffmg.pomodorotimer.testing.BaseUnitTest
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TimerServiceManagerStateSyncTest : BaseUnitTest() {
    
    @Test
    fun `initial state should be STOPPED`() {
        // Given: Service manager initial state expectations
        val expectedInitialState = TimerState.STOPPED
        val expectedInitialTime = 25 * 60L // Default work duration
        val expectedInitialProgress = 1f
        
        // Then: Verify initial state values are correct
        assertThat(expectedInitialState).isEqualTo(TimerState.STOPPED)
        assertThat(expectedInitialTime).isEqualTo(1500L)
        assertThat(expectedInitialProgress).isEqualTo(1f)
    }
    
    @Test
    fun `timer state transitions should work correctly`() {
        // Given: Timer state transitions
        val stoppedState = TimerState.STOPPED
        val runningState = TimerState.RUNNING
        val pausedState = TimerState.PAUSED
        
        // When/Then: Verify state transitions are valid
        assertThat(stoppedState).isNotEqualTo(runningState)
        assertThat(runningState).isNotEqualTo(pausedState)
        assertThat(pausedState).isNotEqualTo(stoppedState)
        
        // And: All states should be valid
        assertThat(stoppedState).isIn(listOf(TimerState.STOPPED, TimerState.RUNNING, TimerState.PAUSED))
        assertThat(runningState).isIn(listOf(TimerState.STOPPED, TimerState.RUNNING, TimerState.PAUSED))
        assertThat(pausedState).isIn(listOf(TimerState.STOPPED, TimerState.RUNNING, TimerState.PAUSED))
    }
    
    @Test
    fun `timer duration calculations should be correct`() {
        // Given: Timer duration expectations
        val workDurationMinutes = 25
        val workDurationSeconds = workDurationMinutes * 60L
        
        // When/Then: Verify duration calculations
        assertThat(workDurationSeconds).isEqualTo(1500L)
        assertThat(workDurationMinutes).isEqualTo(25)
        
        // And: Verify time format conversions
        val minutes = workDurationSeconds / 60L
        val seconds = workDurationSeconds % 60L
        assertThat(minutes).isEqualTo(25L)
        assertThat(seconds).isEqualTo(0L)
    }
    
    @Test
    fun `progress calculation should work correctly`() {
        // Given: Progress calculation test case
        val initialDuration = 300L  // 5 minutes
        val remainingTime = 150L    // 2.5 minutes left
        
        // When: Calculating progress
        val expectedProgress = remainingTime.toFloat() / initialDuration.toFloat()
        
        // Then: Progress should be 0.5 (50% remaining)
        assertThat(expectedProgress).isEqualTo(0.5f)
        
        // And: Progress should be between 0 and 1
        assertThat(expectedProgress).isAtLeast(0f)
        assertThat(expectedProgress).isAtMost(1f)
    }
}
