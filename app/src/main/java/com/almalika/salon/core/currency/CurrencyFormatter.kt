package com.almalika.salon.core.currency

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {

    fun format(
        amount: Double,
        currencyCode: String? = "INR",
        localeTag: String? = "en-IN"
    ): String {
        return try {
            val locale = if (!localeTag.isNullOrBlank()) {
                Locale.forLanguageTag(localeTag.replace("_", "-"))
            } else {
                Locale.Builder().setLanguage("en").setRegion("IN").build()
            }
            val formatter = NumberFormat.getCurrencyInstance(locale)
            val code = if (!currencyCode.isNullOrBlank()) currencyCode.trim() else "INR"
            try {
                formatter.currency = Currency.getInstance(code)
            } catch (_: Exception) {
                // If invalid currency code, fall back to INR
                formatter.currency = Currency.getInstance("INR")
            }
            if (amount % 1.0 == 0.0) {
                formatter.maximumFractionDigits = 0
            }
            formatter.format(amount)
        } catch (_: Exception) {
            val symbol = if (currencyCode.equals("INR", ignoreCase = true)) "₹" else "$currencyCode "
            if (amount % 1.0 == 0.0) {
                "$symbol${amount.toLong()}"
            } else {
                "$symbol${"%.2f".format(amount)}"
            }
        }
    }
}

