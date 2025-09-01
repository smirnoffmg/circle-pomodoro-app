package com.smirnoffmg.pomodorotimer.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

/**
 * Broadcast receiver for timer redundancy system alarms
 * Handles backup alarms, health checks, and failover triggers
 */
class TimerRedundancyReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        // Get dependencies through EntryPoint since BroadcastReceivers can't use @Inject directly
        val entryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                TimerRedundancyEntryPoint::class.java,
            )
        val redundancyManager = entryPoint.redundancyManager()

        when (intent.action) {
            "com.smirnoffmg.pomodorotimer.TIMER_BACKUP_ALARM" -> {
                android.util.Log.i("TimerRedundancy", "Backup alarm received")
                redundancyManager.handleBackupAlarmTrigger()
            }

            "com.smirnoffmg.pomodorotimer.TIMER_HEALTH_CHECK" -> {
                android.util.Log.d("TimerRedundancy", "Health check alarm received")
                redundancyManager.handleHealthCheckTrigger()
            }

            "com.smirnoffmg.pomodorotimer.TIMER_FAILOVER" -> {
                android.util.Log.w("TimerRedundancy", "Failover alarm received")
                redundancyManager.handleFailoverTrigger()
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                android.util.Log.i("TimerRedundancy", "Boot completed - checking for timer recovery")
                // TODO: Implement timer state persistence and recovery after boot
            }

            "android.intent.action.MY_PACKAGE_REPLACED",
            "android.intent.action.PACKAGE_REPLACED",
            -> {
                android.util.Log.i("TimerRedundancy", "App updated - checking for timer recovery")
                // TODO: Implement timer state recovery after app update
            }

            else -> {
                android.util.Log.d("TimerRedundancy", "Unknown action: ${intent.action}")
            }
        }
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface TimerRedundancyEntryPoint {
    fun redundancyManager(): TimerRedundancyManager
}
