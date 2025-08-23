package com.smirnoffmg.pomodorotimer.timer

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Advanced timer health monitoring system
 * Tracks timer accuracy, system interference, and performance metrics
 */
@Singleton
class TimerHealthMonitor
    @Inject
    constructor(
        @ApplicationContext private val context: Context
    ) {
        companion object {
            private const val MAX_HEALTH_EVENTS = 100
            private const val CRITICAL_DRIFT_MS = 5_000L
            private const val WARNING_DRIFT_MS = 1_000L
        }

        // Health event reporting
        private val _healthEvents =
            MutableSharedFlow<TimerHealthEvent>(
                replay = MAX_HEALTH_EVENTS,
                extraBufferCapacity = MAX_HEALTH_EVENTS
            )
        val healthEvents: SharedFlow<TimerHealthEvent> = _healthEvents.asSharedFlow()

        // Performance tracking
        private var totalDriftMs = 0L
        private var maxDriftMs = 0L
        private var driftEventCount = 0
        private var failoverCount = 0
        private var lastAccuracyCheck = 0L

        /**
         * Report timer accuracy measurement
         */
        fun reportAccuracy(
            expectedRemainingMs: Long,
            actualRemainingMs: Long,
            systemTimeMs: Long
        ) {
            val drift = abs(expectedRemainingMs - actualRemainingMs)
        
            if (drift > WARNING_DRIFT_MS) {
                totalDriftMs += drift
                maxDriftMs = maxOf(maxDriftMs, drift)
                driftEventCount++
            
                val severity =
                    when {
                        drift > CRITICAL_DRIFT_MS -> HealthEventSeverity.CRITICAL
                        drift > WARNING_DRIFT_MS -> HealthEventSeverity.WARNING
                        else -> HealthEventSeverity.INFO
                    }
            
                reportHealthEvent(
                    TimerHealthEvent(
                        type = HealthEventType.DRIFT_DETECTED,
                        severity = severity,
                        message = "Timer drift: ${drift}ms (expected: ${expectedRemainingMs}ms, actual: ${actualRemainingMs}ms)",
                        timestamp = systemTimeMs,
                        data =
                            mapOf(
                                "drift_ms" to drift,
                                "expected_ms" to expectedRemainingMs,
                                "actual_ms" to actualRemainingMs
                            )
                    )
                )
            }
        
            lastAccuracyCheck = systemTimeMs
        }

        /**
         * Report system interference event
         */
        fun reportSystemInterference(
            type: SystemInterferenceType,
            details: String,
            impact: Long = 0L
        ) {
            val severity =
                when (type) {
                    SystemInterferenceType.DOZE_MODE -> HealthEventSeverity.WARNING
                    SystemInterferenceType.APP_STANDBY -> HealthEventSeverity.WARNING
                    SystemInterferenceType.BATTERY_OPTIMIZATION -> HealthEventSeverity.CRITICAL
                    SystemInterferenceType.PROCESS_KILLED -> HealthEventSeverity.CRITICAL
                    SystemInterferenceType.LOW_MEMORY -> HealthEventSeverity.WARNING
                }
        
            reportHealthEvent(
                TimerHealthEvent(
                    type = HealthEventType.SYSTEM_INTERFERENCE,
                    severity = severity,
                    message = "System interference: $type - $details",
                    timestamp = System.currentTimeMillis(),
                    data =
                        mapOf(
                            "interference_type" to type.name,
                            "impact_ms" to impact,
                            "device_api" to Build.VERSION.SDK_INT,
                            "device_model" to Build.MODEL
                        )
                )
            )
        }

        /**
         * Report failover activation
         */
        fun reportFailover(
            reason: String,
            recoveryTimeMs: Long,
            success: Boolean
        ) {
            failoverCount++
        
            reportHealthEvent(
                TimerHealthEvent(
                    type = HealthEventType.FAILOVER_ACTIVATED,
                    severity = if (success) HealthEventSeverity.WARNING else HealthEventSeverity.CRITICAL,
                    message = "Failover ${if (success) "successful" else "failed"}: $reason",
                    timestamp = System.currentTimeMillis(),
                    data =
                        mapOf(
                            "reason" to reason,
                            "recovery_time_ms" to recoveryTimeMs,
                            "success" to success,
                            "failover_count" to failoverCount
                        )
                )
            )
        }

        /**
         * Report timer service lifecycle event
         */
        fun reportServiceLifecycle(
            event: ServiceLifecycleEvent,
            details: String = ""
        ) {
            val severity =
                when (event) {
                    ServiceLifecycleEvent.SERVICE_STARTED -> HealthEventSeverity.INFO
                    ServiceLifecycleEvent.SERVICE_STOPPED -> HealthEventSeverity.INFO
                    ServiceLifecycleEvent.SERVICE_KILLED -> HealthEventSeverity.WARNING
                    ServiceLifecycleEvent.SERVICE_RECREATED -> HealthEventSeverity.WARNING
                    ServiceLifecycleEvent.FOREGROUND_REMOVED -> HealthEventSeverity.CRITICAL
                }
        
            reportHealthEvent(
                TimerHealthEvent(
                    type = HealthEventType.SERVICE_LIFECYCLE,
                    severity = severity,
                    message = "Service lifecycle: $event - $details",
                    timestamp = System.currentTimeMillis(),
                    data =
                        mapOf(
                            "lifecycle_event" to event.name,
                            "details" to details
                        )
                )
            )
        }

        /**
         * Get current health statistics
         */
        fun getHealthStatistics(): TimerHealthStatistics {
            val avgDrift = if (driftEventCount > 0) totalDriftMs / driftEventCount else 0L
        
            return TimerHealthStatistics(
                totalDriftMs = totalDriftMs,
                maxDriftMs = maxDriftMs,
                averageDriftMs = avgDrift,
                driftEventCount = driftEventCount,
                failoverCount = failoverCount,
                lastAccuracyCheck = lastAccuracyCheck,
                uptime = System.currentTimeMillis() - lastAccuracyCheck
            )
        }

        /**
         * Reset health statistics
         */
        fun resetStatistics() {
            totalDriftMs = 0L
            maxDriftMs = 0L
            driftEventCount = 0
            failoverCount = 0
            lastAccuracyCheck = System.currentTimeMillis()
        }

        /**
         * Get health recommendations based on current statistics
         */
        fun getHealthRecommendations(): List<HealthRecommendation> {
            val recommendations = mutableListOf<HealthRecommendation>()
            val stats = getHealthStatistics()
        
            if (stats.maxDriftMs > CRITICAL_DRIFT_MS) {
                recommendations.add(
                    HealthRecommendation(
                        priority = RecommendationPriority.HIGH,
                        title = "Critical Timer Drift Detected",
                        description = "Timer has drifted by up to ${stats.maxDriftMs}ms. Consider checking battery optimization settings.",
                        action = "Check battery optimization and background app restrictions"
                    )
                )
            }
        
            if (stats.failoverCount > 3) {
                recommendations.add(
                    HealthRecommendation(
                        priority = RecommendationPriority.HIGH,
                        title = "Frequent Failovers",
                        description = "Timer has failed over ${stats.failoverCount} times. System may be aggressively managing background apps.",
                        action = "Review app permissions and system battery settings"
                    )
                )
            }
        
            if (stats.averageDriftMs > WARNING_DRIFT_MS) {
                recommendations.add(
                    HealthRecommendation(
                        priority = RecommendationPriority.MEDIUM,
                        title = "Timer Accuracy Issues",
                        description = "Average timer drift is ${stats.averageDriftMs}ms. Performance could be improved.",
                        action = "Close unnecessary background apps and ensure stable power supply"
                    )
                )
            }
        
            return recommendations
        }

        private fun reportHealthEvent(event: TimerHealthEvent) {
            _healthEvents.tryEmit(event)
            android.util.Log.d("TimerHealth", "${event.severity}: ${event.message}")
        }
    }

