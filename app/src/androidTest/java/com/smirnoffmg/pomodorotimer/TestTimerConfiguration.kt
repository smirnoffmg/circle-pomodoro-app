package com.smirnoffmg.pomodorotimer

import com.smirnoffmg.pomodorotimer.domain.model.TimerSettings

/**
 * Test configuration for Pomodoro Timer integration tests.
 * 
 * Provides shortened timer durations for faster testing while maintaining
 * the same logic flow and acceptance criteria verification.
 */
object TestTimerConfiguration {
    /**
     * Test timer settings with shortened durations for fast integration testing.
     * 
     * Real durations:
     * - Work: 25 minutes (1500 seconds)
     * - Short break: 5 minutes (300 seconds)  
     * - Long break: 15 minutes (900 seconds)
     * 
     * Test durations:
     * - Work: 2 seconds
     * - Short break: 3 seconds
     * - Long break: 4 seconds
     */
    val testSettings =
        TimerSettings(
            workDurationMinutes = 0, // Will be overridden to 2 seconds
            shortBreakDurationMinutes = 0, // Will be overridden to 3 seconds
            longBreakDurationMinutes = 0, // Will be overridden to 4 seconds
            sessionsBeforeLongBreak = 4
        )

    /**
     * Get test work duration in seconds.
     */
    fun getTestWorkDurationSeconds(): Long = 2L

    /**
     * Get test short break duration in seconds.
     */
    fun getTestShortBreakDurationSeconds(): Long = 3L

    /**
     * Get test long break duration in seconds.
     */
    fun getTestLongBreakDurationSeconds(): Long = 4L

    /**
     * Get test sessions before long break.
     */
    fun getTestSessionsBeforeLongBreak(): Int = 4

    /**
     * Wait time for work session completion in tests.
     */
    fun getWorkSessionWaitTime(): Long = 2500L // 2.5 seconds to ensure completion

    /**
     * Wait time for break session completion in tests.
     */
    fun getBreakSessionWaitTime(): Long = 3500L // 3.5 seconds to ensure completion

    /**
     * Wait time for long break session completion in tests.
     */
    fun getLongBreakSessionWaitTime(): Long = 4500L // 4.5 seconds to ensure completion
}
