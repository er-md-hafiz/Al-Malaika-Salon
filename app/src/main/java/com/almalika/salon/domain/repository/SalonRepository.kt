package com.almalika.salon.domain.repository

import com.almalika.salon.domain.model.*
import kotlinx.coroutines.flow.Flow

interface SalonRepository {
    fun getMasterConfig(): Flow<SalonMasterConfig>
    fun getHomeConfig(): Flow<HomeConfig>
    fun getServices(): Flow<List<ServiceCategory>>
    fun getOffers(): Flow<List<Offer>>
    fun getReviews(): Flow<List<Review>>
    fun getGalleryImages(): Flow<List<GalleryImage>>
    fun getBranches(): Flow<List<Branch>>
    fun getBarbers(): Flow<List<Barber>>
    fun getBookingConfig(): Flow<BookingConfig>
    fun getHomeServiceConfig(): Flow<BookingConfig>

    suspend fun refreshConfig(force: Boolean = false): Result<Unit>
    suspend fun isFeatureEnabled(featureKey: String): Boolean
}
