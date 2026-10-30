package com.almalika.salon.core.util

import org.junit.Assert.*
import org.junit.Test

class ContactValidatorTest {

    // --- Automatic Input Detection Tests ---

    @Test
    fun testDetectInputType() {
        assertEquals(InputType.EMAIL, ContactValidator.detectInputType("user@gmail.com"))
        assertEquals(InputType.EMAIL, ContactValidator.detectInputType("test.user@example.com"))
        assertEquals(InputType.EMAIL, ContactValidator.detectInputType("user@"))
        assertEquals(InputType.EMAIL, ContactValidator.detectInputType("@gmail.com"))

        assertEquals(InputType.PHONE, ContactValidator.detectInputType("9876543210"))
        assertEquals(InputType.PHONE, ContactValidator.detectInputType("+919876543210"))
        assertEquals(InputType.PHONE, ContactValidator.detectInputType("919876543210"))
        assertEquals(InputType.PHONE, ContactValidator.detectInputType("+91 98765 43210"))

        assertEquals(InputType.EMPTY, ContactValidator.detectInputType(""))
        assertEquals(InputType.EMPTY, ContactValidator.detectInputType("   "))
        assertEquals(InputType.EMPTY, ContactValidator.detectInputType(null))

        assertEquals(InputType.UNKNOWN, ContactValidator.detectInputType("abc1234567"))
        assertEquals(InputType.UNKNOWN, ContactValidator.detectInputType("user name"))
        assertEquals(InputType.UNKNOWN, ContactValidator.detectInputType("12345")) // too short to be phone
    }

    // --- Email Validation Tests ---

    @Test
    fun testValidEmails() {
        val validList = listOf(
            "user@gmail.com",
            "test.user@example.com",
            "USER@EXAMPLE.COM",
            "  user.name+tag@sub.domain.co.in  ",
            "customer_123@salon.org"
        )

        for (email in validList) {
            val result = ContactValidator.validateEmail(email)
            assertTrue("Expected valid for $email", result is InputValidationResult.ValidEmail)
            val normalized = (result as InputValidationResult.ValidEmail).normalizedEmail
            assertEquals(email.trim().lowercase(), normalized)
        }
    }

    @Test
    fun testInvalidEmails() {
        val invalidList = listOf(
            "user@",
            "@gmail.com",
            "user@gmail",
            "user @domain.com",
            "user..name@domain.com",
            ".user@domain.com",
            "user@.domain.com",
            "user@domain..com",
            "user@domain.c",
            "notanemail",
            "",
            "   ",
            null
        )

        for (email in invalidList) {
            val result = ContactValidator.validateEmail(email)
            assertTrue("Expected invalid for '$email'", result is InputValidationResult.Invalid)
        }
    }

    // --- Indian Mobile Number Validation Tests ---

    @Test
    fun testValidIndianMobileNumbers() {
        val validCases = mapOf(
            "9876543210" to "+919876543210",
            "+919876543210" to "+919876543210",
            "919876543210" to "+919876543210",
            "09876543210" to "+919876543210",
            " 9876543210 " to "+919876543210",
            "+91 98765 43210" to "+919876543210",
            "+91-98765-43210" to "+919876543210",
            "6123456789" to "+916123456789",
            "7123456789" to "+917123456789",
            "8123456789" to "+918123456789"
        )

        for ((input, expectedE164) in validCases) {
            val result = ContactValidator.validateIndianMobileNumber(input)
            assertTrue("Expected valid for $input", result is InputValidationResult.ValidPhone)
            val phoneResult = result as InputValidationResult.ValidPhone
            assertEquals(expectedE164, phoneResult.normalizedE164)
            assertEquals(expectedE164.removePrefix("+91"), phoneResult.nationalNumber)
        }
    }

    @Test
    fun testInvalidIndianMobileNumbers() {
        val invalidList = listOf(
            "12345",              // Too short
            "987654321",          // 9 digits
            "987654321000",       // 12 digits without country code
            "abc1234567",         // Alpha characters
            "5876543210",         // Starts with 5 (Indian mobiles start with 6-9)
            "0000000000",         // All zeroes
            "987654321a",         // Trailing alpha
            "+1234567890",         // Wrong country code for Indian validator
            "",
            "   ",
            null
        )

        for (input in invalidList) {
            val result = ContactValidator.validateIndianMobileNumber(input)
            assertTrue("Expected invalid for '$input'", result is InputValidationResult.Invalid)
        }
    }

    // --- Automatic Identifier Validation Tests ---

    @Test
    fun testValidateIdentifierAutomaticDetection() {
        // Valid email
        val emailResult = ContactValidator.validateIdentifier("USER@GMAIL.COM")
        assertTrue(emailResult is InputValidationResult.ValidEmail)
        assertEquals("user@gmail.com", (emailResult as InputValidationResult.ValidEmail).normalizedEmail)

        // Valid mobile
        val phoneResult = ContactValidator.validateIdentifier("9876543210")
        assertTrue(phoneResult is InputValidationResult.ValidPhone)
        assertEquals("+919876543210", (phoneResult as InputValidationResult.ValidPhone).normalizedE164)

        // Invalid email
        val badEmail = ContactValidator.validateIdentifier("user@")
        assertTrue(badEmail is InputValidationResult.Invalid)
        assertEquals(ValidationError.INVALID_EMAIL, (badEmail as InputValidationResult.Invalid).error)

        // Invalid mobile
        val badPhone = ContactValidator.validateIdentifier("12345")
        assertTrue(badPhone is InputValidationResult.Invalid)

        // Mixed invalid
        val badMixed = ContactValidator.validateIdentifier("abc1234567")
        assertTrue(badMixed is InputValidationResult.Invalid)
        assertEquals(ValidationError.INVALID_INPUT, (badMixed as InputValidationResult.Invalid).error)

        // Empty
        val emptyResult = ContactValidator.validateIdentifier("   ")
        assertTrue(emptyResult is InputValidationResult.Invalid)
        assertEquals(ValidationError.EMPTY_INPUT, (emptyResult as InputValidationResult.Invalid).error)
    }
}

