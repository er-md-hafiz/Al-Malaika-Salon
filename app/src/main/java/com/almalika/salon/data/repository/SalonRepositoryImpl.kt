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
    private val moshi: Moshi
) : SalonRepository {

    private val menuAdapter = moshi.adapter(MenuResponseDto::class.java)

    private fun parseMenu(jsonString: String?): MenuResponseDto {
        if (jsonString.isNullOrBlank()) {
            return parseFallback()
        }
        return try {
            menuAdapter.fromJson(jsonString) ?: parseFallback()
        } catch (e: Exception) {
            parseFallback()
        }
    }

    private fun parseFallback(): MenuResponseDto {
        return try {
            val fallbackJson = assetReader.readAssetJson("config/menu_fallback.json")
            menuAdapter.fromJson(fallbackJson) ?: MenuResponseDto()
        } catch (e: Exception) {
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
            parseMenu(entity?.json).toBookingConfig()
        }
    }

    override fun getHomeServiceConfig(): Flow<BookingConfig> {
        return dao.observeMenu().map { entity ->
            parseMenu(entity?.json).toHomeServiceConfig()
        }
    }

    override suspend fun refreshConfig(force: Boolean): Result<Unit> {
        return try {
            val menuDto = api.getMenuConfig()
            val json = menuAdapter.toJson(menuDto)
            val menuEntity = MenuEntity(json = json)
            dao.replaceConfig(
                menu = menuEntity,
                home = HomeEntity(json = json),
                serviceCategories = emptyList(),
                offers = emptyList(),
                galleryImages = emptyList(),
                branches = emptyList()
            )
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
