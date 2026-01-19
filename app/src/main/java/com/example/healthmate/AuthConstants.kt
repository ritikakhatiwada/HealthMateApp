package com.example.healthmate

/**
 * Static authentication constants for testing purposes.
 * Replace with actual backend integration later.
 */
object AuthConstants {
    
    // User credentials
    const val USER_EMAIL = "user@test.com"
    const val USER_PASSWORD = "user123"
    
    // Admin credentials
    const val ADMIN_EMAIL = "admin@test.com"
    const val ADMIN_PASSWORD = "admin123"
    
    /**
     * Validates credentials and returns the user role.
     * @return "user", "admin", or null if invalid
     */
    fun validateCredentials(email: String, password: String): String? {
        return when {
            email == USER_EMAIL && password == USER_PASSWORD -> "user"
            email == ADMIN_EMAIL && password == ADMIN_PASSWORD -> "admin"
            else -> null
        }
    }
}
