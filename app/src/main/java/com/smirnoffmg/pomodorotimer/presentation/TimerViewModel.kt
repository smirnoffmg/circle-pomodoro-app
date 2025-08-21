package com.smirnoffmg.pomodorotimer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smirnoffmg.pomodorotimer.data.db.TimerRecord
import com.smirnoffmg.pomodorotimer.domain.usecase.AddTimerRecordUseCase
import com.smirnoffmg.pomodorotimer.domain.usecase.GetTimerRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val getTimerRecordsUseCase: GetTimerRecordsUseCase,
    private val addTimerRecordUseCase: AddTimerRecordUseCase
) : ViewModel() {

    val timerRecords: StateFlow<List<TimerRecord>> = getTimerRecordsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTimerRecord(duration: Long) {
        viewModelScope.launch {
            addTimerRecordUseCase(
                TimerRecord(
                    timestamp = System.currentTimeMillis(),
                    duration = duration
                )
            )
        }
    }
}
