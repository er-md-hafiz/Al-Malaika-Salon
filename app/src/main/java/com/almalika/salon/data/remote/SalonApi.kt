package com.almalika.salon.data.remote

import com.almalika.salon.data.remote.dto.*
import retrofit2.http.GET
import retrofit2.http.Url

interface SalonApi {

    @GET("v1/salon/config")
    suspend fun getMasterConfig(): ApiResponse<SalonMasterConfigDto>

    @GET("v1/salon/home")
    suspend fun getHomeConfig(): ApiResponse<HomeConfigDto>

    @GET("v1/salon/services")
    suspend fun getServicesConfig(): ApiResponse<ServicesConfigDto>

    @GET("v1/salon/offers")
    suspend fun getOffersConfig(): ApiResponse<OffersConfigDto>

    @GET("v1/salon/gallery")
    suspend fun getGalleryConfig(): ApiResponse<GalleryConfigDto>

    @GET("v1/salon/locations")
    suspend fun getLocationsConfig(): ApiResponse<LocationsConfigDto>

    @GET("v1/salon/booking")
    suspend fun getBookingConfig(): ApiResponse<BookingConfigDto>

    @GET("v1/salon/home-service")
    suspend fun getHomeServiceConfig(): ApiResponse<BookingConfigDto>

    @GET
    suspend fun getDynamicConfigByUrl(@Url url: String): ApiResponse<Map<String, Any>>
}
