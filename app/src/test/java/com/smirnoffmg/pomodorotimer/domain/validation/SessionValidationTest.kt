package com.smirnoffmg.pomodorotimer.domain.validation

import com.google.common.truth.Truth.assertThat
import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import com.smirnoffmg.pomodorotimer.domain.model.SessionType
import com.smirnoffmg.pomodorotimer.testing.BaseUnitTest
import org.junit.Test

class SessionValidationTest : BaseUnitTest() {

    @Test
    fun validSession_passesValidation() {
        val validSession = createValidSession()
        
        val result = SessionValidator.validate(validSession)
        
        assertThat(result).isEqualTo(ValidationResult.Valid)
    }

    @Test
    fun sessionWithZeroDuration_failsValidation() {
        val session = createValidSession(duration = 0L)
        
        val result = SessionValidator.validate(session)
        
        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val errors = (result as ValidationResult.Invalid).errors
        assertThat(errors).contains(ValidationError.InvalidDuration)
    }

    @Test
    fun sessionWithNegativeDuration_failsValidation() {
        val session = createValidSession(duration = -1000L)
        
        val result = SessionValidator.validate(session)
        
        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val errors = (result as ValidationResult.Invalid).errors
        assertThat(errors).contains(ValidationError.InvalidDuration)
    }

    @Test
    fun sessionWithTooShortDuration_failsValidation() {
        val session = createValidSession(duration = 30_000L) // 30 seconds
        
        val result = SessionValidator.validate(session)
        
        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val errors = (result as ValidationResult.Invalid).errors
        assertThat(errors).contains(ValidationError.SessionTooShort)
    }

    @Test
    fun sessionWithTooLongDuration_failsValidation() {
        val session = createValidSession(duration = 3 * 60 * 60 * 1000L) // 3 hours
        
        val result = SessionValidator.validate(session)
        
        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val errors = (result as ValidationResult.Invalid).errors
        assertThat(errors).contains(ValidationError.SessionTooLong)
    }

    @Test
    fun sessionWithFutureStartTime_failsValidation() {
        val futureTime = System.currentTimeMillis() + 60_000L // 1 minute in future
        val session = createValidSession(startTime = futureTime)
        
        val result = SessionValidator.validate(session)
        
        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val errors = (result as ValidationResult.Invalid).errors
        assertThat(errors).contains(ValidationError.InvalidStartTime)
    }

    @Test
    fun sessionWithEndTimeBeforeStartTime_failsValidation() {
        val startTime = System.currentTimeMillis()
        val endTime = startTime - 1000L // 1 second before start
        val session = createValidSession(
            startTime = startTime,
            endTime = endTime
        )
        
        val result = SessionValidator.validate(session)
        
        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val errors = (result as ValidationResult.Invalid).errors
        assertThat(errors).contains(ValidationError.InvalidEndTime)
    }

    @Test
    fun completedSessionWithoutEndTime_failsValidation() {
        val session = createValidSession(
            isCompleted = true,
            endTime = null
        )
        
        val result = SessionValidator.validate(session)
        
        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val errors = (result as ValidationResult.Invalid).errors
        assertThat(errors).contains(ValidationError.InvalidCompletionState)
    }

    @Test
    fun completedSessionWithInconsistentDuration_failsValidation() {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + 30 * 60 * 1000L // 30 minutes actual
        val reportedDuration = 25 * 60 * 1000L // 25 minutes reported
        
        val session = createValidSession(
            startTime = startTime,
            endTime = endTime,
            duration = reportedDuration,
            isCompleted = true
        )
        
        val result = SessionValidator.validate(session)
        
        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val errors = (result as ValidationResult.Invalid).errors
        assertThat(errors).contains(ValidationError.InconsistentDuration)
    }

    @Test
    fun completedSessionWithConsistentDuration_passesValidation() {
        val startTime = System.currentTimeMillis()
        val duration = 25 * 60 * 1000L // 25 minutes
        val endTime = startTime + duration
        
        val session = createValidSession(
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            isCompleted = true
        )
        
        val result = SessionValidator.validate(session)
        
        assertThat(result).isEqualTo(ValidationResult.Valid)
    }

