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

@HiltWorker
class TimerWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted workerParameters: WorkerParameters,
        private val addTimerRecordUseCase: AddTimerRecordUseCase,
    ) : CoroutineWorker(context, workerParameters) {
        override suspend fun doWork(): ListenableWorker.Result =
            try {
                val record =
                    TimerRecord(
                        durationSeconds = 25 * 60,
                        startTimestamp = System.currentTimeMillis(),
                    )
                addTimerRecordUseCase(record)
                ListenableWorker.Result.success()
            } catch (e: Exception) {
                ListenableWorker.Result.failure()
            }
    }
