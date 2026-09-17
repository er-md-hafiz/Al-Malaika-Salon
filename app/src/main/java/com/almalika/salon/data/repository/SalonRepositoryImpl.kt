package com.almalika.salon.data.repository

import com.almalika.salon.data.local.asset.AssetConfigReader
import com.almalika.salon.data.local.dao.SalonConfigDao
import com.almalika.salon.data.local.entity.*
import com.almalika.salon.data.mapper.toDomain
import com.almalika.salon.data.remote.SalonApi
import com.almalika.salon.data.remote.dto.*
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

    private val masterAdapter = moshi.adapter(SalonMasterConfigDto::class.java)
    private val homeAdapter = moshi.adapter(HomeConfigDto::class.java)
    private val servicesAdapter = moshi.adapter(ServicesConfigDto::class.java)
    private val offersAdapter = moshi.adapter(OffersConfigDto::class.java)
    private val galleryAdapter = moshi.adapter(GalleryConfigDto::class.java)
    private val locationsAdapter = moshi.adapter(LocationsConfigDto::class.java)
    private val bookingAdapter = moshi.adapter(BookingConfigDto::class.java)

    override fun getMasterConfig(): Flow<SalonMasterConfig> {
        return dao.observeMenu().map { entity ->
            val json = entity?.json ?: assetReader.readAssetJson("config/menu_fallback.json")
            val dto = try {
                masterAdapter.fromJson(json)
            } catch (e: Exception) {
                masterAdapter.fromJson(assetReader.readAssetJson("config/menu_fallback.json"))
            }
            (dto ?: SalonMasterConfigDto()).toDomain()
        }
    }

    override fun getHomeConfig(): Flow<HomeConfig> {
        return dao.observeHome().map { entity ->
            val json = entity?.json ?: assetReader.readAssetJson("config/home_fallback.json")
            val dto = try {
                homeAdapter.fromJson(json)
            } catch (e: Exception) {
                homeAdapter.fromJson(assetReader.readAssetJson("config/home_fallback.json"))
            }
            (dto ?: HomeConfigDto()).toDomain()
        }
    }

    override fun getServices(): Flow<List<ServiceCategory>> {
        return dao.observeServiceCategories().map { list ->
            if (list.isEmpty()) {
                val fallbackJson = assetReader.readAssetJson("config/services_fallback.json")
                val dto = servicesAdapter.fromJson(fallbackJson)
                dto?.categories?.map { it.toDomain() } ?: emptyList()
            } else {
                list.mapNotNull { entity ->
                    try {
                        val catDto = moshi.adapter(ServiceCategoryDto::class.java).fromJson(entity.json)
                        catDto?.toDomain()
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.order }
            }
        }
    }

    override fun getOffers(): Flow<List<Offer>> {
        return dao.observeOffers().map { list ->
            if (list.isEmpty()) {
                val fallbackJson = assetReader.readAssetJson("config/offers_fallback.json")
                val dto = offersAdapter.fromJson(fallbackJson)
                dto?.offers?.map { it.toDomain() } ?: emptyList()
            } else {
                list.mapNotNull { entity ->
                    try {
                        val offerDto = moshi.adapter(OfferDto::class.java).fromJson(entity.json)
                        offerDto?.toDomain()
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }

    override fun getGalleryImages(): Flow<List<GalleryImage>> {
        return dao.observeGalleryImages().map { list ->
            if (list.isEmpty()) {
                val fallbackJson = assetReader.readAssetJson("config/gallery_fallback.json")
                val dto = galleryAdapter.fromJson(fallbackJson)
                dto?.images?.map { it.toDomain() } ?: emptyList()
            } else {
                list.mapNotNull { entity ->
                    try {
                        val imgDto = moshi.adapter(GalleryImageDto::class.java).fromJson(entity.json)
                        imgDto?.toDomain()
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.order }
            }
        }
    }

    override fun getBranches(): Flow<List<Branch>> {
        return dao.observeBranches().map { list ->
            if (list.isEmpty()) {
                val fallbackJson = assetReader.readAssetJson("config/locations_fallback.json")
                val dto = locationsAdapter.fromJson(fallbackJson)
                dto?.branches?.map { it.toDomain() } ?: emptyList()
            } else {
                list.mapNotNull { entity ->
                    try {
                        val branchDto = moshi.adapter(BranchDto::class.java).fromJson(entity.json)
                        branchDto?.toDomain()
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }

    override fun getBookingConfig(): Flow<BookingConfig> {
        return dao.observeHome().map {
            // Using booking form config from fallback or dynamic URL
            val json = assetReader.readAssetJson("config/booking_fallback.json")
            val dto = try {
                bookingAdapter.fromJson(json)
            } catch (e: Exception) {
                null
            }
            (dto ?: BookingConfigDto()).toDomain()
        }
    }

    override fun getHomeServiceConfig(): Flow<BookingConfig> {
        return dao.observeHome().map {
            val json = assetReader.readAssetJson("config/home_service_fallback.json")
            val dto = try {
                bookingAdapter.fromJson(json)
            } catch (e: Exception) {
                null
            }
            (dto ?: BookingConfigDto()).toDomain()
        }
    }

    override suspend fun refreshConfig(force: Boolean): Result<Unit> {
        return try {
            val masterResponse = api.getMasterConfig()
            if (masterResponse.success && masterResponse.data != null) {
                val masterDto = masterResponse.data
                val serverVersion = masterDto.configVersion ?: 1

                val currentMenu = dao.getMenu()
                val currentVersion = if (currentMenu != null) {
                    try {
                        masterAdapter.fromJson(currentMenu.json)?.configVersion ?: 0
                    } catch (e: Exception) {
                        0
                    }
                } else {
                    0
                }

                if (!force && currentVersion >= serverVersion) {
                    return Result.success(Unit)
                }

                val menuEntity = MenuEntity(json = masterAdapter.toJson(masterDto))

                // Fetch Home
                val homeDto = try {
                    api.getHomeConfig().data
                } catch (e: Exception) {
                    homeAdapter.fromJson(assetReader.readAssetJson("config/home_fallback.json"))
                }
                val homeEntity = HomeEntity(json = homeAdapter.toJson(homeDto ?: HomeConfigDto()))

                // Fetch Services
                val servicesDto = try {
                    api.getServicesConfig().data
                } catch (e: Exception) {
                    servicesAdapter.fromJson(assetReader.readAssetJson("config/services_fallback.json"))
                }
                val categoryEntities = servicesDto?.categories?.map {
                    ServiceCategoryEntity(
                        id = it.id,
                        json = moshi.adapter(ServiceCategoryDto::class.java).toJson(it)
                    )
                } ?: emptyList()

                // Fetch Offers
                val offersDto = try {
                    api.getOffersConfig().data
                } catch (e: Exception) {
                    offersAdapter.fromJson(assetReader.readAssetJson("config/offers_fallback.json"))
                }
                val offerEntities = offersDto?.offers?.map {
                    OfferEntity(
                        id = it.id,
                        json = moshi.adapter(OfferDto::class.java).toJson(it)
                    )
                } ?: emptyList()

                // Fetch Gallery
                val galleryDto = try {
                    api.getGalleryConfig().data
                } catch (e: Exception) {
                    galleryAdapter.fromJson(assetReader.readAssetJson("config/gallery_fallback.json"))
                }
                val galleryEntities = galleryDto?.images?.map {
                    GalleryImageEntity(
                        id = it.id,
                        json = moshi.adapter(GalleryImageDto::class.java).toJson(it)
                    )
                } ?: emptyList()

                // Fetch Locations
                val locationsDto = try {
                    api.getLocationsConfig().data
                } catch (e: Exception) {
                    locationsAdapter.fromJson(assetReader.readAssetJson("config/locations_fallback.json"))
                }
                val branchEntities = locationsDto?.branches?.map {
                    BranchEntity(
                        id = it.id,
                        json = moshi.adapter(BranchDto::class.java).toJson(it)
                    )
                } ?: emptyList()

                // Atomically update local cache
                dao.replaceConfig(
                    menu = menuEntity,
                    home = homeEntity,
                    serviceCategories = categoryEntities,
                    offers = offerEntities,
                    galleryImages = galleryEntities,
                    branches = branchEntities
                )
                Result.success(Unit)
            } else {
                Result.failure(Exception(masterResponse.message ?: "Master config fetch failed"))
            }
        } catch (e: Exception) {
            // Keep previous cache and fail gracefully without crashing
            Result.failure(e)
        }
    }

    override suspend fun isFeatureEnabled(featureKey: String): Boolean {
        val menu = dao.getMenu() ?: return true
        return try {
            val dto = masterAdapter.fromJson(menu.json)
            dto?.featureFlags?.get(featureKey) ?: true
        } catch (e: Exception) {
            true
        }
    }
}
