package com.smirnoffmg.pomodorotimer.timer

interface AlarmScheduler {
    fun scheduleBackupAlarm(durationMs: Long)

    fun scheduleHealthCheck()

    fun scheduleFailoverAlarm()

    fun cancelAllAlarms()

    fun cancelBackupAlarm()

    fun cancelHealthCheckAlarm()

    fun cancelFailoverAlarm()
}
