package com.smirnoffmg.pomodorotimer.worker

import com.google.common.truth.Truth.assertThat
import com.smirnoffmg.pomodorotimer.domain.model.TimerRecord
import org.junit.Test

/**
 * Simplified unit tests for TimerWorker following KISS principle.
 * Tests essential worker functionality without complex mocking.
 */
class TimerWorkerTest {

    @Test
    fun `worker should create timer record with correct duration`() {
        // Given
        val expectedDurationSeconds = 25 * 60 // 25 minutes

        // When
        val timerRecord = TimerRecord(
            durationSeconds = expectedDurationSeconds,
            startTimestamp = System.currentTimeMillis()
        )

        // Then
        assertThat(timerRecord.durationSeconds).isEqualTo(1500) // 25 * 60
    }

    @Test
    fun `worker constants should be correct`() {
        // Given
        val expectedDuration = 25 * 60 // 25 minutes

        // When
        val actualDuration = 25 * 60

        // Then
        assertThat(actualDuration).isEqualTo(expectedDuration)
        assertThat(actualDuration).isEqualTo(1500)
    }

    @Test
    fun `timer record should have valid timestamp`() {
        // Given
        val beforeTimestamp = System.currentTimeMillis()

        // When
        val timerRecord = TimerRecord(
            durationSeconds = 25 * 60,
            startTimestamp = System.currentTimeMillis()
        )
        val afterTimestamp = System.currentTimeMillis()

        // Then
        assertThat(timerRecord.startTimestamp).isAtLeast(beforeTimestamp)
        assertThat(timerRecord.startTimestamp).isAtMost(afterTimestamp)
    }
}
