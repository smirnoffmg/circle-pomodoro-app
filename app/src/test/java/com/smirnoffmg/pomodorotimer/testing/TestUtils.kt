package com.smirnoffmg.pomodorotimer.testing

import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import com.smirnoffmg.pomodorotimer.domain.model.SessionType
import com.smirnoffmg.pomodorotimer.domain.model.TimerRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Test utilities following DRY and KISS principles.
 * Provides common testing functionality to avoid code duplication.
 */
object TestUtils {
    
    /**
     * Creates a test PomodoroSession with default values.
     * Follows KISS principle by providing sensible defaults.
     */
    fun createTestPomodoroSession(
        id: Long = 1L,
        startTime: Long = System.currentTimeMillis(),
        endTime: Long? = null,
        duration: Long = 25 * 60 * 1000, // 25 minutes
        isCompleted: Boolean = false,
        type: SessionType = SessionType.WORK
    ): PomodoroSession = PomodoroSession(
        id = id,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        isCompleted = isCompleted,
        type = type
    )

    /**
     * Creates a test TimerRecord with default values.
     * Follows KISS principle by providing sensible defaults.
     */
    fun createTestTimerRecord(
        id: Int = 1,
        durationSeconds: Int = 25 * 60, // 25 minutes
        startTimestamp: Long = System.currentTimeMillis()
    ): TimerRecord = TimerRecord(
        id = id,
        durationSeconds = durationSeconds,
        startTimestamp = startTimestamp
    )

    /**
     * Creates a list of test PomodoroSessions.
     * Follows DRY principle by reusing the single creation method.
     */
    fun createTestPomodoroSessions(count: Int): List<PomodoroSession> =
        (1..count).map { index ->
            createTestPomodoroSession(
                id = index.toLong(),
                startTime = System.currentTimeMillis() - (index * 25 * 60 * 1000)
            )
        }

    /**
     * Creates a list of test TimerRecords.
     * Follows DRY principle by reusing the single creation method.
     */
    fun createTestTimerRecords(count: Int): List<TimerRecord> =
        (1..count).map { index ->
            createTestTimerRecord(
                id = index,
                startTimestamp = System.currentTimeMillis() - (index * 25 * 60 * 1000)
            )
        }
}

/**
 * Test dispatcher utilities for coroutine testing.
 * Follows Single Responsibility Principle by only handling dispatcher setup.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object TestDispatchers {
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    fun setMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }
}
