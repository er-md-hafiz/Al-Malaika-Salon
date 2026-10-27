package com.almalika.salon.data.repository

import com.almalika.salon.data.local.asset.AssetConfigReader
import com.almalika.salon.data.local.dao.SalonConfigDao
import com.almalika.salon.data.local.entity.HomeEntity
import com.almalika.salon.data.local.entity.MenuEntity
import com.almalika.salon.data.mapper.*
import com.almalika.salon.data.remote.SalonApi
import com.almalika.salon.data.remote.dto.MenuResponseDto
import com.almalika.salon.domain.model.*
import com.almalika.salon.domain.repository.SalonRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalonRepositoryImpl @Inject constructor(
    private val api: SalonApi,
    private val dao: SalonConfigDao,
    private val assetReader: AssetConfigReader,
    moshi: Moshi
) : SalonRepository {

    private val menuAdapter = moshi.adapter(MenuResponseDto::class.java)

    private fun parseMenu(jsonString: String?): MenuResponseDto {
        if (jsonString.isNullOrBlank()) {
            return parseFallback()
        }
        return try {
            menuAdapter.fromJson(jsonString) ?: parseFallback()
        } catch (_: Exception) {
            parseFallback()
        }
    }

    private fun parseFallback(): MenuResponseDto {
        return try {
            val fallbackJson = assetReader.readAssetJson("config/menu_fallback.json")
            menuAdapter.fromJson(fallbackJson) ?: MenuResponseDto()
        } catch (_: Exception) {
            MenuResponseDto()
        }
    }

    override fun getMasterConfig(): Flow<SalonMasterConfig> {
        return dao.observeMenu().map { entity ->
            parseMenu(entity?.json).toDomain()
        }
    }

    override fun getHomeConfig(): Flow<HomeConfig> {
        return dao.observeMenu().map { entity ->
            parseMenu(entity?.json).toHomeConfig()
        }
    }

    override fun getServices(): Flow<List<ServiceCategory>> {
        return dao.observeMenu().map { entity ->
            parseMenu(entity?.json).toServiceCategories()
        }
    }

    override fun getOffers(): Flow<List<Offer>> {
        return dao.observeMenu().map { entity ->
            parseMenu(entity?.json).toOffers()
        }
    }

    override fun getReviews(): Flow<List<Review>> {
        return dao.observeMenu().map { entity ->
            parseMenu(entity?.json).toReviews()
        }
    }

    override fun getGalleryImages(): Flow<List<GalleryImage>> {
        return dao.observeMenu().map { entity ->
            parseMenu(entity?.json).toGalleryImages()
        }
    }

    override fun getBranches(): Flow<List<Branch>> {
        return dao.observeMenu().map { entity ->
            parseMenu(entity?.json).toBranches()
        }
    }

    override fun getBookingConfig(): Flow<BookingConfig> {
        return dao.observeMenu().map { entity ->
            toBookingConfig()
        }
    }

    override fun getHomeServiceConfig(): Flow<BookingConfig> {
        return dao.observeMenu().map { entity ->
            parseMenu(entity?.json).toHomeServiceConfig()
        }
    }

    private var lastRefreshTimestamp: Long = 0L

    companion object {
        const val PREFERRED_CONFIG_URL = "https://er-md-hafiz.github.io/api.almalaikasalon/menu.json"
        const val FALLBACK_RAW_GITHUB_URL = "https://raw.githubusercontent.com/er-md-hafiz/api.almalaikasalon/main/menu.json"

        /**
         * Resolves the configuration URL using remoteConfig.url as source of truth.
         * Enforces that GitHub blob HTML pages are never used as JSON endpoints.
         */
        fun resolveRemoteConfigUrl(configuredUrl: String?): String {
            val trimmed = configuredUrl?.trim().orEmpty()
            return when {
                trimmed.isBlank() -> PREFERRED_CONFIG_URL
                trimmed.contains("/blob/", ignoreCase = true) && trimmed.contains("github.com", ignoreCase = true) -> {
                    // Never attempt to parse GitHub HTML web page as JSON; fallback to preferred JSON endpoint
                    PREFERRED_CONFIG_URL
                }
                else -> trimmed
            }
        }

        /**
         * Validates remote JSON content: HTTP body, JSON syntax, schema version, and required sections.
         */
        fun validateConfiguration(dto: MenuResponseDto?, rawJson: String): Result<MenuResponseDto> {
            if (dto == null) {
                return Result.failure(IllegalStateException("Invalid JSON syntax: deserialized object is null"))
            }

            val trimmedJson = rawJson.trimStart()
            if (trimmedJson.startsWith("<!DOCTYPE", ignoreCase = true) ||
                trimmedJson.startsWith("<html", ignoreCase = true) ||
                trimmedJson.startsWith("<head", ignoreCase = true)
            ) {
                return Result.failure(IllegalStateException("Received HTML document instead of valid JSON configuration"))
            }

            // Essential schema sections validation
            val hasRequiredSections = (dto.salon != null || dto.metadata != null) &&
                    (dto.navigation != null || dto.home != null)
            if (!hasRequiredSections) {
                return Result.failure(IllegalStateException("Invalid remote configuration: missing critical schema sections (salon/metadata/navigation)"))
            }

            // Schema version validation
            val schemaVer = dto.metadata?.schemaVersion?.trim()
            if (schemaVer.isNullOrBlank()) {
                return Result.failure(IllegalStateException("Missing schemaVersion in configuration metadata"))
            }

            return Result.success(dto)
        }
    }

    override suspend fun refreshConfig(force: Boolean): Result<Unit> {
        return try {
            val currentMenuEntity = dao.getMenu()
            val currentConfig = parseMenu(currentMenuEntity?.json)
            val remoteSettings = currentConfig.remoteConfig

            // Check if remote configuration refresh is enabled
            if (remoteSettings?.enabled == false && !force) {
                return Result.success(Unit)
            }

            // Check refresh interval unless forced
            val intervalMs = (remoteSettings?.refreshIntervalHours ?: 6) * 3600 * 1000L
            val now = System.currentTimeMillis()
            if (!force && lastRefreshTimestamp > 0 && (now - lastRefreshTimestamp) < intervalMs) {
                return Result.success(Unit)
            }

            // Determine configured URLs: primary from remoteConfig.url, alternative raw GitHub JSON fallback
            val primaryUrl = resolveRemoteConfigUrl(remoteSettings?.url)
            val urlsToTry = if (primaryUrl == FALLBACK_RAW_GITHUB_URL) {
                listOf(primaryUrl)
            } else {
                listOf(primaryUrl, FALLBACK_RAW_GITHUB_URL)
            }

            var validatedDto: MenuResponseDto? = null
            var lastException: Exception? = null

            for (targetUrl in urlsToTry) {
                try {
                    val response = api.getMenuConfigRaw(targetUrl)
                    if (!response.isSuccessful) {
                        throw IllegalStateException("HTTP ${response.code()} error loading configuration from $targetUrl")
                    }
                    val bodyString = response.body()?.string()
                    if (bodyString.isNullOrBlank()) {
                        throw IllegalStateException("Empty response body received from $targetUrl")
                    }

                    val parsedDto = menuAdapter.fromJson(bodyString)
                    val validationResult = validateConfiguration(parsedDto, bodyString)
                    if (validationResult.isSuccess) {
                        validatedDto = validationResult.getOrThrow()
                        break
                    } else {
                        throw validationResult.exceptionOrNull() ?: IllegalStateException("Configuration validation failed")
                    }
                } catch (e: Exception) {
                    lastException = e
                }
            }

            if (validatedDto == null) {
                // Keep existing cache (last-known-good) and bundled fallback without crashing
                return Result.failure(lastException ?: IllegalStateException("Failed to load valid configuration from remote endpoints"))
            }

            // Cache ONLY successfully validated configuration atomically
            val json = menuAdapter.toJson(validatedDto)
            val menuEntity = MenuEntity(json = json)
            dao.replaceConfig(
                menu = menuEntity,
                home = HomeEntity(json = json),
                serviceCategories = emptyList(),
                offers = emptyList(),
                galleryImages = emptyList(),
                branches = emptyList()
            )
            lastRefreshTimestamp = now
            Result.success(Unit)
        } catch (e: Exception) {
            // Keep existing cache and do not crash
            Result.failure(e)
        }
    }

    override suspend fun isFeatureEnabled(featureKey: String): Boolean {
        val entity = dao.getMenu()
        val dto = parseMenu(entity?.json)
        return when (featureKey) {
            "homeService" -> dto.homeServiceBooking?.enabled ?: false
            "franchise" -> dto.franchise?.enabled ?: false
            "offers" -> !dto.offers.isNullOrEmpty()
            "gallery" -> !dto.galleryItems.isNullOrEmpty()
            else -> true
        }
    }
}
