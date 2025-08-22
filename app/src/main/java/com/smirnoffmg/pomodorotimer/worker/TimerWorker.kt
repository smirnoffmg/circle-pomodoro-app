package com.smirnoffmg.pomodorotimer.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import com.smirnoffmg.pomodorotimer.domain.model.SessionType
import com.smirnoffmg.pomodorotimer.domain.repository.PomodoroRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker for timer-related background tasks following Single Responsibility Principle.
 * Only handles periodic timer record creation.
 */
@HiltWorker
class TimerWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted workerParameters: WorkerParameters,
        private val pomodoroRepository: PomodoroRepository,
    ) : CoroutineWorker(context, workerParameters) {
        companion object {
            private const val DEFAULT_POMODORO_DURATION_MS = 25 * 60 * 1000L // 25 minutes
        }

        override suspend fun doWork(): ListenableWorker.Result =
            try {
                val session = createDefaultPomodoroSession()
                pomodoroRepository.insertSession(session)
                ListenableWorker.Result.success()
            } catch (exception: Exception) {
                ListenableWorker.Result.failure()
            }

        /**
         * Creates a default Pomodoro session following KISS principle.
         * Uses simple, hardcoded values for clarity.
         */
        private fun createDefaultPomodoroSession(): PomodoroSession =
            PomodoroSession(
                startTime = System.currentTimeMillis(),
                endTime = null,
                duration = DEFAULT_POMODORO_DURATION_MS,
                isCompleted = false,
                type = SessionType.WORK,
            )
    }