/**
 * Timer health event data class
 */
data class TimerHealthEvent(
    val type: HealthEventType,
    val severity: HealthEventSeverity,
    val message: String,
    val timestamp: Long,
    val data: Map<String, Any> = emptyMap()
)

/**
 * Health event types
 */
enum class HealthEventType {
    DRIFT_DETECTED,
    SYSTEM_INTERFERENCE,
    FAILOVER_ACTIVATED,
    SERVICE_LIFECYCLE
}

/**
 * Health event severity levels
 */
enum class HealthEventSeverity {
    INFO,
    WARNING,
    CRITICAL
}

/**
 * System interference types
 */
enum class SystemInterferenceType {
    DOZE_MODE,
    APP_STANDBY,
    BATTERY_OPTIMIZATION,
    PROCESS_KILLED,
    LOW_MEMORY
}

/**
 * Service lifecycle events
 */
enum class ServiceLifecycleEvent {
    SERVICE_STARTED,
    SERVICE_STOPPED,
    SERVICE_KILLED,
    SERVICE_RECREATED,
    FOREGROUND_REMOVED
}

/**
 * Timer health statistics
 */
data class TimerHealthStatistics(
    val totalDriftMs: Long,
    val maxDriftMs: Long,
    val averageDriftMs: Long,
    val driftEventCount: Int,
    val failoverCount: Int,
    val lastAccuracyCheck: Long,
    val uptime: Long
)

/**
 * Health recommendation
 */
data class HealthRecommendation(
    val priority: RecommendationPriority,
    val title: String,
    val description: String,
    val action: String
)

/**
 * Recommendation priority levels
 */
enum class RecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