    @Test
    fun validateDuration_workSession_acceptsReasonableDuration() {
        val duration = 25 * 60 * 1000L // 25 minutes
        
        val result = SessionValidator.validateDuration(duration, SessionType.WORK)
        
        assertThat(result).isEqualTo(ValidationResult.Valid)
    }

    @Test
    fun validateDuration_workSession_rejectsTooShort() {
        val duration = 5 * 60 * 1000L // 5 minutes (too short for work)
        
        val result = SessionValidator.validateDuration(duration, SessionType.WORK)
        
        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val errors = (result as ValidationResult.Invalid).errors
        assertThat(errors).contains(ValidationError.SessionTooShort)
    }

    @Test
    fun validateDuration_workSession_rejectsTooLong() {
        val duration = 90 * 60 * 1000L // 90 minutes (too long for work)
        
        val result = SessionValidator.validateDuration(duration, SessionType.WORK)
        
        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val errors = (result as ValidationResult.Invalid).errors
        assertThat(errors).contains(ValidationError.SessionTooLong)
    }

    @Test
    fun validateDuration_shortBreak_acceptsReasonableDuration() {
        val duration = 5 * 60 * 1000L // 5 minutes
        
        val result = SessionValidator.validateDuration(duration, SessionType.SHORT_BREAK)
        
        assertThat(result).isEqualTo(ValidationResult.Valid)
    }

    @Test
    fun validateDuration_shortBreak_rejectsTooLong() {
        val duration = 20 * 60 * 1000L // 20 minutes (too long for short break)
        
        val result = SessionValidator.validateDuration(duration, SessionType.SHORT_BREAK)
        
        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val errors = (result as ValidationResult.Invalid).errors
        assertThat(errors).contains(ValidationError.SessionTooLong)
    }

    @Test
    fun validateDuration_longBreak_acceptsReasonableDuration() {
        val duration = 15 * 60 * 1000L // 15 minutes
        
        val result = SessionValidator.validateDuration(duration, SessionType.LONG_BREAK)
        
        assertThat(result).isEqualTo(ValidationResult.Valid)
    }

    @Test
    fun validateDuration_longBreak_rejectsTooLong() {
        val duration = 45 * 60 * 1000L // 45 minutes (too long for long break)
        
        val result = SessionValidator.validateDuration(duration, SessionType.LONG_BREAK)
        
        assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        val errors = (result as ValidationResult.Invalid).errors
        assertThat(errors).contains(ValidationError.SessionTooLong)
    }

    @Test
    fun sessionExtension_isValid_returnsTrueForValidSession() {
        val validSession = createValidSession()
        
        val result = validSession.isValid()
        
        assertThat(result).isTrue()
    }

    @Test
    fun sessionExtension_isValid_returnsFalseForInvalidSession() {
        val invalidSession = createValidSession(duration = -1000L)
        
        val result = invalidSession.isValid()
        
        assertThat(result).isFalse()
    }

    @Test
    fun validationResult_getErrorMessages_returnsEmptyForValid() {
        val result = ValidationResult.Valid
        
        val messages = result.getErrorMessages()
        
        assertThat(messages).isEmpty()
    }

    @Test
    fun validationResult_getErrorMessages_returnsMessagesForInvalid() {
        val errors = listOf(
            ValidationError.InvalidDuration,
            ValidationError.SessionTooShort
        )
        val result = ValidationResult.Invalid(errors)
        
        val messages = result.getErrorMessages()
        
        assertThat(messages).hasSize(2)
        assertThat(messages).contains("Duration must be greater than 0")
        assertThat(messages).contains("Session duration is below minimum allowed")
    }

    private fun createValidSession(
        id: Long = 1L,
        startTime: Long = System.currentTimeMillis() - 1000L, // 1 second ago
        endTime: Long? = null,
        duration: Long = 25 * 60 * 1000L, // 25 minutes
        isCompleted: Boolean = false,
        type: SessionType = SessionType.WORK,
        createdAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    ) = PomodoroSession(
        id = id,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        isCompleted = isCompleted,
        type = type,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}