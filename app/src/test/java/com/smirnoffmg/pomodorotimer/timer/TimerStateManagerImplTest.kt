package com.smirnoffmg.pomodorotimer.timer

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TimerStateManagerImplTest {
    private lateinit var timerStateManager: TimerStateManagerImpl

    @Before
    fun setUp() {
        timerStateManager = TimerStateManagerImpl()
    }

    @Test
    fun `startTimer should set state to RUNNING and health to HEALTHY`() =
        runTest {
            // Given
            val durationMs = 25 * 60 * 1000L

            // When
            timerStateManager.startTimer(durationMs)

            // Then
            assertEquals(TimerRedundancyState.RUNNING, timerStateManager.timerState.first())
            assertEquals(TimerHealthStatus.HEALTHY, timerStateManager.healthStatus.first())
            assertTrue(timerStateManager.isPrimaryTimerAlive())
        }

    @Test
    fun `pauseTimer should set state to PAUSED and return remaining time`() =
        runTest {
            // Given
            val durationMs = 25 * 60 * 1000L
            timerStateManager.startTimer(durationMs)
        
            // Simulate time passing
            Thread.sleep(1000)

            // When
            val remaining = timerStateManager.pauseTimer()

            // Then
            assertEquals(TimerRedundancyState.PAUSED, timerStateManager.timerState.first())
            assertTrue(remaining > 0)
            assertTrue(remaining < durationMs)
            assertFalse(timerStateManager.isPrimaryTimerAlive())
        }

    @Test
    fun `pauseTimer should return 0 when not running`() =
        runTest {
            // When
            val remaining = timerStateManager.pauseTimer()

            // Then
            assertEquals(0L, remaining)
        }

    @Test
    fun `resumeTimer should set state back to RUNNING`() =
        runTest {
            // Given
            val durationMs = 25 * 60 * 1000L
            timerStateManager.startTimer(durationMs)
            timerStateManager.pauseTimer()
            val remaining = 20 * 60 * 1000L

            // When
            timerStateManager.resumeTimer(remaining)

            // Then
            assertEquals(TimerRedundancyState.RUNNING, timerStateManager.timerState.first())
            assertEquals(TimerHealthStatus.HEALTHY, timerStateManager.healthStatus.first())
            assertTrue(timerStateManager.isPrimaryTimerAlive())
        }

    @Test
    fun `resumeTimer should not change state when not paused`() =
        runTest {
            // Given
            val durationMs = 25 * 60 * 1000L
            timerStateManager.startTimer(durationMs)
            val originalState = timerStateManager.timerState.first()

            // When
            timerStateManager.resumeTimer(20 * 60 * 1000L)

            // Then
            assertEquals(originalState, timerStateManager.timerState.first())
        }

    @Test
    fun `stopTimer should set state to STOPPED`() =
        runTest {
            // Given
            val durationMs = 25 * 60 * 1000L
            timerStateManager.startTimer(durationMs)

            // When
            timerStateManager.stopTimer()

            // Then
            assertEquals(TimerRedundancyState.STOPPED, timerStateManager.timerState.first())
            assertEquals(TimerHealthStatus.HEALTHY, timerStateManager.healthStatus.first())
            assertFalse(timerStateManager.isPrimaryTimerAlive())
        }

    @Test
    fun `reportPrimaryTimerHeartbeat should detect drift`() =
        runTest {
            // Given
            val durationMs = 25 * 60 * 1000L
            timerStateManager.startTimer(durationMs)
        
            // Simulate significant drift (3 seconds)
            val driftedRemaining = durationMs - 3000L

            // When
            timerStateManager.reportPrimaryTimerHeartbeat(driftedRemaining)

            // Then
            assertEquals(TimerHealthStatus.DRIFT_DETECTED, timerStateManager.healthStatus.first())
            assertTrue(timerStateManager.isPrimaryTimerAlive())
        }

    @Test
    fun `reportPrimaryTimerHeartbeat should maintain healthy status when no drift`() =
        runTest {
            // Given
            val durationMs = 25 * 60 * 1000L
            timerStateManager.startTimer(durationMs)
        
            // Simulate minimal drift (1 second)
            val minimalDriftRemaining = durationMs - 1000L

            // When
            timerStateManager.reportPrimaryTimerHeartbeat(minimalDriftRemaining)

            // Then
            assertEquals(TimerHealthStatus.HEALTHY, timerStateManager.healthStatus.first())
        }

    @Test
    fun `reportPrimaryTimerHeartbeat should not change state when not running`() =
        runTest {
            // Given
            val originalState = timerStateManager.timerState.first()

            // When
            timerStateManager.reportPrimaryTimerHeartbeat(1000L)

            // Then
            assertEquals(originalState, timerStateManager.timerState.first())
            assertFalse(timerStateManager.isPrimaryTimerAlive())
        }

    @Test
    fun `getTimerStartTime should return correct value`() {
        // Given
        val durationMs = 25 * 60 * 1000L
        timerStateManager.startTimer(durationMs)

        // When
        val startTime = timerStateManager.getTimerStartTime()

        // Then
        assertTrue(startTime > 0)
    }

    @Test
    fun `getTimerDurationMs should return correct value`() {
        // Given
        val durationMs = 25 * 60 * 1000L
        timerStateManager.startTimer(durationMs)

        // When
        val actualDuration = timerStateManager.getTimerDurationMs()

        // Then
        assertEquals(durationMs, actualDuration)
    }

    @Test
    fun `setHealthStatus should update health status`() =
        runTest {
            // When
            timerStateManager.setHealthStatus(TimerHealthStatus.DRIFT_DETECTED)

            // Then
            assertEquals(TimerHealthStatus.DRIFT_DETECTED, timerStateManager.healthStatus.first())
        }
}
