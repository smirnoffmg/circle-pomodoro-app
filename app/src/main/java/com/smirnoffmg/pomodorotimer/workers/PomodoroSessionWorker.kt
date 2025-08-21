package com.smirnoffmg.pomodorotimer.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smirnoffmg.pomodorotimer.data.local.db.dao.PomodoroSessionDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PomodoroSessionWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
        private val pomodoroSessionDao: PomodoroSessionDao,
    ) : CoroutineWorker(appContext, workerParams) {
        override suspend fun doWork(): Result =
            try {
                // Implement your background work here
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
    }
