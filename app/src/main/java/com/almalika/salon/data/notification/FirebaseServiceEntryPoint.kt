package com.almalika.salon.data.notification

import com.almalika.salon.core.analytics.AnalyticsHelper
import com.almalika.salon.core.crashlytics.CrashlyticsHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirebaseServiceEntryPoint {
    fun getFcmTokenManager(): FcmTokenManager
    fun getAnalyticsHelper(): AnalyticsHelper
    fun getCrashlyticsHelper(): CrashlyticsHelper
    fun getUserPreferencesRepository(): com.almalika.salon.data.local.preferences.UserPreferencesRepository
}
