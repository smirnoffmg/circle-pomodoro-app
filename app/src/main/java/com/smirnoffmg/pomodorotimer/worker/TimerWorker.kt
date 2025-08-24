package com.smirnoffmg.pomodorotimer.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

/**
 * Worker for timer-related background tasks following Single Responsibility Principle.
 * Only handles periodic timer record creation.
 * Simplified to avoid Hilt dependency injection issues.
 */
class TimerWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {
    companion object {
        private const val DEFAULT_POMODORO_DURATION_MS = 25 * 60 * 1000L // 25 minutes
    }

    override suspend fun doWork(): ListenableWorker.Result =
        try {
            // For now, just log that the worker ran successfully
            // TODO: Implement session creation when repository is available
            android.util.Log.d("TimerWorker", "Timer worker executed successfully")
            ListenableWorker.Result.success()
        } catch (exception: Exception) {
            android.util.Log.e("TimerWorker", "Timer worker failed: ${exception.message}")
            ListenableWorker.Result.failure()
        }
}
