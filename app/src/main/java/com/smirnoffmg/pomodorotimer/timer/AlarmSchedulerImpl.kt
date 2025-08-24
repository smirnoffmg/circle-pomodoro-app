package com.smirnoffmg.pomodorotimer.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmSchedulerImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context
    ) : AlarmScheduler {
        companion object {
            private const val BACKUP_ALARM_ACTION = "com.smirnoffmg.pomodorotimer.TIMER_BACKUP_ALARM"
            private const val HEALTH_CHECK_ACTION = "com.smirnoffmg.pomodorotimer.TIMER_HEALTH_CHECK"
            private const val FAILOVER_ACTION = "com.smirnoffmg.pomodorotimer.TIMER_FAILOVER"
        
            private const val BACKUP_ALARM_REQUEST_CODE = 2001
            private const val HEALTH_CHECK_REQUEST_CODE = 2002
            private const val FAILOVER_REQUEST_CODE = 2003
        
            private const val HEALTH_CHECK_INTERVAL = 15_000L
            private const val FAILOVER_TIMEOUT_MS = 5_000L
            private const val SAFETY_MARGIN_MS = 5_000L
        }

        private val alarmManager: AlarmManager by lazy {
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager? 
                ?: throw IllegalStateException("AlarmManager not available")
        }

        private var backupAlarmPendingIntent: PendingIntent? = null
        private var healthCheckPendingIntent: PendingIntent? = null
        private var failoverPendingIntent: PendingIntent? = null

        override fun scheduleBackupAlarm(durationMs: Long) {
            cancelBackupAlarm()
        
            val intent =
                Intent(BACKUP_ALARM_ACTION).apply {
                    setPackage(context.packageName)
                }
        
            backupAlarmPendingIntent =
                createPendingIntent(
                    context,
                    BACKUP_ALARM_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        
            val triggerTime = System.currentTimeMillis() + durationMs + SAFETY_MARGIN_MS
            scheduleExactAlarm(triggerTime, backupAlarmPendingIntent!!)
        }

        override fun scheduleHealthCheck() {
            cancelHealthCheckAlarm()
        
            val intent =
                Intent(HEALTH_CHECK_ACTION).apply {
                    setPackage(context.packageName)
                }
        
            healthCheckPendingIntent =
                createPendingIntent(
                    context,
                    HEALTH_CHECK_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        
            val triggerTime = System.currentTimeMillis() + HEALTH_CHECK_INTERVAL
            scheduleExactAlarm(triggerTime, healthCheckPendingIntent!!)
        }

        override fun scheduleFailoverAlarm() {
            cancelFailoverAlarm()
        
            val intent =
                Intent(FAILOVER_ACTION).apply {
                    setPackage(context.packageName)
                }
        
            failoverPendingIntent =
                createPendingIntent(
                    context,
                    FAILOVER_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        
            val triggerTime = System.currentTimeMillis() + FAILOVER_TIMEOUT_MS
            scheduleExactAlarm(triggerTime, failoverPendingIntent!!)
        }

        override fun cancelAllAlarms() {
            cancelBackupAlarm()
            cancelHealthCheckAlarm()
            cancelFailoverAlarm()
        }

        override fun cancelBackupAlarm() {
            backupAlarmPendingIntent?.let {
                alarmManager.cancel(it)
                backupAlarmPendingIntent = null
            }
        }

        override fun cancelHealthCheckAlarm() {
            healthCheckPendingIntent?.let {
                alarmManager.cancel(it)
                healthCheckPendingIntent = null
            }
        }

        override fun cancelFailoverAlarm() {
            failoverPendingIntent?.let {
                alarmManager.cancel(it)
                failoverPendingIntent = null
            }
        }

        private fun createPendingIntent(
            context: Context,
            requestCode: Int,
            intent: Intent,
            flags: Int
        ): PendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        private fun scheduleExactAlarm(
            triggerTime: Long,
            pendingIntent: PendingIntent
        ) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                throw IllegalStateException("Failed to schedule alarm", e)
            }
        }
    }
