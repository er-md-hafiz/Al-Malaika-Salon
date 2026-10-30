package com.almalika.salon.core.resilience

import org.junit.Assert.assertEquals
import org.junit.Test

class ResilienceComponentsTest {

    @Test
    fun testMoneyFormatterWithCustomSymbol() {
        val formatted = MoneyFormatter.format(150, currencySymbol = "₹")
        assertEquals("₹150", formatted)
    }

    @Test
    fun testMoneyFormatterNegativeCoercedToZero() {
        val formatted = MoneyFormatter.format(-50, currencySymbol = "₹")
        assertEquals("₹0", formatted)
    }

    @Test
    fun testMoneyFormatterFallbackByCurrencyCode() {
        val formattedUsd = MoneyFormatter.format(20, currencySymbol = null, currencyCode = "USD")
        assertEquals("$20", formattedUsd)

        val formattedInr = MoneyFormatter.format(500, currencySymbol = null, currencyCode = "INR")
        assertEquals("₹500", formattedInr)
    }
}

