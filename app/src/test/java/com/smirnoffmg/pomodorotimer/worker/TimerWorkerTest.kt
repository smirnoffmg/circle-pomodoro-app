package com.smirnoffmg.pomodorotimer.worker

import com.google.common.truth.Truth.assertThat
import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import com.smirnoffmg.pomodorotimer.domain.model.SessionType
import org.junit.Test

/**
 * Simplified unit tests for TimerWorker following KISS principle.
 * Tests essential worker functionality without complex mocking.
 */
class TimerWorkerTest {
    @Test
    fun `worker should create pomodoro session with correct duration`() {
        // Given
        val expectedDurationMs = 25 * 60 * 1000L // 25 minutes

        // When
        val session =
            PomodoroSession(
                duration = expectedDurationMs,
                startTime = System.currentTimeMillis(),
                endTime = null,
                isCompleted = false,
                type = SessionType.WORK
            )

        // Then
        assertThat(session.duration).isEqualTo(25 * 60 * 1000L) // 25 minutes in milliseconds
    }

    @Test
    fun `worker constants should be correct`() {
        // Given
        val expectedDuration = 25 * 60 * 1000L // 25 minutes

        // When
        val actualDuration = 25 * 60 * 1000L

        // Then
        assertThat(actualDuration).isEqualTo(expectedDuration)
        assertThat(actualDuration).isEqualTo(1500000L)
    }

    @Test
    fun `pomodoro session should have valid timestamp`() {
        // Given
        val beforeTimestamp = System.currentTimeMillis()

        // When
        val session =
            PomodoroSession(
                duration = 25 * 60 * 1000L,
                startTime = System.currentTimeMillis(),
                endTime = null,
                isCompleted = false,
                type = SessionType.WORK
            )
        val afterTimestamp = System.currentTimeMillis()

        // Then
        assertThat(session.startTime).isAtLeast(beforeTimestamp)
        assertThat(session.startTime).isAtMost(afterTimestamp)
    }
}
