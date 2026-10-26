package com.almalika.salon.di

import android.content.Context
import com.almalika.salon.core.analytics.AnalyticsHelper
import com.almalika.salon.core.analytics.FirebaseAnalyticsHelper
import com.almalika.salon.core.crashlytics.CrashlyticsHelper
import com.almalika.salon.core.crashlytics.FirebaseCrashlyticsHelper
import com.almalika.salon.core.performance.FirebasePerformanceHelper
import com.almalika.salon.core.performance.PerformanceHelper
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.perf.FirebasePerformance
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseProviderModule {

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
        return FirebaseCrashlytics.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebasePerformance(): FirebasePerformance {
        return FirebasePerformance.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseBindingModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsHelper(
        impl: FirebaseAnalyticsHelper
    ): AnalyticsHelper

    @Binds
    @Singleton
    abstract fun bindCrashlyticsHelper(
        impl: FirebaseCrashlyticsHelper
    ): CrashlyticsHelper

    @Binds
    @Singleton
    abstract fun bindPerformanceHelper(
        impl: FirebasePerformanceHelper
    ): PerformanceHelper
}
