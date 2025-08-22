package com.smirnoffmg.pomodorotimer.domain.exception

sealed class DatabaseException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    class SessionNotFound(
        sessionId: Long
    ) : DatabaseException("Session with ID $sessionId not found")

    class InvalidSessionData(
        message: String,
        cause: Throwable? = null
    ) : DatabaseException("Invalid session data: $message", cause)

    class DatabaseCorrupted(
        message: String,
        cause: Throwable? = null
    ) : DatabaseException("Database corrupted: $message", cause)

    class MigrationFailed(
        fromVersion: Int,
        toVersion: Int,
        cause: Throwable? = null
    ) : DatabaseException("Migration failed from version $fromVersion to $toVersion", cause)

    class DiskSpaceInsufficient(
        cause: Throwable? = null
    ) : DatabaseException("Insufficient disk space for database operation", cause)

    class DatabaseLocked(
        cause: Throwable? = null
    ) : DatabaseException("Database is locked and cannot be accessed", cause)

    class QueryTimeout(
        query: String,
        cause: Throwable? = null
    ) : DatabaseException("Query timed out: $query", cause)

    class ConstraintViolation(
        constraint: String,
        cause: Throwable? = null
    ) : DatabaseException("Database constraint violation: $constraint", cause)

    class UnknownDatabaseError(
        cause: Throwable
    ) : DatabaseException("Unknown database error: ${cause.message}", cause)
}

object DatabaseErrorHandler {
    fun handleException(throwable: Throwable): DatabaseException =
        when {
            throwable.message?.contains("no such table", ignoreCase = true) == true -> 
                DatabaseException.DatabaseCorrupted("Table not found", throwable)
                
            throwable.message?.contains("database is locked", ignoreCase = true) == true -> 
                DatabaseException.DatabaseLocked(throwable)
                
            throwable.message?.contains("disk space", ignoreCase = true) == true || 
                throwable.message?.contains("no space", ignoreCase = true) == true ->
                DatabaseException.DiskSpaceInsufficient(throwable)
                
            throwable.message?.contains("constraint", ignoreCase = true) == true ||
                throwable.message?.contains("unique", ignoreCase = true) == true ->
                DatabaseException.ConstraintViolation(throwable.message ?: "Unknown constraint", throwable)
                
            throwable.message?.contains("timeout", ignoreCase = true) == true ->
                DatabaseException.QueryTimeout("Unknown query", throwable)
                
            throwable is android.database.sqlite.SQLiteException -> {
                when (throwable.message?.lowercase()) {
                    null -> DatabaseException.UnknownDatabaseError(throwable)
                    else -> DatabaseException.UnknownDatabaseError(throwable)
                }
            }
            
            else -> DatabaseException.UnknownDatabaseError(throwable)
        }
}

// Extension functions for cleaner error handling
inline fun <T> safeDbCall(action: () -> T): Result<T> =
    try {
        Result.success(action())
    } catch (e: Exception) {
        Result.failure(DatabaseErrorHandler.handleException(e))
    }

suspend inline fun <T> safeSuspendDbCall(crossinline action: suspend () -> T): Result<T> =
    try {
        Result.success(action())
    } catch (e: Exception) {
        Result.failure(DatabaseErrorHandler.handleException(e))
    }
