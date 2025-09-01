package com.smirnoffmg.pomodorotimer.domain.validation

import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import com.smirnoffmg.pomodorotimer.domain.model.SessionType

sealed class ValidationResult {
    object Valid : ValidationResult()

    data class Invalid(
        val errors: List<ValidationError>,
    ) : ValidationResult()
}

sealed class ValidationError(
    val message: String,
) {
    object InvalidDuration : ValidationError("Duration must be greater than 0")

    object InvalidStartTime : ValidationError("Start time cannot be in the future")

    object InvalidEndTime : ValidationError("End time cannot be before start time")

    object InvalidSessionType : ValidationError("Session type is required")

    object SessionTooLong : ValidationError("Session duration exceeds maximum allowed")

    object SessionTooShort : ValidationError("Session duration is below minimum allowed")

    object InvalidCompletionState : ValidationError("Completed session must have an end time")

    object InconsistentDuration : ValidationError("Calculated duration doesn't match provided duration")
}

object SessionValidator {
    private const val MIN_SESSION_DURATION_MS = 60_000L // 1 minute
    private const val MAX_SESSION_DURATION_MS = 7_200_000L // 2 hours
    private const val DURATION_TOLERANCE_MS = 5_000L // 5 seconds tolerance

    fun validate(session: PomodoroSession): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Validate duration
        if (session.duration <= 0) {
            errors.add(ValidationError.InvalidDuration)
        } else {
            if (session.duration < MIN_SESSION_DURATION_MS) {
                errors.add(ValidationError.SessionTooShort)
            }
            if (session.duration > MAX_SESSION_DURATION_MS) {
                errors.add(ValidationError.SessionTooLong)
            }
        }

        // Validate start time
        val currentTime = System.currentTimeMillis()
        if (session.startTime > currentTime + DURATION_TOLERANCE_MS) {
            errors.add(ValidationError.InvalidStartTime)
        }

        // Validate end time
        session.endTime?.let { endTime ->
            if (endTime < session.startTime) {
                errors.add(ValidationError.InvalidEndTime)
            }

            // If session is completed, validate consistency
            if (session.isCompleted) {
                val calculatedDuration = endTime - session.startTime
                if (kotlin.math.abs(calculatedDuration - session.duration) > DURATION_TOLERANCE_MS) {
                    errors.add(ValidationError.InconsistentDuration)
                }
            }
        }

        // Validate completion state
        if (session.isCompleted && session.endTime == null) {
            errors.add(ValidationError.InvalidCompletionState)
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    fun validateDuration(
        duration: Long,
        sessionType: SessionType,
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (duration <= 0) {
            errors.add(ValidationError.InvalidDuration)
            return ValidationResult.Invalid(errors)
        }

        // Allow some flexibility but warn about extremes
        when (sessionType) {
            SessionType.WORK -> {
                if (duration < 10 * 60 * 1000L) { // Less than 10 minutes
                    errors.add(ValidationError.SessionTooShort)
                } else if (duration > 60 * 60 * 1000L) { // More than 1 hour
                    errors.add(ValidationError.SessionTooLong)
                }
            }
            SessionType.SHORT_BREAK -> {
                if (duration < 60 * 1000L) { // Less than 1 minute
                    errors.add(ValidationError.SessionTooShort)
                } else if (duration > 15 * 60 * 1000L) { // More than 15 minutes
                    errors.add(ValidationError.SessionTooLong)
                }
            }
            SessionType.LONG_BREAK -> {
                if (duration < 5 * 60 * 1000L) { // Less than 5 minutes
                    errors.add(ValidationError.SessionTooShort)
                } else if (duration > 30 * 60 * 1000L) { // More than 30 minutes
                    errors.add(ValidationError.SessionTooLong)
                }
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

// Extension functions for easier validation
fun PomodoroSession.validate(): ValidationResult = SessionValidator.validate(this)

fun PomodoroSession.isValid(): Boolean = validate() is ValidationResult.Valid

fun ValidationResult.getErrorMessages(): List<String> =
    when (this) {
        is ValidationResult.Valid -> emptyList()
        is ValidationResult.Invalid -> errors.map { it.message }
    }
