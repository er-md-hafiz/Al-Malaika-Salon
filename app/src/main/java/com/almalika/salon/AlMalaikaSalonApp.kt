package com.almalika.salon

import android.app.Application
import com.almalika.salon.core.analytics.AnalyticsHelper
import com.almalika.salon.core.crashlytics.CrashlyticsHelper
import com.almalika.salon.data.notification.FcmTokenManager
import com.almalika.salon.data.notification.NotificationChannels
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AlMalaikaSalonApp : Application() {

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    @Inject
    lateinit var crashlyticsHelper: CrashlyticsHelper

    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Initialize Android 8.0+ notification channels
        NotificationChannels.createChannels(this)

        // Set Crashlytics application context keys
        crashlyticsHelper.setCustomKey("app_version_name", BuildConfig.VERSION_NAME)
        crashlyticsHelper.setCustomKey("app_version_code", BuildConfig.VERSION_CODE)
        crashlyticsHelper.log("AlMalaikaSalonApp initialized")

        // Record cold launch in Analytics
        analyticsHelper.logAppOpen("direct")

        // Retrieve and persist FCM device token
        appScope.launch {
            try {
                firebaseMessaging.token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        if (!token.isNullOrBlank()) {
                            appScope.launch {
                                fcmTokenManager.saveToken(token)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                crashlyticsHelper.recordException(e)
            }
        }
    }
}
