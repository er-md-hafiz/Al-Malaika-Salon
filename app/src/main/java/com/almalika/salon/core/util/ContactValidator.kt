package com.almalika.salon.core.util

/**
 * Result of validating an input identifier (email or phone).
 */
sealed interface InputValidationResult {
    data class ValidEmail(val normalizedEmail: String) : InputValidationResult
    data class ValidPhone(val normalizedE164: String, val nationalNumber: String, val countryCode: String = "+91") : InputValidationResult
    data class Invalid(val error: ValidationError) : InputValidationResult
}

enum class InputType {
    EMPTY,
    EMAIL,
    PHONE,
    UNKNOWN
}

enum class ValidationError(val defaultMessage: String) {
    EMPTY_INPUT("Please enter your email address or mobile number."),
    INVALID_EMAIL("Please enter a valid email address."),
    INVALID_PHONE("Please enter a valid 10-digit mobile number."),
    INVALID_INPUT("Please enter a valid email address or mobile number."),
    PHONE_AUTH_DISABLED("Mobile login is currently unavailable. Please sign in with your email address."),
    INVALID_COUNTRY_CODE("Please select a valid country code.")
}

/**
 * Robust Email Address and Mobile Number Validator for Android applications.
 *
 * Supports:
 * - Identification of input type (email vs phone vs unknown).
 * - RFC 5322-compliant email parsing without rejecting legitimate domains/tlds.
 * - Indian mobile number normalization (10 digits, +91, 91, starting with [6-9]).
 * - International phone normalization for E.164.
 * - Sanitization, trimming, lowercase normalization.
 */
object ContactValidator {

