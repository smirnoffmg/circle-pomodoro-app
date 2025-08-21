package com.smirnoffmg.pomodorotimer

import android.app.Application
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.smirnoffmg.pomodorotimer.worker.TimerWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class PomodoroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupWorkManager()
    }

    private fun setupWorkManager() {
        val workRequest =
            PeriodicWorkRequestBuilder<TimerWorker>(15, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }
}
