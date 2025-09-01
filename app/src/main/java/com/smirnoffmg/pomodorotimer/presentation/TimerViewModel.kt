package com.smirnoffmg.pomodorotimer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import com.smirnoffmg.pomodorotimer.domain.model.SessionType
import com.smirnoffmg.pomodorotimer.domain.repository.PomodoroRepository
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
class TimerViewModel
    @Inject
    constructor(
        private val pomodoroRepository: PomodoroRepository,
    ) : ViewModel() {
        /**
         * Pomodoro sessions state following KISS principle with simple state management.
         */
        val pomodoroSessions: StateFlow<List<PomodoroSession>> =
            pomodoroRepository
                .getAllSessions()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList(),
                )

        /**
         * Adds a Pomodoro session following Single Responsibility Principle.
         * Only handles the action of adding a session.
         */
        fun addPomodoroSession(durationMillis: Long) {
            viewModelScope.launch {
                val session = createPomodoroSession(durationMillis)
                pomodoroRepository.insertSession(session)
            }
        }

        /**
         * Creates a Pomodoro session following DRY principle.
         * Centralizes session creation logic.
         */
        private fun createPomodoroSession(durationMillis: Long): PomodoroSession =
            PomodoroSession(
                startTime = System.currentTimeMillis(),
                endTime = null,
                duration = durationMillis,
                isCompleted = false,
                type = SessionType.WORK,
            )
    }
