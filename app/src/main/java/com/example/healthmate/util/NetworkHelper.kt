package com.example.healthmate.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * Network helper utilities for reliable network operations.
 */
object NetworkHelper {

    /**
     * Execute a suspending operation with exponential backoff retry.
     *
     * @param times Number of retry attempts (default 3)
     * @param initialDelayMs Initial delay in milliseconds (default 100)
     * @param maxDelayMs Maximum delay in milliseconds (default 2000)
     * @param factor Exponential factor for delay increase (default 2.0)
     * @param block The suspending operation to execute
     * @return The result of the operation
     * @throws Exception if all retries fail
     */
    suspend fun <T> withRetry(
        times: Int = 3,
        initialDelayMs: Long = 100,
        maxDelayMs: Long = 2000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        repeat(times - 1) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                // Don't retry on cancellation
                if (e is CancellationException) throw e

                SecureLogger.w(
                    "NetworkHelper",
                    "Attempt ${attempt + 1}/$times failed, retrying in ${currentDelay}ms"
                )

                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
            }
        }
        // Last attempt without catching
        return block()
    }

    /**
     * Execute a suspending operation with retry, returning Result instead of throwing.
     *
     * @param times Number of retry attempts
     * @param initialDelayMs Initial delay in milliseconds
     * @param maxDelayMs Maximum delay in milliseconds
     * @param factor Exponential factor for delay increase
     * @param block The suspending operation to execute
     * @return Result.success with the value or Result.failure with the exception
     */
    suspend fun <T> withRetryResult(
        times: Int = 3,
        initialDelayMs: Long = 100,
        maxDelayMs: Long = 2000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(withRetry(times, initialDelayMs, maxDelayMs, factor, block))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    /**
     * Execute a suspending operation with timeout and retry.
     *
     * @param timeoutMs Timeout in milliseconds
     * @param times Number of retry attempts
     * @param block The suspending operation to execute
     * @return The result of the operation
     */
    suspend fun <T> withTimeoutAndRetry(
        timeoutMs: Long = 10000,
        times: Int = 3,
        block: suspend () -> T
    ): T {
        return kotlinx.coroutines.withTimeout(timeoutMs) {
            withRetry(times = times, block = block)
        }
    }
}

/**
 * Sealed class representing the result of a network operation.
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data
    fun errorMessageOrNull(): String? = (this as? Error)?.message
}
