package com.smirnoffmg.pomodorotimer.domain.repository

import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import kotlinx.coroutines.flow.Flow

interface PomodoroRepository {
    fun getAllSessions(): Flow<List<PomodoroSession>>

    suspend fun getSessionById(sessionId: Long): PomodoroSession?

    suspend fun insertSession(session: PomodoroSession): Long

    suspend fun updateSessionCompletion(
        sessionId: Long,
        endTime: Long,
        isCompleted: Boolean,
    )
}
