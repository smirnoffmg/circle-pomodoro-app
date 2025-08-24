package com.smirnoffmg.pomodorotimer.timer

import kotlinx.coroutines.flow.StateFlow

interface TimerStateManager {
    val timerState: StateFlow<TimerRedundancyState>
    val healthStatus: StateFlow<TimerHealthStatus>

    fun startTimer(durationMs: Long)

    fun pauseTimer(): Long

    fun resumeTimer(remainingMs: Long)

    fun stopTimer()

    fun reportPrimaryTimerHeartbeat(currentRemainingMs: Long)

    // Methods needed by HealthMonitor
    fun getTimerStartTime(): Long

    fun getTimerDurationMs(): Long

    fun getLastHealthCheck(): Long

    fun isPrimaryTimerAlive(): Boolean

    fun setHealthStatus(status: TimerHealthStatus)
}
