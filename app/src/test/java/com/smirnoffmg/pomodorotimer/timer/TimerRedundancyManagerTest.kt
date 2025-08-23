package com.smirnoffmg.pomodorotimer.timer

import org.junit.Assert.assertTrue
import org.junit.Test

class TimerRedundancyManagerTest {
    @Test
    fun `health status enum has correct values`() {
        val healthStatuses = TimerHealthStatus.values()
        assertTrue(healthStatuses.contains(TimerHealthStatus.HEALTHY))
        assertTrue(healthStatuses.contains(TimerHealthStatus.DRIFT_DETECTED))
        assertTrue(healthStatuses.contains(TimerHealthStatus.PRIMARY_UNRESPONSIVE))
        assertTrue(healthStatuses.contains(TimerHealthStatus.FAILOVER_ACTIVATED))
        assertTrue(healthStatuses.contains(TimerHealthStatus.BACKUP_UNAVAILABLE))
    }

    @Test
    fun `timer redundancy state enum has correct values`() {
        val states = TimerRedundancyState.values()
        assertTrue(states.contains(TimerRedundancyState.STOPPED))
        assertTrue(states.contains(TimerRedundancyState.RUNNING))
        assertTrue(states.contains(TimerRedundancyState.PAUSED))
    }
}
