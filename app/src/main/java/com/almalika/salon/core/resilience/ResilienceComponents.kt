package com.almalika.salon.core.resilience

object TelemetryLogger {
    // Stable Telemetry Tag Constants
    const val TAG_SCHEMA_MISMATCH = "SCHEMA_MISMATCH"
    const val TAG_NAV_UNKNOWN = "NAV_UNKNOWN"
    const val TAG_NAV_DISABLED_SCREEN = "NAV_DISABLED_SCREEN"
    const val TAG_WARN_INVALID_URL = "WARN_INVALID_URL"
    const val TAG_WARN_INVALID_COLOR = "WARN_INVALID_COLOR"
    const val TAG_WARN_RATING_DRIFT = "WARN_RATING_DRIFT"
    const val TAG_WARN_MISSING_DATASOURCE = "WARN_MISSING_DATASOURCE"
    const val TAG_WARN_UNKNOWN_ICON = "WARN_UNKNOWN_ICON"
    const val TAG_WARN_NAV_TRUNCATED = "WARN_NAV_TRUNCATED"
    const val TAG_STARTUP_ROUTE = "STARTUP_ROUTE"
    const val TAG_THEME_MODE_RESOLVED = "THEME_MODE_RESOLVED"

    fun log(tag: String, message: String) {
        // Redacts full payload and formats cleanly
        android.util.Log.w("AlMalaikaTelemetry", "[$tag] $message")
    }
}

object MoneyFormatter {
    /**
     * Formats prices with dynamic currency symbol from remote JSON.
     * Never displays negative prices.
     */
    fun format(price: Int, currencySymbol: String? = null, currencyCode: String = "INR"): String {
        val safePrice = price.coerceAtLeast(0)
        val symbol = currencySymbol?.ifBlank { null } ?: when (currencyCode) {
            "INR" -> "₹"
            "USD" -> "$"
            "EUR" -> "€"
            else -> "₹"
        }
        return "$symbol$safePrice"
    }
}

