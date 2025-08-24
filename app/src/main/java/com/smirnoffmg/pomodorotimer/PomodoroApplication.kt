package com.smirnoffmg.pomodorotimer

import android.app.Application
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.smirnoffmg.pomodorotimer.notification.NotificationChannelManager
import com.smirnoffmg.pomodorotimer.worker.TimerWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class PomodoroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupNotificationChannels()
        // TODO: Fix WorkManager Hilt integration
        // setupWorkManager()
    }

    private fun setupNotificationChannels() {
        // Create notification channels directly without DI to avoid initialization issues
        val channelManager = NotificationChannelManager(this)
        channelManager.createNotificationChannels()
    }

    private fun setupWorkManager() {
        val workRequest =
            PeriodicWorkRequestBuilder<TimerWorker>(15, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }
}
