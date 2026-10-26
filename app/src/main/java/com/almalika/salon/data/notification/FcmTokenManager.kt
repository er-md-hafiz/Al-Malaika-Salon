package com.almalika.salon.data.notification

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.fcmDataStore: DataStore<Preferences> by preferencesDataStore(name = "fcm_settings")

@Singleton
class FcmTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fcmTokenKey = stringPreferencesKey("fcm_device_token")

    val fcmToken: Flow<String?> = context.fcmDataStore.data.map { preferences ->
        preferences[fcmTokenKey]
    }

    suspend fun saveToken(token: String) {
        context.fcmDataStore.edit { preferences ->
            preferences[fcmTokenKey] = token
        }
    }

    suspend fun getToken(): String? {
        return fcmToken.first()
    }
}
