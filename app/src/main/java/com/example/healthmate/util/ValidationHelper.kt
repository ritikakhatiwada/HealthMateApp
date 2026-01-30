package com.example.healthmate.util

import android.util.Patterns

/**
 * Input validation utility for healthcare application.
 *
 * Provides validation for:
 * - Email addresses
 * - Passwords (with strength requirements)
 * - Phone numbers
 * - Names
 * - Medical data inputs
 *
 * All validation methods return a [ValidationResult] that can be checked for validity.
 */
object ValidationHelper {

    // Password requirements
    private const val MIN_PASSWORD_LENGTH = 6
    private const val MAX_PASSWORD_LENGTH = 128

    // Name requirements
    private const val MIN_NAME_LENGTH = 2
    private const val MAX_NAME_LENGTH = 100

    // Phone requirements
    private const val MIN_PHONE_LENGTH = 10
    private const val MAX_PHONE_LENGTH = 15

    // Medical input requirements
    private const val MAX_MEDICAL_INPUT_LENGTH = 5000

    /**
     * Validate email address
     */
    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()

        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Email is required")
            trimmed.length > 254 -> ValidationResult.Invalid("Email is too long")
            !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() ->
                ValidationResult.Invalid("Please enter a valid email address")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate password with strength requirements
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() ->
                ValidationResult.Invalid("Password is required")
            password.length < MIN_PASSWORD_LENGTH ->
                ValidationResult.Invalid("Password must be at least $MIN_PASSWORD_LENGTH characters")
            password.length > MAX_PASSWORD_LENGTH ->
                ValidationResult.Invalid("Password is too long")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate password with strong requirements (for sensitive operations)
     */
    fun validateStrongPassword(password: String): ValidationResult {
        val basicValidation = validatePassword(password)
        if (basicValidation is ValidationResult.Invalid) {
            return basicValidation
        }

        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }

        return when {
            !hasUppercase ->
                ValidationResult.Invalid("Password must contain at least one uppercase letter")
            !hasLowercase ->
                ValidationResult.Invalid("Password must contain at least one lowercase letter")
            !hasDigit ->
                ValidationResult.Invalid("Password must contain at least one number")
            !hasSpecial ->
                ValidationResult.Invalid("Password must contain at least one special character")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate phone number
     */
    fun validatePhoneNumber(phone: String): ValidationResult {
        val cleaned = phone.replace(Regex("[\\s\\-()]+"), "")

        return when {
            cleaned.isEmpty() ->
                ValidationResult.Invalid("Phone number is required")
            cleaned.length < MIN_PHONE_LENGTH ->
                ValidationResult.Invalid("Phone number is too short")
            cleaned.length > MAX_PHONE_LENGTH ->
                ValidationResult.Invalid("Phone number is too long")
            !cleaned.matches(Regex("^\\+?[0-9]+$")) ->
                ValidationResult.Invalid("Please enter a valid phone number")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate name (for user names, doctor names, etc.)
     */
    fun validateName(name: String): ValidationResult {
        val trimmed = name.trim()

        return when {
            trimmed.isEmpty() ->
                ValidationResult.Invalid("Name is required")
            trimmed.length < MIN_NAME_LENGTH ->
                ValidationResult.Invalid("Name must be at least $MIN_NAME_LENGTH characters")
            trimmed.length > MAX_NAME_LENGTH ->
                ValidationResult.Invalid("Name is too long")
            !trimmed.matches(Regex("^[\\p{L}\\s.'-]+$")) ->
                ValidationResult.Invalid("Name contains invalid characters")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate age input
     */
    fun validateAge(age: String): ValidationResult {
        val ageInt = age.toIntOrNull()

        return when {
            age.isEmpty() ->
                ValidationResult.Invalid("Age is required")
            ageInt == null ->
                ValidationResult.Invalid("Please enter a valid age")
            ageInt < 0 ->
                ValidationResult.Invalid("Age cannot be negative")
            ageInt > 150 ->
                ValidationResult.Invalid("Please enter a valid age")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate blood group
     */
    fun validateBloodGroup(bloodGroup: String): ValidationResult {
        val validGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val normalized = bloodGroup.uppercase().trim()

        return when {
            bloodGroup.isEmpty() ->
                ValidationResult.Invalid("Blood group is required")
            normalized !in validGroups ->
                ValidationResult.Invalid("Please select a valid blood group")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Sanitize medical input to prevent injection and remove harmful content
     */
    fun sanitizeMedicalInput(input: String): String {
        return input
            .trim()
            .take(MAX_MEDICAL_INPUT_LENGTH)
            // Remove potential script tags
            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.IGNORE_CASE), "")
            // Remove HTML tags
            .replace(Regex("<[^>]+>"), "")
            // Normalize whitespace
            .replace(Regex("\\s+"), " ")
    }

    /**
     * Validate medical notes or descriptions
     */
    fun validateMedicalInput(input: String): ValidationResult {
        val sanitized = sanitizeMedicalInput(input)

        return when {
            sanitized.isEmpty() ->
                ValidationResult.Invalid("This field is required")
            sanitized.length > MAX_MEDICAL_INPUT_LENGTH ->
                ValidationResult.Invalid("Input is too long (max $MAX_MEDICAL_INPUT_LENGTH characters)")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate date in YYYY-MM-DD format
     */
    fun validateDate(date: String): ValidationResult {
        return when {
            date.isEmpty() ->
                ValidationResult.Invalid("Date is required")
            !date.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) ->
                ValidationResult.Invalid("Invalid date format")
            else -> {
                try {
                    val parts = date.split("-")
                    val year = parts[0].toInt()
                    val month = parts[1].toInt()
                    val day = parts[2].toInt()

                    when {
                        year < 1900 || year > 2100 ->
                            ValidationResult.Invalid("Invalid year")
                        month < 1 || month > 12 ->
                            ValidationResult.Invalid("Invalid month")
                        day < 1 || day > 31 ->
                            ValidationResult.Invalid("Invalid day")
                        else -> ValidationResult.Valid
                    }
                } catch (e: Exception) {
                    ValidationResult.Invalid("Invalid date")
                }
            }
        }
    }

    /**
     * Validate time in HH:MM format
     */
    fun validateTime(time: String): ValidationResult {
        return when {
            time.isEmpty() ->
                ValidationResult.Invalid("Time is required")
            !time.matches(Regex("^\\d{2}:\\d{2}$")) ->
                ValidationResult.Invalid("Invalid time format (use HH:MM)")
            else -> {
                val parts = time.split(":")
                val hour = parts[0].toIntOrNull() ?: -1
                val minute = parts[1].toIntOrNull() ?: -1

                when {
                    hour < 0 || hour > 23 ->
                        ValidationResult.Invalid("Invalid hour")
                    minute < 0 || minute > 59 ->
                        ValidationResult.Invalid("Invalid minute")
                    else -> ValidationResult.Valid
                }
            }
        }
    }
}

/**
 * Result of a validation check
 */
sealed class ValidationResult {
    /** Input is valid */
    object Valid : ValidationResult()

    /** Input is invalid with an error message */
    data class Invalid(val message: String) : ValidationResult()

    /** Check if the result is valid */
    val isValid: Boolean get() = this is Valid

    /** Get error message or null if valid */
    val errorMessage: String? get() = (this as? Invalid)?.message
}

/**
 * Extension function to get error message or null
 */
fun ValidationResult.errorOrNull(): String? = (this as? ValidationResult.Invalid)?.message
