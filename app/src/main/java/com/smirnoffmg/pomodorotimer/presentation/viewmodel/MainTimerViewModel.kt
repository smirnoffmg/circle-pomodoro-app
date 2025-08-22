package com.smirnoffmg.pomodorotimer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smirnoffmg.pomodorotimer.domain.model.DailyStatistics
import com.smirnoffmg.pomodorotimer.domain.usecase.GetDailyStatisticsUseCase
import com.smirnoffmg.pomodorotimer.service.TimerServiceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainTimerViewModel @Inject constructor(
    private val serviceManager: TimerServiceManager,
    private val getDailyStatisticsUseCase: GetDailyStatisticsUseCase
) : ViewModel() {

    val timerState: StateFlow<TimerState> = serviceManager.timerState
    val remainingTime: StateFlow<Long> = serviceManager.remainingTime
    val progress: StateFlow<Float> = serviceManager.progress

    private val _dailyStatistics = MutableStateFlow(DailyStatistics.empty())
    val dailyStatistics: StateFlow<DailyStatistics> = _dailyStatistics.asStateFlow()

    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

    init {
        loadDailyStatistics()
    }

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

    fun loadDailyStatistics() {
        viewModelScope.launch {
            try {
                val stats = getDailyStatisticsUseCase.getTodayStatistics()
                _dailyStatistics.value = stats
                checkForCelebration(stats)
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }

    private fun checkForCelebration(stats: DailyStatistics) {
        val dailyGoal = 8 // Default daily goal of 8 pomodoros
        if (stats.workSessions >= dailyGoal && !_showCelebration.value) {
            _showCelebration.value = true
            // Auto-hide celebration after 3 seconds
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                _showCelebration.value = false
            }
        }
    }

    fun dismissCelebration() {
        _showCelebration.value = false
    }

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
