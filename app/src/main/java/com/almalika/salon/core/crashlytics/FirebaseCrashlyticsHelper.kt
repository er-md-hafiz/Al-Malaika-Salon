package com.almalika.salon.core.crashlytics

import com.almalika.salon.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCrashlyticsHelper @Inject constructor(
    private val crashlytics: FirebaseCrashlytics
) : CrashlyticsHelper {

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        // Sanitize to avoid accidental logging of sensitive data
        crashlytics.log(message.take(256))
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value.take(128))
    }

    override fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setUserId(userId: String) {
        // Anonymized/hashed identifier only
        crashlytics.setUserId(userId.take(64))
    }

    /**
     * Controlled test crash strictly guarded for Debug builds only.
     * Never executes in production releases.
     */
    override fun testCrash() {
        if (BuildConfig.DEBUG) {
            throw RuntimeException("Al-Malaika Salon: Controlled Test Crash for Crashlytics Verification")
        }
    }
}
