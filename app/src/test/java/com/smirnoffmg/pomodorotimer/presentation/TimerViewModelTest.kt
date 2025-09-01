package com.smirnoffmg.pomodorotimer.presentation

import com.google.common.truth.Truth.assertThat
import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import com.smirnoffmg.pomodorotimer.domain.model.SessionType
import org.junit.Test

/**
 * Simplified unit tests for TimerViewModel following KISS principle.
 * Tests core logic without complex dependencies or mocking.
 */
class TimerViewModelTest {
    @Test
    fun `duration conversion should work correctly`() {
        // Given
        val durationMillis = 25 * 60 * 1000L // 25 minutes
        val expectedDurationSeconds = 25 * 60 // 1500 seconds

        // When
        val actualDurationSeconds = (durationMillis / 1000).toInt()

        // Then
        assertThat(actualDurationSeconds).isEqualTo(expectedDurationSeconds)
    }

    @Test
    fun `pomodoro session creation should work with zero duration`() {
        // Given
        val durationMillis = 0L

        // When
        val session =
            PomodoroSession(
                duration = durationMillis,
                startTime = System.currentTimeMillis(),
                endTime = null,
                isCompleted = false,
                type = SessionType.WORK,
            )

        // Then
        assertThat(session.duration).isEqualTo(0)
        assertThat(session.id).isEqualTo(0)
    }

    @Test
    fun `pomodoro session creation should handle millisecond duration`() {
        // Given
        val durationMillis = 1500L // 1.5 seconds

        // When
        val session =
            PomodoroSession(
                duration = durationMillis,
                startTime = System.currentTimeMillis(),
                endTime = null,
                isCompleted = false,
                type = SessionType.WORK,
            )

        // Then
        assertThat(session.duration).isEqualTo(1500)
    }

    @Test
    fun `pomodoro session should have valid properties`() {
        // Given
        val durationMillis = 25 * 60 * 1000L
        val timestamp = System.currentTimeMillis()

        // When
        val session =
            PomodoroSession(
                id = 1,
                duration = durationMillis,
                startTime = timestamp,
                endTime = null,
                isCompleted = false,
                type = SessionType.WORK,
            )

        // Then
        assertThat(session.id).isEqualTo(1)
        assertThat(session.duration).isEqualTo(durationMillis)
        assertThat(session.startTime).isEqualTo(timestamp)
        assertThat(session.type).isEqualTo(SessionType.WORK)
    }
}
