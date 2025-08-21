package com.smirnoffmg.pomodorotimer.data.repository

import com.smirnoffmg.pomodorotimer.data.local.db.dao.PomodoroSessionDao
import com.smirnoffmg.pomodorotimer.data.mapper.toDomain
import com.smirnoffmg.pomodorotimer.data.mapper.toEntity
import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import com.smirnoffmg.pomodorotimer.domain.repository.PomodoroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PomodoroRepositoryImpl
    @Inject
    constructor(
        private val pomodoroSessionDao: PomodoroSessionDao,
    ) : PomodoroRepository {
        override fun getAllSessions(): Flow<List<PomodoroSession>> =
            pomodoroSessionDao.getAllSessions().map { sessions ->
                sessions.map { it.toDomain() }
            }

        override suspend fun getSessionById(sessionId: Long): PomodoroSession? = pomodoroSessionDao.getSessionById(sessionId)?.toDomain()

        override suspend fun insertSession(session: PomodoroSession): Long = pomodoroSessionDao.insertSession(session.toEntity())

        override suspend fun updateSessionCompletion(
            sessionId: Long,
            endTime: Long,
            isCompleted: Boolean,
        ) = pomodoroSessionDao.updateSessionCompletion(sessionId, endTime, isCompleted)
    }
