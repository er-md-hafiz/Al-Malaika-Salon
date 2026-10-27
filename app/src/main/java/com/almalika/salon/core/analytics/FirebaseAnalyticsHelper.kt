package com.almalika.salon.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsHelper @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsHelper {

    override fun logAppOpen(source: String?) {
        val bundle = Bundle().apply {
            source?.let { putString("launch_source", sanitize(it, 32)) }
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle)
    }

    override fun logScreenView(screenName: String, screenClass: String?) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, sanitize(screenName, 64))
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass ?: sanitize(screenName, 64))
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    override fun logViewItem(
        itemId: String,
        itemName: String,
        itemCategory: String,
        price: Double?
    ) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, sanitize(itemId, 64))
            putString(FirebaseAnalytics.Param.ITEM_NAME, sanitize(itemName, 64))
            putString(FirebaseAnalytics.Param.ITEM_CATEGORY, sanitize(itemCategory, 64))
            price?.let { putDouble(FirebaseAnalytics.Param.PRICE, it) }
            putString(FirebaseAnalytics.Param.CURRENCY, "INR")
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
    }

    override fun logSearch(searchTerm: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, sanitize(searchTerm, 40))
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
    }

    override fun logSelectContent(contentType: String, itemId: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, sanitize(contentType, 32))
            putString(FirebaseAnalytics.Param.ITEM_ID, sanitize(itemId, 64))
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    override fun logBookingStarted(serviceName: String?) {
        val bundle = Bundle().apply {
            serviceName?.let { putString("service_name", sanitize(it, 64)) }
        }
        firebaseAnalytics.logEvent("booking_started", bundle)
    }

    override fun logBookingCompleted(serviceName: String?, branch: String?) {
        val bundle = Bundle().apply {
            serviceName?.let { putString("service_name", sanitize(it, 64)) }
            branch?.let { putString("branch", sanitize(it, 64)) }
        }
        firebaseAnalytics.logEvent("booking_completed", bundle)
    }

    override fun logBookingCancelled(reason: String?) {
        val bundle = Bundle().apply {
            reason?.let { putString("cancellation_reason", sanitize(it, 64)) }
        }
        firebaseAnalytics.logEvent("booking_cancelled", bundle)
    }

    override fun logNotificationOpen(campaign: String?, route: String?, notificationId: String?) {
        val bundle = Bundle().apply {
            campaign?.let { putString("campaign", sanitize(it, 64)) }
            route?.let { putString("target_route", sanitize(it, 64)) }
            notificationId?.let { putString("notification_id", sanitize(it, 64)) }
        }
        firebaseAnalytics.logEvent("app_notification_open", bundle)
    }

    override fun logApiError(endpoint: String, httpCode: Int?, errorType: String?) {
        val bundle = Bundle().apply {
            putString("endpoint", sanitize(endpoint, 64))
            httpCode?.let { putInt("http_code", it) }
            errorType?.let { putString("error_type", sanitize(it, 32)) }
        }
        firebaseAnalytics.logEvent("api_error", bundle)
    }

    override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

    /**
     * Sanitizes inputs to prevent PII leakage and ensure compliance with Firebase limits.
     */
    private fun sanitize(input: String, maxLength: Int): String {
        return input.trim()
            .take(maxLength)
            .replace(Regex("""[^\w\s\-_./]"""), "")
    }
}
