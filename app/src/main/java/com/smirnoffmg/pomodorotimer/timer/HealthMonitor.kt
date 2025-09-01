package com.smirnoffmg.pomodorotimer.timer

interface HealthMonitor {
    fun handleBackupAlarmTrigger()

    fun handleHealthCheckTrigger()

    fun handleFailoverTrigger()

    fun checkForDrift(
        currentRemainingMs: Long,
        expectedRemainingMs: Long,
    ): Boolean
}
