package com.smirnoffmg.pomodorotimer.timer

import org.junit.Assert.assertTrue
import org.junit.Test

class TimerHealthMonitorTest {
    @Test
    fun `health event types are defined correctly`() {
        val types = HealthEventType.values()
        assertTrue(types.contains(HealthEventType.DRIFT_DETECTED))
        assertTrue(types.contains(HealthEventType.SYSTEM_INTERFERENCE))
        assertTrue(types.contains(HealthEventType.FAILOVER_ACTIVATED))
        assertTrue(types.contains(HealthEventType.SERVICE_LIFECYCLE))
    }

    @Test
    fun `health event severity levels are defined correctly`() {
        val severities = HealthEventSeverity.values()
        assertTrue(severities.contains(HealthEventSeverity.INFO))
        assertTrue(severities.contains(HealthEventSeverity.WARNING))
        assertTrue(severities.contains(HealthEventSeverity.CRITICAL))
    }

    @Test
    fun `system interference types are defined correctly`() {
        val types = SystemInterferenceType.values()
        assertTrue(types.contains(SystemInterferenceType.DOZE_MODE))
        assertTrue(types.contains(SystemInterferenceType.APP_STANDBY))
        assertTrue(types.contains(SystemInterferenceType.BATTERY_OPTIMIZATION))
        assertTrue(types.contains(SystemInterferenceType.PROCESS_KILLED))
        assertTrue(types.contains(SystemInterferenceType.LOW_MEMORY))
    }

    @Test
    fun `recommendation priority levels are defined correctly`() {
        val priorities = RecommendationPriority.values()
        assertTrue(priorities.contains(RecommendationPriority.LOW))
        assertTrue(priorities.contains(RecommendationPriority.MEDIUM))
        assertTrue(priorities.contains(RecommendationPriority.HIGH))
        assertTrue(priorities.contains(RecommendationPriority.CRITICAL))
    }
}
