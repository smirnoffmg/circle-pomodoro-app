package com.smirnoffmg.pomodorotimer.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.smirnoffmg.pomodorotimer.domain.model.TimerRecord
import com.smirnoffmg.pomodorotimer.domain.usecase.AddTimerRecordUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker for timer-related background tasks following Single Responsibility Principle.
 * Only handles periodic timer record creation.
 */
@HiltWorker
class TimerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val addTimerRecordUseCase: AddTimerRecordUseCase,
) : CoroutineWorker(context, workerParameters) {

    companion object {
        private const val DEFAULT_POMODORO_DURATION_SECONDS = 25 * 60 // 25 minutes
    }

    override suspend fun doWork(): ListenableWorker.Result =
        try {
            val timerRecord = createDefaultTimerRecord()
            addTimerRecordUseCase(timerRecord)
            ListenableWorker.Result.success()
        } catch (exception: Exception) {
            ListenableWorker.Result.failure()
        }

    /**
     * Creates a default timer record following KISS principle.
     * Uses simple, hardcoded values for clarity.
     */
    private fun createDefaultTimerRecord(): TimerRecord =
        TimerRecord(
            durationSeconds = DEFAULT_POMODORO_DURATION_SECONDS,
            startTimestamp = System.currentTimeMillis(),
        )
}
