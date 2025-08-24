package com.smirnoffmg.pomodorotimer.timer

import android.content.Context
import com.smirnoffmg.pomodorotimer.service.ServiceUtils
import com.smirnoffmg.pomodorotimer.service.TimerForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthMonitorImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val timerStateManager: TimerStateManager,
        private val alarmScheduler: AlarmScheduler
    ) : HealthMonitor {
        companion object {
            private const val FAILOVER_TIMEOUT_MS = 5_000L
            private const val COMPLETION_TOLERANCE_MS = 2_000L
        }

        override fun handleBackupAlarmTrigger() {
            if (timerStateManager.timerState.value != TimerRedundancyState.RUNNING) return
        
            val elapsed = System.currentTimeMillis() - timerStateManager.getTimerStartTime()
            val expectedRemaining = (timerStateManager.getTimerDurationMs() - elapsed).coerceAtLeast(0L)
        
            if (expectedRemaining <= COMPLETION_TOLERANCE_MS) {
                timerStateManager.setHealthStatus(TimerHealthStatus.FAILOVER_ACTIVATED)
            
                ServiceUtils.startService(
                    context = context,
                    serviceClass = TimerForegroundService::class.java,
                    action = TimerForegroundService.ACTION_TIMER_COMPLETE
                )
            
                timerStateManager.stopTimer()
            } else {
                alarmScheduler.scheduleBackupAlarm(expectedRemaining)
            }
        }

        override fun handleHealthCheckTrigger() {
            val now = System.currentTimeMillis()
            val timeSinceLastHeartbeat = now - timerStateManager.getLastHealthCheck()
        
            if (timerStateManager.timerState.value == TimerRedundancyState.RUNNING) {
                if (timeSinceLastHeartbeat > FAILOVER_TIMEOUT_MS) {
                    timerStateManager.setHealthStatus(TimerHealthStatus.PRIMARY_UNRESPONSIVE)
                    alarmScheduler.scheduleFailoverAlarm()
                } else {
                    alarmScheduler.scheduleHealthCheck()
                }
            }
        }

        override fun handleFailoverTrigger() {
            if (timerStateManager.timerState.value != TimerRedundancyState.RUNNING) return
        
            timerStateManager.setHealthStatus(TimerHealthStatus.FAILOVER_ACTIVATED)
        
            val elapsed = System.currentTimeMillis() - timerStateManager.getTimerStartTime()
            val remaining = (timerStateManager.getTimerDurationMs() - elapsed).coerceAtLeast(0L)
        
            if (remaining > 0) {
                ServiceUtils.startService(
                    context = context,
                    serviceClass = TimerForegroundService::class.java,
                    action = TimerForegroundService.ACTION_START_TIMER,
                    extras = mapOf(TimerForegroundService.EXTRA_DURATION to (remaining / 1000L))
                )
            
                timerStateManager.resumeTimer(remaining)
                alarmScheduler.scheduleBackupAlarm(remaining)
                alarmScheduler.scheduleHealthCheck()
            } else {
                handleBackupAlarmTrigger()
            }
        }

        override fun checkForDrift(
            currentRemainingMs: Long,
            expectedRemainingMs: Long
        ): Boolean {
            val drift = kotlin.math.abs(currentRemainingMs - expectedRemainingMs)
            return drift > 2_000L
        }
    }
