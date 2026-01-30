package com.example.healthmate.util

import android.util.Log
import com.example.healthmate.BuildConfig

/**
 * Secure logging utility that only logs in debug builds.
 *
 * This prevents sensitive information from being logged in production builds.
 * All log calls are no-ops in release builds for security.
 *
 * Usage:
 * ```kotlin
 * SecureLogger.d("MyTag", "Debug message")
 * SecureLogger.e("MyTag", "Error message", exception)
 * ```
 */
object SecureLogger {

    private const val MAX_LOG_LENGTH = 4000

    /**
     * Log a debug message (only in debug builds)
     */
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            logLong(tag, message) { t, m -> Log.d(t, m) }
        }
    }

    /**
     * Log an info message (only in debug builds)
     */
    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            logLong(tag, message) { t, m -> Log.i(t, m) }
        }
    }

    /**
     * Log a warning message (only in debug builds)
     */
    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            logLong(tag, message) { t, m -> Log.w(t, m) }
        }
    }

    /**
     * Log an error message (only in debug builds)
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                logLong(tag, message) { t, m -> Log.e(t, m) }
            }
        }
    }

    /**
     * Log a verbose message (only in debug builds)
     */
    fun v(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            logLong(tag, message) { t, m -> Log.v(t, m) }
        }
    }

    /**
     * Handle long messages by splitting them
     */
    private inline fun logLong(tag: String, message: String, logFunc: (String, String) -> Unit) {
        if (message.length <= MAX_LOG_LENGTH) {
            logFunc(tag, message)
        } else {
            var i = 0
            while (i < message.length) {
                val end = minOf(i + MAX_LOG_LENGTH, message.length)
                logFunc(tag, message.substring(i, end))
                i = end
            }
        }
    }

    /**
     * Sanitize sensitive data before logging
     * Use this when you need to log something that might contain PII
     */
    fun sanitize(value: String, visibleChars: Int = 4): String {
        return if (value.length <= visibleChars * 2) {
            "*".repeat(value.length)
        } else {
            "${value.take(visibleChars)}${"*".repeat(value.length - visibleChars * 2)}${value.takeLast(visibleChars)}"
        }
    }

    /**
     * Sanitize email for logging (shows domain only)
     */
    fun sanitizeEmail(email: String): String {
        val atIndex = email.indexOf('@')
        return if (atIndex > 0) {
            "****${email.substring(atIndex)}"
        } else {
            "****"
        }
    }

    /**
     * Sanitize phone number for logging
     */
    fun sanitizePhone(phone: String): String {
        return if (phone.length > 4) {
            "*".repeat(phone.length - 4) + phone.takeLast(4)
        } else {
            "****"
        }
    }
}
