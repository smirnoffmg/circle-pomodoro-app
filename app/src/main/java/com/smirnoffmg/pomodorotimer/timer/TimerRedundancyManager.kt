package com.smirnoffmg.pomodorotimer.timer

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dual-timer redundancy system orchestrator providing bulletproof timer reliability
 * - Primary timer: Foreground service with coroutine countdown
 * - Backup timer: AlarmManager for system-level reliability
 * - Health monitoring: Automatic failover and drift correction
 * 
 * Follows SOLID principles through dependency injection and interface segregation
 */
@Singleton
class TimerRedundancyManager
    @Inject
    constructor(
        private val timerStateManager: TimerStateManager,
        private val alarmScheduler: AlarmScheduler,
        private val healthMonitor: HealthMonitor
    ) {
        // Expose state flows from timer state manager
        val timerState = timerStateManager.timerState
        val healthStatus = timerStateManager.healthStatus

        /**
         * Start the dual-timer redundancy system
         */
        fun startTimer(durationMs: Long) {
            alarmScheduler.cancelAllAlarms()
            timerStateManager.startTimer(durationMs)
            alarmScheduler.scheduleBackupAlarm(durationMs)
            alarmScheduler.scheduleHealthCheck()
        }

        /**
         * Pause the timer system
         */
        fun pauseTimer(): Long {
            val remaining = timerStateManager.pauseTimer()
            alarmScheduler.cancelAllAlarms()
            return remaining
        }

        /**
         * Resume the timer system
         */
        fun resumeTimer(remainingMs: Long) {
            timerStateManager.resumeTimer(remainingMs)
            alarmScheduler.scheduleBackupAlarm(remainingMs)
            alarmScheduler.scheduleHealthCheck()
        }

        /**
         * Stop the timer system completely
         */
        fun stopTimer() {
            timerStateManager.stopTimer()
            alarmScheduler.cancelAllAlarms()
        }

        /**
         * Report primary timer heartbeat for health monitoring
         */
        fun reportPrimaryTimerHeartbeat(currentRemainingMs: Long) {
            timerStateManager.reportPrimaryTimerHeartbeat(currentRemainingMs)
        }

        /**
         * Handle backup alarm trigger
         */
        fun handleBackupAlarmTrigger() {
            healthMonitor.handleBackupAlarmTrigger()
        }

        /**
         * Handle health check trigger
         */
        fun handleHealthCheckTrigger() {
            healthMonitor.handleHealthCheckTrigger()
        }

        /**
         * Handle failover trigger
         */
        fun handleFailoverTrigger() {
            healthMonitor.handleFailoverTrigger()
        }
    }

enum class TimerRedundancyState {
    STOPPED,
    RUNNING,
    PAUSED
}

enum class TimerHealthStatus {
    HEALTHY,
    DRIFT_DETECTED,
    PRIMARY_UNRESPONSIVE,
    FAILOVER_ACTIVATED,
    BACKUP_UNAVAILABLE
}
