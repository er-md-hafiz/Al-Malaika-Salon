package com.almalika.salon.core.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsSanitizationTest {

    private fun sanitize(input: String, maxLength: Int): String {
        return input.trim()
            .take(maxLength)
            .replace(Regex("""[^\w\s\-_./]"""), "")
    }

    @Test
    fun `test sanitize trims and limits maximum length`() {
        val longInput = "a".repeat(100)
        val result = sanitize(longInput, 32)
        assertEquals(32, result.length)
    }

    @Test
    fun `test sanitize removes sensitive punctuation and special characters`() {
        val sensitiveInput = "user@example.com?secret=123#token"
        val result = sanitize(sensitiveInput, 64)
        assertFalse(result.contains("@"))
        assertFalse(result.contains("?"))
        assertFalse(result.contains("="))
        assertFalse(result.contains("#"))
    }

    @Test
    fun `test sanitize preserves standard alphanumeric and safe delimiters`() {
        val safeInput = "hair_cut-style.v1 /main"
        val result = sanitize(safeInput, 64)
        assertEquals(safeInput, result)
    }
}
