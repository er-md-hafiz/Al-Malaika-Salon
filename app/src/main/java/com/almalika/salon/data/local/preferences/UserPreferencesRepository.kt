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
        private val KEY_THEME_MODE = androidx.datastore.preferences.core.stringPreferencesKey("theme_display_mode") // "auto", "light", "dark"
        private val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")

        const val THEME_AUTO = "auto"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }

    private val sharedPrefs by lazy {
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    }

    val isOnboardingCompleted: Flow<Boolean> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    val themeMode: Flow<String> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: sharedPrefs.getString("theme_display_mode", null) ?: run {
            val legacyDark = preferences[KEY_DARK_MODE] ?: if (sharedPrefs.contains("dark_mode_enabled")) {
                sharedPrefs.getBoolean("dark_mode_enabled", true)
            } else null
            when (legacyDark) {
                true -> THEME_DARK
                false -> THEME_LIGHT
                null -> THEME_AUTO
            }
        }
    }

    suspend fun setThemeMode(mode: String) {
        val normalized = when (mode.lowercase()) {
            THEME_LIGHT -> THEME_LIGHT
            THEME_DARK -> THEME_DARK
            else -> THEME_AUTO
        }
        // Save to SharedPreferences synchronously / immediately for compatibility
        sharedPrefs.edit()
            .putString("theme_display_mode", normalized)
            .putBoolean("dark_mode_enabled", normalized == THEME_DARK)
            .apply()

        // Also save to DataStore
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = normalized
            if (normalized == THEME_AUTO) {
                preferences.remove(KEY_DARK_MODE)
            } else {
                preferences[KEY_DARK_MODE] = (normalized == THEME_DARK)
            }
        }
    }

    val isDarkMode: Flow<Boolean?> = context.userPreferencesDataStore.data.map { preferences ->
        val mode = preferences[KEY_THEME_MODE] ?: sharedPrefs.getString("theme_display_mode", null)
        when (mode) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            THEME_AUTO -> null
            else -> preferences[KEY_DARK_MODE] ?: if (sharedPrefs.contains("dark_mode_enabled")) {
                sharedPrefs.getBoolean("dark_mode_enabled", true)
            } else null
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        setThemeMode(if (enabled) THEME_DARK else THEME_LIGHT)
    }

    val isNotificationsEnabled: Flow<Boolean> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATIONS] ?: sharedPrefs.getBoolean("notifications_enabled", true)
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        sharedPrefs.edit()
            .putBoolean("notifications_enabled", enabled)
            .apply()

        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS] = enabled
        }
    }

    fun isNotificationsEnabledSync(): Boolean {
        return sharedPrefs.getBoolean("notifications_enabled", true)
    }

    fun getThemeModeSync(): String {
        return sharedPrefs.getString("theme_display_mode", THEME_AUTO) ?: THEME_AUTO
    }
}
