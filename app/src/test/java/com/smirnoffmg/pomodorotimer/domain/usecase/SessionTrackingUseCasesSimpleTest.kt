package com.smirnoffmg.pomodorotimer.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import com.smirnoffmg.pomodorotimer.domain.model.SessionType
import com.smirnoffmg.pomodorotimer.domain.repository.PomodoroRepository
import com.smirnoffmg.pomodorotimer.testing.BaseUnitTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SessionTrackingUseCasesSimpleTest : BaseUnitTest() {

    @Mock
    private lateinit var mockRepository: PomodoroRepository
    
    private lateinit var startSessionUseCase: StartSessionUseCase
    private lateinit var completeSessionUseCase: CompleteSessionUseCase
    private lateinit var cancelSessionUseCase: CancelSessionUseCase

    @Before
    override fun setUp() {
        super.setUp()
        MockitoAnnotations.openMocks(this)
        startSessionUseCase = StartSessionUseCase(mockRepository)
        completeSessionUseCase = CompleteSessionUseCase(mockRepository)
        cancelSessionUseCase = CancelSessionUseCase(mockRepository)
    }

    @Test
    fun startSessionUseCase_callsRepositoryInsert() = runTest {
        val sessionType = SessionType.WORK
        val duration = 25 * 60 * 1000L
        val expectedId = 123L
        whenever(mockRepository.insertSession(any())).thenReturn(expectedId)
        
        val result = startSessionUseCase(sessionType, duration)
        
        assertThat(result).isEqualTo(expectedId)
        verify(mockRepository).insertSession(any())
    }

    @Test
    fun completeSessionUseCase_successWithValidSession() = runTest {
        val sessionId = 1L
        val session = createTestSession(id = sessionId, isCompleted = false)
        whenever(mockRepository.getSessionById(sessionId)).thenReturn(session)
        
        val result = completeSessionUseCase(sessionId)
        
        assertThat(result.isSuccess).isTrue()
        verify(mockRepository).updateSessionCompletion(any(), any(), any())
    }

    @Test
    fun completeSessionUseCase_failsWhenSessionNotFound() = runTest {
        val sessionId = 1L
        whenever(mockRepository.getSessionById(sessionId)).thenReturn(null)
        
        val result = completeSessionUseCase(sessionId)
        
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun completeSessionUseCase_failsWhenSessionAlreadyCompleted() = runTest {
        val sessionId = 1L
        val session = createTestSession(id = sessionId, isCompleted = true)
        whenever(mockRepository.getSessionById(sessionId)).thenReturn(session)
        
        val result = completeSessionUseCase(sessionId)
        
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun cancelSessionUseCase_successWithIncompleteSession() = runTest {
        val sessionId = 1L
        val session = createTestSession(id = sessionId, isCompleted = false)
        whenever(mockRepository.getSessionById(sessionId)).thenReturn(session)
        
        val result = cancelSessionUseCase(sessionId)
        
        assertThat(result.isSuccess).isTrue()
        verify(mockRepository).deleteSession(sessionId)
    }

    @Test
    fun cancelSessionUseCase_failsWithCompletedSession() = runTest {
        val sessionId = 1L
        val session = createTestSession(id = sessionId, isCompleted = true)
        whenever(mockRepository.getSessionById(sessionId)).thenReturn(session)
        
        val result = cancelSessionUseCase(sessionId)
        
        assertThat(result.isFailure).isTrue()
    }

    private fun createTestSession(
        id: Long = 1L,
        startTime: Long = System.currentTimeMillis(),
        endTime: Long? = null,
        duration: Long = 25 * 60 * 1000L,
        isCompleted: Boolean = false,
        type: SessionType = SessionType.WORK
    ) = PomodoroSession(
        id = id,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        isCompleted = isCompleted,
        type = type,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}