package com.almalika.salon.core.config

import android.content.Context
import android.util.Log
import com.almalika.salon.data.remote.config.RemoteConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigPipeline @Inject constructor(
    @ApplicationContext private val context: Context,
    private val baseOkHttpClient: OkHttpClient
) {
    companion object {
        private const val TAG = "ConfigPipeline"
        private const val DEFAULT_REMOTE_URL = "https://er-md-hafiz.github.io/api.almalaikasalon/menu.json"
        private const val CACHE_FILE_NAME = "cached_config.json"
        private const val ETAG_PREF_KEY = "config_etag"
        private const val LAST_FETCH_PREF_KEY = "config_last_fetch"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
        encodeDefaults = true
    }

    private val prefs = context.getSharedPreferences("config_pipeline_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Single source of truth for the entire application (Atomic Swap)
    private val _configState = MutableStateFlow<RemoteConfig>(loadInitialConfig())
    val configState: StateFlow<RemoteConfig> = _configState.asStateFlow()

    init {
        val current = _configState.value
        if (current.remoteConfig.enabled && current.remoteConfig.refreshOnLaunch) {
            scope.launch {
                refreshConfig(force = false)
            }
        }
    }

    /**
     * Cold start pipeline:
     * 1. DataStore / cached_config.json -> parse -> apply
     * 2. Else assets/fallback_config.json -> parse -> apply
     */
    private fun loadInitialConfig(): RemoteConfig {
        // 1. Try disk cache
        val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
        if (cacheFile.exists()) {
            try {
                val cachedJson = cacheFile.readText()
                val parsed = json.decodeFromString<RemoteConfig>(cachedJson)
                val validation = ConfigValidator.validate(parsed)
                if (validation is ValidationResult.Success) {
                    Log.d(TAG, "Loaded valid config from disk cache.")
                    return parsed
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load disk cache: ${e.message}")
            }
        }

        // 2. Try bundled asset
        return loadBundledAsset()
    }

    private fun loadBundledAsset(): RemoteConfig {
        return try {
            val assetJson = context.assets.open("fallback_config.json").bufferedReader().use { it.readText() }
            val parsed = json.decodeFromString<RemoteConfig>(assetJson)
            Log.d(TAG, "Loaded config from bundled assets/fallback_config.json")
            parsed
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bundled fallback_config.json: ${e.message}")
            RemoteConfig()
        }
    }

    suspend fun refreshConfig(force: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        val currentConfig = _configState.value
        val meta = currentConfig.remoteConfig
        if (!meta.enabled) {
            Log.d(TAG, "Remote config refresh disabled in config.")
            return@withContext false
        }

        val lastFetch = prefs.getLong(LAST_FETCH_PREF_KEY, 0L)
        val intervalMs = TimeUnit.HOURS.toMillis(meta.refreshIntervalHours.toLong())
        val now = System.currentTimeMillis()

        if (!force && (now - lastFetch < intervalMs)) {
            Log.d(TAG, "Refresh skipped: interval not elapsed.")
            return@withContext false
        }

        val targetUrl = meta.url.ifBlank { DEFAULT_REMOTE_URL }
        val timeoutSeconds = meta.connectTimeoutSeconds.toLong().coerceAtLeast(5L)
        val client = baseOkHttpClient.newBuilder()
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(meta.readTimeoutSeconds.toLong().coerceAtLeast(10L), TimeUnit.SECONDS)
            .build()

        val etag = prefs.getString(ETAG_PREF_KEY, null)
        val requestBuilder = Request.Builder().url(targetUrl)
        if (!etag.isNullOrBlank()) {
            requestBuilder.header("If-None-Match", etag)
        }

        // Retries: 2 retries with exponential backoff on IOException / 5xx
        var attempt = 0
        val maxAttempts = 3
        while (attempt < maxAttempts) {
            attempt++
            try {
                client.newCall(requestBuilder.build()).execute().use { response ->
                    if (response.code == 304) {
                        Log.d(TAG, "HTTP 304 Not Modified. Keeping current config.")
                        prefs.edit().putLong(LAST_FETCH_PREF_KEY, now).apply()
                        return@withContext true
                    }

                    if (response.isSuccessful) {
                        val bodyString = response.body?.string().orEmpty()
                        val newEtag = response.header("ETag")
                        return@withContext applyRemoteJson(bodyString, newEtag, now)
                    } else if (response.code in 500..599) {
                        Log.w(TAG, "Server error ${response.code}, attempt $attempt/$maxAttempts")
                    } else {
                        Log.w(TAG, "HTTP ${response.code} received, aborting refresh.")
                        return@withContext false
                    }
                }
            } catch (e: IOException) {
                Log.w(TAG, "Network error on attempt $attempt/$maxAttempts: ${e.message}")
            }

            if (attempt < maxAttempts) {
                kotlinx.coroutines.delay(1000L * attempt)
            }
        }

        false
    }

    private fun applyRemoteJson(jsonString: String, etag: String?, timestamp: Long): Boolean {
        return try {
            val parsed = json.decodeFromString<RemoteConfig>(jsonString)
            val validation = ConfigValidator.validate(parsed)
            when (validation) {
                is ValidationResult.Success -> {
                    validation.warnings.forEach { Log.w(TAG, it) }
                    // Atomic update of StateFlow
                    _configState.value = parsed

                    // Persist if cacheOnlyValidatedConfig
                    if (parsed.remoteConfig.cacheOnlyValidatedConfig) {
                        val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
                        cacheFile.writeText(jsonString)
                    }

                    val editor = prefs.edit().putLong(LAST_FETCH_PREF_KEY, timestamp)
                    if (etag != null) {
                        editor.putString(ETAG_PREF_KEY, etag)
                    }
                    editor.apply()
                    Log.i(TAG, "Config atomically applied and cached successfully.")
                    true
                }
                is ValidationResult.Error -> {
                    Log.e(TAG, "Rejected remote config: ${validation.reason}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse remote config: ${e.message}")
            false
        }
    }
}

