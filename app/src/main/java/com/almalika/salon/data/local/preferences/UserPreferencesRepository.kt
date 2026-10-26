package com.almalika.salon.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
        private val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    val isDarkMode: Flow<Boolean?> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[KEY_DARK_MODE]
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_DARK_MODE] = enabled
        }
    }

    val isNotificationsEnabled: Flow<Boolean?> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATIONS]
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS] = enabled
        }
    }
}
