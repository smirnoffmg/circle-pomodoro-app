package com.smirnoffmg.pomodorotimer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smirnoffmg.pomodorotimer.domain.model.TimerRecord
import com.smirnoffmg.pomodorotimer.domain.usecase.AddTimerRecordUseCase
import com.smirnoffmg.pomodorotimer.domain.usecase.GetTimerRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for timer functionality following Single Responsibility Principle.
 * Only handles timer-related UI state and user actions.
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val getTimerRecordsUseCase: GetTimerRecordsUseCase,
    private val addTimerRecordUseCase: AddTimerRecordUseCase,
) : ViewModel() {

    /**
     * Timer records state following KISS principle with simple state management.
     */
    val timerRecords: StateFlow<List<TimerRecord>> =
        getTimerRecordsUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Adds a timer record following Single Responsibility Principle.
     * Only handles the action of adding a record.
     */
    fun addTimerRecord(durationMillis: Long) {
        viewModelScope.launch {
            val timerRecord = createTimerRecord(durationMillis)
            addTimerRecordUseCase(timerRecord)
        }
    }

    /**
     * Creates a timer record following DRY principle.
     * Centralizes timer record creation logic.
     */
    private fun createTimerRecord(durationMillis: Long): TimerRecord =
        TimerRecord(
            durationSeconds = (durationMillis / 1000).toInt(),
            startTimestamp = System.currentTimeMillis(),
        )
}
