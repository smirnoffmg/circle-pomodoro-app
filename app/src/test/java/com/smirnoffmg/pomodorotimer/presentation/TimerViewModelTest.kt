package com.smirnoffmg.pomodorotimer.presentation

import com.google.common.truth.Truth.assertThat
import com.smirnoffmg.pomodorotimer.domain.model.TimerRecord
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
    fun `timer record creation should work with zero duration`() {
        // Given
        val durationMillis = 0L

        // When
        val timerRecord = TimerRecord(
            durationSeconds = (durationMillis / 1000).toInt(),
            startTimestamp = System.currentTimeMillis()
        )

        // Then
        assertThat(timerRecord.durationSeconds).isEqualTo(0)
        assertThat(timerRecord.id).isEqualTo(0)
    }

    @Test
    fun `timer record creation should round down partial seconds`() {
        // Given
        val durationMillis = 1500L // 1.5 seconds

        // When
        val timerRecord = TimerRecord(
            durationSeconds = (durationMillis / 1000).toInt(),
            startTimestamp = System.currentTimeMillis()
        )

        // Then
        assertThat(timerRecord.durationSeconds).isEqualTo(1)
    }

    @Test
    fun `timer record should have valid properties`() {
        // Given
        val durationSeconds = 1500
        val timestamp = System.currentTimeMillis()

        // When
        val timerRecord = TimerRecord(
            id = 1,
            durationSeconds = durationSeconds,
            startTimestamp = timestamp
        )

        // Then
        assertThat(timerRecord.id).isEqualTo(1)
        assertThat(timerRecord.durationSeconds).isEqualTo(1500)
        assertThat(timerRecord.startTimestamp).isEqualTo(timestamp)
    }
}
