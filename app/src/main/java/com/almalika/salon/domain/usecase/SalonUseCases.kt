package com.almalika.salon.domain.usecase

import com.almalika.salon.domain.model.*
import com.almalika.salon.domain.repository.SalonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMasterConfigUseCase @Inject constructor(
    private val repository: SalonRepository
) {
    operator fun invoke(): Flow<SalonMasterConfig> = repository.getMasterConfig()
}

class GetHomeConfigUseCase @Inject constructor(
    private val repository: SalonRepository
) {
    operator fun invoke(): Flow<HomeConfig> = repository.getHomeConfig()
}

class GetServicesUseCase @Inject constructor(
    private val repository: SalonRepository
) {
    operator fun invoke(): Flow<List<ServiceCategory>> = repository.getServices()
}

class GetOffersUseCase @Inject constructor(
    private val repository: SalonRepository
) {
    operator fun invoke(filterExpired: Boolean = true): Flow<List<Offer>> {
        return repository.getOffers().map { list ->
            list.filter { offer ->
                offer.enabled && (!filterExpired || !offer.isExpired)
            }
        }
    }
}

class GetReviewsUseCase @Inject constructor(
    private val repository: SalonRepository
) {
    operator fun invoke(): Flow<List<Review>> = repository.getReviews()
}

class GetGalleryUseCase @Inject constructor(
    private val repository: SalonRepository
) {
    operator fun invoke(categoryId: String? = null): Flow<List<GalleryImage>> {
        return repository.getGalleryImages().map { list ->
            val enabledList = list.filter { it.enabled }.sortedBy { it.order }
            if (categoryId == null || categoryId == "all") {
                enabledList
            } else {
                enabledList.filter { it.categoryId.equals(categoryId, ignoreCase = true) }
            }
        }
    }
}

class GetLocationsUseCase @Inject constructor(
    private val repository: SalonRepository
) {
    operator fun invoke(): Flow<List<Branch>> {
        return repository.getBranches().map { list ->
            list.filter { it.enabled }
        }
    }
}

class GetBookingConfigUseCase @Inject constructor(
    private val repository: SalonRepository
) {
    operator fun invoke(): Flow<BookingConfig> = repository.getBookingConfig()
}

class GetHomeServiceConfigUseCase @Inject constructor(
    private val repository: SalonRepository
) {
    operator fun invoke(): Flow<BookingConfig> = repository.getHomeServiceConfig()
}

class RefreshConfigUseCase @Inject constructor(
    private val repository: SalonRepository
) {
    suspend operator fun invoke(force: Boolean = false): Result<Unit> {
        return repository.refreshConfig(force)
    }
}

class CheckFeatureFlagUseCase @Inject constructor(
    private val repository: SalonRepository
) {
    suspend operator fun invoke(flagKey: String): Boolean {
        return repository.isFeatureEnabled(flagKey)
    }
}

class FormatWhatsAppMessageUseCase @Inject constructor() {
    operator fun invoke(
        template: String,
        salonName: String,
        values: Map<String, String>
    ): String {
        var result = template.replace("{salonName}", salonName)
        values.forEach { (key, value) ->
            result = result.replace("{$key}", value)
        }
        return result
    }
}
