package com.smirnoffmg.pomodorotimer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smirnoffmg.pomodorotimer.service.TimerServiceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainTimerViewModel @Inject constructor(
    private val serviceManager: TimerServiceManager
) : ViewModel() {

    val timerState: StateFlow<TimerState> = serviceManager.timerState
    val remainingTime: StateFlow<Long> = serviceManager.remainingTime
    val progress: StateFlow<Float> = serviceManager.progress

    fun startTimer() {
        serviceManager.startTimer()
    }

    fun pauseTimer() {
        serviceManager.pauseTimer()
    }

    fun stopTimer() {
        serviceManager.stopTimer()
    }

    fun setTimerDuration(durationInSeconds: Long) {
        serviceManager.setTimerDuration(durationInSeconds)
    }

    fun isServiceRunning(): Boolean = serviceManager.isServiceRunning()

    override fun onCleared() {
        super.onCleared()
        serviceManager.cleanup()
    }
}

enum class TimerState {
    RUNNING,
    PAUSED,
    STOPPED
}
