package com.almalika.salon.core.analytics

interface AnalyticsHelper {
    fun logAppOpen(source: String? = null)
    fun logScreenView(screenName: String, screenClass: String? = null)
    fun logViewItem(itemId: String, itemName: String, itemCategory: String, price: Double? = null)
    fun logSearch(searchTerm: String)
    fun logSelectContent(contentType: String, itemId: String)
    fun logBookingStarted(serviceName: String? = null)
    fun logBookingCompleted(serviceName: String? = null, branch: String? = null)
    fun logBookingCancelled(reason: String? = null)
    fun logNotificationOpen(campaign: String? = null, route: String? = null, notificationId: String? = null)
    fun logApiError(endpoint: String, httpCode: Int? = null, errorType: String? = null)
    fun setAnalyticsCollectionEnabled(enabled: Boolean)
}
