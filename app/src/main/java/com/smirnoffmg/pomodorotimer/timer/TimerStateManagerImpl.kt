package com.smirnoffmg.pomodorotimer.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class TimerStateManagerImpl
    @Inject
    constructor() : TimerStateManager {
        private val _timerState = MutableStateFlow(TimerRedundancyState.STOPPED)
        override val timerState: StateFlow<TimerRedundancyState> = _timerState.asStateFlow()
    
        private val _healthStatus = MutableStateFlow(TimerHealthStatus.HEALTHY)
        override val healthStatus: StateFlow<TimerHealthStatus> = _healthStatus.asStateFlow()

        private var timerStartTime: Long = 0L
        private var timerDurationMs: Long = 0L
        private var lastHealthCheck: Long = 0L
        private var primaryTimerAlive = false

        companion object {
            private const val DRIFT_TOLERANCE_MS = 2_000L
        }

        override fun startTimer(durationMs: Long) {
            timerStartTime = System.currentTimeMillis()
            timerDurationMs = durationMs
            primaryTimerAlive = true
            lastHealthCheck = timerStartTime
        
            _timerState.value = TimerRedundancyState.RUNNING
            _healthStatus.value = TimerHealthStatus.HEALTHY
        }

        override fun pauseTimer(): Long {
            if (_timerState.value != TimerRedundancyState.RUNNING) return 0L
        
            val elapsed = System.currentTimeMillis() - timerStartTime
            val remaining = (timerDurationMs - elapsed).coerceAtLeast(0L)
        
            _timerState.value = TimerRedundancyState.PAUSED
            primaryTimerAlive = false
        
            return remaining
        }

        override fun resumeTimer(remainingMs: Long) {
            if (_timerState.value != TimerRedundancyState.PAUSED) return
        
            timerStartTime = System.currentTimeMillis()
            timerDurationMs = remainingMs
            primaryTimerAlive = true
            lastHealthCheck = timerStartTime
        
            _timerState.value = TimerRedundancyState.RUNNING
            _healthStatus.value = TimerHealthStatus.HEALTHY
        }

        override fun stopTimer() {
            _timerState.value = TimerRedundancyState.STOPPED
            _healthStatus.value = TimerHealthStatus.HEALTHY
            primaryTimerAlive = false
        }

        override fun reportPrimaryTimerHeartbeat(currentRemainingMs: Long) {
            if (_timerState.value != TimerRedundancyState.RUNNING) return
        
            primaryTimerAlive = true
            lastHealthCheck = System.currentTimeMillis()
        
            val expectedElapsed = System.currentTimeMillis() - timerStartTime
            val expectedRemaining = (timerDurationMs - expectedElapsed).coerceAtLeast(0L)
            val drift = abs(currentRemainingMs - expectedRemaining)
        
            if (drift > DRIFT_TOLERANCE_MS) {
                _healthStatus.value = TimerHealthStatus.DRIFT_DETECTED
                timerStartTime = System.currentTimeMillis() - (timerDurationMs - currentRemainingMs)
            } else if (_healthStatus.value == TimerHealthStatus.DRIFT_DETECTED) {
                _healthStatus.value = TimerHealthStatus.HEALTHY
            }
        }

        override fun getTimerStartTime(): Long = timerStartTime

        override fun getTimerDurationMs(): Long = timerDurationMs

        override fun getLastHealthCheck(): Long = lastHealthCheck

        override fun isPrimaryTimerAlive(): Boolean = primaryTimerAlive

        override fun setHealthStatus(status: TimerHealthStatus) {
            _healthStatus.value = status
        }
    }