    // RFC 5322 compatible compliant email regex pattern (case-insensitive)
    // Disallows leading/trailing dots in local part, enforces valid domain parts and TLD length >= 2
    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9]+([._%+-][A-Za-z0-9]+)*@[A-Za-z0-9]+([.-][A-Za-z0-9]+)*\\.[A-Za-z]{2,}\$"
    )

    // Indian mobile prefix and 10-digit starting with 6, 7, 8, 9
    // Supports formats: 9876543210, +919876543210, 919876543210, 09876543210
    // with optional spaces or dashes
    private val INDIAN_MOBILE_PREFIX_REGEX = Regex("^(?:\\+91|91|0)?([6-9]\\d{9})\$")

    /**
     * Determines whether the raw input looks like an email, phone number, or unknown.
     */
    fun detectInputType(rawInput: String?): InputType {
        if (rawInput.isNullOrBlank()) return InputType.EMPTY
        val trimmed = rawInput.trim()

        if (trimmed.contains("@")) {
            return InputType.EMAIL
        }

        // Strip allowable phone formatting characters like +, -, spaces, parentheses
        val cleanForDigits = trimmed.replace(Regex("[\\s\\-\\(\\)]"), "")
        if (cleanForDigits.all { it.isDigit() || it == '+' }) {
            val digitsCount = cleanForDigits.count { it.isDigit() }
            if (digitsCount in 7..15) {
                return InputType.PHONE
            }
        }

        return InputType.UNKNOWN
    }

    /**
     * Validates and normalizes an email address.
     * Trims leading/trailing whitespace and normalizes to lower case.
     */
    fun validateEmail(rawEmail: String?): InputValidationResult {
        if (rawEmail.isNullOrBlank()) {
            return InputValidationResult.Invalid(ValidationError.EMPTY_INPUT)
        }
        val trimmed = rawEmail.trim()

        // Reject if contains spaces or illegal characters
        if (trimmed.contains(" ") || !EMAIL_REGEX.matches(trimmed)) {
            return InputValidationResult.Invalid(ValidationError.INVALID_EMAIL)
        }

        val parts = trimmed.split("@")
        if (parts.size != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            return InputValidationResult.Invalid(ValidationError.INVALID_EMAIL)
        }

        val localPart = parts[0]
        val domainPart = parts[1]

        // Reject consecutive dots or invalid domain boundary
        if (localPart.startsWith(".") || localPart.endsWith(".") || localPart.contains("..")) {
            return InputValidationResult.Invalid(ValidationError.INVALID_EMAIL)
        }
        if (domainPart.startsWith(".") || domainPart.endsWith(".") || domainPart.contains("..") || !domainPart.contains(".")) {
            return InputValidationResult.Invalid(ValidationError.INVALID_EMAIL)
        }

        val normalized = trimmed.lowercase()
        return InputValidationResult.ValidEmail(normalizedEmail = normalized)
    }

    /**
     * Validates and normalizes an Indian mobile number.
     * Accepts:
     * - 10 digits starting with [6-9]
     * - With +91, 91, or 0 prefix
     * - Formatted with optional spaces, hyphens, or parentheses
     * Normalizes to E.164: +91XXXXXXXXXX
     */
    fun validateIndianMobileNumber(rawNumber: String?): InputValidationResult {
        if (rawNumber.isNullOrBlank()) {
            return InputValidationResult.Invalid(ValidationError.EMPTY_INPUT)
        }

        val trimmed = rawNumber.trim()

        // Check if there are illegal alphabetic characters
        val nonAllowedChars = trimmed.filter { !(it.isDigit() || it == '+' || it == ' ' || it == '-' || it == '(' || it == ')') }
        if (nonAllowedChars.isNotEmpty()) {
            return InputValidationResult.Invalid(ValidationError.INVALID_PHONE)
        }

        // Strip non-digits except initial '+'
        val sanitized = trimmed.replace(Regex("[\\s\\-\\(\\)]"), "")
        val match = INDIAN_MOBILE_PREFIX_REGEX.matchEntire(sanitized)

        if (match != null) {
            val tenDigits = match.groupValues[1]
            val e164 = "+91$tenDigits"
            return InputValidationResult.ValidPhone(
                normalizedE164 = e164,
                nationalNumber = tenDigits,
                countryCode = "+91"
            )
        }

        return InputValidationResult.Invalid(ValidationError.INVALID_PHONE)
    }

    /**
     * Normalizes any mobile number string by stripping extraneous formatting.
     * If valid Indian mobile number, returns standard E.164 (+91XXXXXXXXXX).
     * Otherwise returns digits with '+' if originally present.
     */
    fun normalizeMobileNumber(rawNumber: String?, defaultCountryCode: String = "+91"): String? {
        val indianResult = validateIndianMobileNumber(rawNumber)
        if (indianResult is InputValidationResult.ValidPhone) {
            return indianResult.normalizedE164
        }

        if (rawNumber.isNullOrBlank()) return null
        val cleaned = rawNumber.trim().replace(Regex("[\\s\\-\\(\\)]"), "")
        if (cleaned.all { it.isDigit() || it == '+' }) {
            return if (cleaned.startsWith("+")) cleaned else "$defaultCountryCode$cleaned"
        }
        return null
    }

    /**
     * Automatically identifies whether the input is an email or phone number and validates it.
     */
    fun validateIdentifier(rawInput: String?): InputValidationResult {
        if (rawInput.isNullOrBlank()) {
            return InputValidationResult.Invalid(ValidationError.EMPTY_INPUT)
        }

        return when (detectInputType(rawInput)) {
            InputType.EMAIL -> validateEmail(rawInput)
            InputType.PHONE -> validateIndianMobileNumber(rawInput)
            InputType.EMPTY -> InputValidationResult.Invalid(ValidationError.EMPTY_INPUT)
            InputType.UNKNOWN -> {
                // If contains alphabets and digits or symbols without @, it's invalid input
                InputValidationResult.Invalid(ValidationError.INVALID_INPUT)
            }
        }
    }

    /**
     * Convenience helper returning boolean for email validity.
     */
    fun isValidEmail(email: String?): Boolean {
        return validateEmail(email) is InputValidationResult.ValidEmail
    }

    /**
     * Convenience helper returning boolean for Indian phone validity.
     */
    fun isValidIndianMobile(phone: String?): Boolean {
        return validateIndianMobileNumber(phone) is InputValidationResult.ValidPhone
    }
}

