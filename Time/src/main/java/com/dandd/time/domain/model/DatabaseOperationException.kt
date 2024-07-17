package com.dandd.time.domain.model
/**
 * Exception thrown when an operation on the database fails.
 */
class DatabaseOperationException(message: String, cause: Throwable? = null) : Exception(message, cause)