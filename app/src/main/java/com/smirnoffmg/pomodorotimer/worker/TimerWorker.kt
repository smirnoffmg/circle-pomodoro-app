package com.smirnoffmg.pomodorotimer.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smirnoffmg.pomodorotimer.data.repository.TimerRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TimerWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TimerRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("TimerWorker", "WorkManager is running")
        // Here you can add any background task, for example, fetching data from a server
        // and storing it in the database using the repository.
        return Result.success()
    }
}
