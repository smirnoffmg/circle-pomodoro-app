package com.smirnoffmg.pomodorotimer.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.core.content.getSystemService
import com.smirnoffmg.pomodorotimer.service.TimerForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Dual-timer redundancy system providing bulletproof timer reliability
 * - Primary timer: Foreground service with coroutine countdown
 * - Backup timer: AlarmManager for system-level reliability
 * - Health monitoring: Automatic failover and drift correction
 */
@Singleton
class TimerRedundancyManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context
    ) {
        companion object {
            private const val BACKUP_ALARM_ACTION = "com.smirnoffmg.pomodorotimer.TIMER_BACKUP_ALARM"
            private const val HEALTH_CHECK_ACTION = "com.smirnoffmg.pomodorotimer.TIMER_HEALTH_CHECK"
            private const val FAILOVER_ACTION = "com.smirnoffmg.pomodorotimer.TIMER_FAILOVER"
        
            private const val BACKUP_ALARM_REQUEST_CODE = 2001
            private const val HEALTH_CHECK_REQUEST_CODE = 2002
            private const val FAILOVER_REQUEST_CODE = 2003
        
            private const val HEALTH_CHECK_INTERVAL = 15_000L // 15 seconds
            private const val DRIFT_TOLERANCE_MS = 2_000L // 2 seconds
            private const val FAILOVER_TIMEOUT_MS = 5_000L // 5 seconds
        }

        private val alarmManager: AlarmManager by lazy {
            context.getSystemService() ?: throw IllegalStateException("AlarmManager not available")
        }
    
        private val redundancyScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        // Timer state management
        private val _timerState = MutableStateFlow(TimerRedundancyState.STOPPED)
        val timerState: StateFlow<TimerRedundancyState> = _timerState.asStateFlow()
    
        private val _healthStatus = MutableStateFlow(TimerHealthStatus.HEALTHY)
        val healthStatus: StateFlow<TimerHealthStatus> = _healthStatus.asStateFlow()

        // Timer tracking
        private var timerStartTime: Long = 0L
        private var timerDurationMs: Long = 0L
        private var lastHealthCheck: Long = 0L
        private var primaryTimerAlive = false

        // Pending intents for different alarms
        private var backupAlarmPendingIntent: PendingIntent? = null
        private var healthCheckPendingIntent: PendingIntent? = null
        private var failoverPendingIntent: PendingIntent? = null

        /**
         * Start the dual-timer redundancy system
         */
        fun startTimer(durationMs: Long) {
            // Always cancel any existing alarms and reset state when starting a new timer
            // This prevents stale backup alarms from previous sessions
            cancelAllAlarms()
        
            timerStartTime = System.currentTimeMillis()
            timerDurationMs = durationMs
            primaryTimerAlive = true
            lastHealthCheck = timerStartTime
        
            _timerState.value = TimerRedundancyState.RUNNING
            _healthStatus.value = TimerHealthStatus.HEALTHY
        
            // Start primary timer (handled by service)
            // Set up backup alarm for full duration + safety margin
            scheduleBackupAlarm(durationMs + 5000L) // 5 second safety margin to prevent premature completion
        
            // Start health monitoring
            scheduleHealthCheck()
        
            android.util.Log.d("TimerRedundancy", "Started dual-timer system: ${durationMs}ms")
        }

        /**
         * Pause the timer system
         */
        fun pauseTimer(): Long {
            if (_timerState.value != TimerRedundancyState.RUNNING) return 0L
        
            val elapsed = System.currentTimeMillis() - timerStartTime
            val remaining = (timerDurationMs - elapsed).coerceAtLeast(0L)
        
            _timerState.value = TimerRedundancyState.PAUSED
            primaryTimerAlive = false
        
            cancelAllAlarms()
        
            android.util.Log.d("TimerRedundancy", "Paused timer: ${remaining}ms remaining")
            return remaining
        }

        /**
         * Resume the timer system
         */
        fun resumeTimer(remainingMs: Long) {
            if (_timerState.value != TimerRedundancyState.PAUSED) return
        
            timerStartTime = System.currentTimeMillis()
            timerDurationMs = remainingMs
            primaryTimerAlive = true
            lastHealthCheck = timerStartTime
        
            _timerState.value = TimerRedundancyState.RUNNING
            _healthStatus.value = TimerHealthStatus.HEALTHY
        
            scheduleBackupAlarm(remainingMs + 5000L)
            scheduleHealthCheck()
        
            android.util.Log.d("TimerRedundancy", "Resumed dual-timer system: ${remainingMs}ms")
        }

        /**
         * Stop the timer system completely
         */
        fun stopTimer() {
            _timerState.value = TimerRedundancyState.STOPPED
            _healthStatus.value = TimerHealthStatus.HEALTHY
            primaryTimerAlive = false
        
            cancelAllAlarms()
        
            android.util.Log.d("TimerRedundancy", "Stopped dual-timer system")
        }

        /**
         * Report primary timer heartbeat for health monitoring
         */
        fun reportPrimaryTimerHeartbeat(currentRemainingMs: Long) {
            if (_timerState.value != TimerRedundancyState.RUNNING) return
        
            primaryTimerAlive = true
            lastHealthCheck = System.currentTimeMillis()
        
            // Check for timer drift
            val expectedElapsed = System.currentTimeMillis() - timerStartTime
            val expectedRemaining = (timerDurationMs - expectedElapsed).coerceAtLeast(0L)
            val drift = abs(currentRemainingMs - expectedRemaining)
        
            if (drift > DRIFT_TOLERANCE_MS) {
                _healthStatus.value = TimerHealthStatus.DRIFT_DETECTED
                android.util.Log.w("TimerRedundancy", "Timer drift detected: ${drift}ms")
            
                // Correct drift by updating our reference
                timerStartTime = System.currentTimeMillis() - (timerDurationMs - currentRemainingMs)
            } else if (_healthStatus.value == TimerHealthStatus.DRIFT_DETECTED) {
                _healthStatus.value = TimerHealthStatus.HEALTHY
            }
        }

        /**
         * Handle backup alarm trigger
         */
        fun handleBackupAlarmTrigger() {
            android.util.Log.w("TimerRedundancy", "Backup alarm triggered - checking if primary timer failed")
        
            if (_timerState.value == TimerRedundancyState.RUNNING) {
                // Check if the timer should have completed by now
                val elapsed = System.currentTimeMillis() - timerStartTime
                val expectedRemaining = (timerDurationMs - elapsed).coerceAtLeast(0L)
                
                android.util.Log.d("TimerRedundancy", "Timer state: elapsed=${elapsed}ms, duration=${timerDurationMs}ms, expectedRemaining=${expectedRemaining}ms")
                
                // Only trigger completion if timer should have finished (with 2 second tolerance)
                if (expectedRemaining <= 2000L) {
                    android.util.Log.w("TimerRedundancy", "Primary timer failed - triggering completion")
                    _healthStatus.value = TimerHealthStatus.FAILOVER_ACTIVATED
                
                    // Trigger service to complete timer with API compatibility
                    com.smirnoffmg.pomodorotimer.service.ServiceUtils.startService(
                        context = context,
                        serviceClass = TimerForegroundService::class.java,
                        action = TimerForegroundService.ACTION_TIMER_COMPLETE
                    )
                
                    stopTimer()
                } else {
                    android.util.Log
                        .w("TimerRedundancy", "Backup alarm fired too early - timer still running normally (${expectedRemaining}ms remaining)")
                    // Reschedule backup alarm for remaining time
                    scheduleBackupAlarm(expectedRemaining + 5000L)
                }
            } else {
                android.util.Log.w("TimerRedundancy", "Backup alarm triggered but timer not running - ignoring")
            }
        }

        /**
         * Handle health check trigger
         */
        fun handleHealthCheckTrigger() {
            val now = System.currentTimeMillis()
            val timeSinceLastHeartbeat = now - lastHealthCheck
        
            if (_timerState.value == TimerRedundancyState.RUNNING) {
                if (timeSinceLastHeartbeat > FAILOVER_TIMEOUT_MS) {
                    android.util.Log.w("TimerRedundancy", "Primary timer unresponsive: ${timeSinceLastHeartbeat}ms")
                    _healthStatus.value = TimerHealthStatus.PRIMARY_UNRESPONSIVE
                
                    // Schedule failover alarm
                    scheduleFailoverAlarm()
                } else {
                    // Schedule next health check
                    scheduleHealthCheck()
                }
            }
        }

        /**
         * Handle failover trigger
         */
        fun handleFailoverTrigger() {
            android.util.Log.e("TimerRedundancy", "Failover triggered - restarting timer service")
        
            if (_timerState.value == TimerRedundancyState.RUNNING) {
                _healthStatus.value = TimerHealthStatus.FAILOVER_ACTIVATED
            
                // Calculate remaining time
                val elapsed = System.currentTimeMillis() - timerStartTime
                val remaining = (timerDurationMs - elapsed).coerceAtLeast(0L)
            
                if (remaining > 0) {
                    // Restart service with remaining time and API compatibility
                    com.smirnoffmg.pomodorotimer.service.ServiceUtils.startService(
                        context = context,
                        serviceClass = TimerForegroundService::class.java,
                        action = TimerForegroundService.ACTION_START_TIMER,
                        extras = mapOf(TimerForegroundService.EXTRA_DURATION to (remaining / 1000L))
                    )
                
                    // Reset our tracking
                    timerStartTime = System.currentTimeMillis()
                    timerDurationMs = remaining
                    primaryTimerAlive = true
                    lastHealthCheck = timerStartTime
                
                    scheduleBackupAlarm(remaining + 5000L)
                    scheduleHealthCheck()
                } else {
                    // Timer should have completed, trigger completion
                    handleBackupAlarmTrigger()
                }
            }
        }

        private fun scheduleBackupAlarm(durationMs: Long) {
            cancelBackupAlarm()
        
            val intent =
                Intent(BACKUP_ALARM_ACTION).apply {
                    setPackage(context.packageName)
                }
        
            backupAlarmPendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    BACKUP_ALARM_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        
            val triggerTime = System.currentTimeMillis() + durationMs
            val triggerTimeFormatted = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.ROOT).format(java.util.Date(triggerTime))
            val currentTimeFormatted =
                java.text
                    .SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.ROOT)
                    .format(java.util.Date(System.currentTimeMillis()))
        
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        backupAlarmPendingIntent!!
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        backupAlarmPendingIntent!!
                    )
                }
                android.util.Log
                    .d("TimerRedundancy", "Scheduled backup alarm for ${durationMs}ms (current: $currentTimeFormatted, trigger: $triggerTimeFormatted)")
            } catch (e: SecurityException) {
                android.util.Log.e("TimerRedundancy", "Failed to schedule backup alarm", e)
                _healthStatus.value = TimerHealthStatus.BACKUP_UNAVAILABLE
            }
        }

        private fun scheduleHealthCheck() {
            cancelHealthCheckAlarm()
        
            val intent =
                Intent(HEALTH_CHECK_ACTION).apply {
                    setPackage(context.packageName)
                }
        
            healthCheckPendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    HEALTH_CHECK_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        
            val triggerTime = System.currentTimeMillis() + HEALTH_CHECK_INTERVAL
        
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    healthCheckPendingIntent!!
                )
            } catch (e: SecurityException) {
                android.util.Log.e("TimerRedundancy", "Failed to schedule health check", e)
            }
        }

        private fun scheduleFailoverAlarm() {
            cancelFailoverAlarm()
        
            val intent =
                Intent(FAILOVER_ACTION).apply {
                    setPackage(context.packageName)
                }
        
            failoverPendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    FAILOVER_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        
            val triggerTime = System.currentTimeMillis() + FAILOVER_TIMEOUT_MS
        
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    failoverPendingIntent!!
                )
            } catch (e: SecurityException) {
                android.util.Log.e("TimerRedundancy", "Failed to schedule failover alarm", e)
            }
        }

        private fun cancelBackupAlarm() {
            backupAlarmPendingIntent?.let {
                android.util.Log.d("TimerRedundancy", "Cancelling backup alarm")
                alarmManager.cancel(it)
                backupAlarmPendingIntent = null
            }
        }

        private fun cancelHealthCheckAlarm() {
            healthCheckPendingIntent?.let {
                alarmManager.cancel(it)
                healthCheckPendingIntent = null
            }
        }

        private fun cancelFailoverAlarm() {
            failoverPendingIntent?.let {
                alarmManager.cancel(it)
                failoverPendingIntent = null
            }
        }

        private fun cancelAllAlarms() {
            cancelBackupAlarm()
            cancelHealthCheckAlarm()
            cancelFailoverAlarm()
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
