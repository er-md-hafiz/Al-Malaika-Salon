package com.almalika.salon.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Top‑level response from GET /v1/salon/config
 */
@JsonClass(generateAdapter = true)
data class SalonConfigResponseDto(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: SalonConfigDataDto
)

@JsonClass(generateAdapter = true)
data class SalonConfigDataDto(
    @Json(name = "configVersion") val configVersion: Int,
    @Json(name = "menuUrl") val menuUrl: String,
    @Json(name = "homeUrl") val homeUrl: String?,
    @Json(name = "servicesUrl") val servicesUrl: String?,
    @Json(name = "offersUrl") val offersUrl: String?,
    @Json(name = "galleryUrl") val galleryUrl: String?,
    @Json(name = "locationsUrl") val locationsUrl: String?,
    @Json(name = "bookingUrl") val bookingUrl: String?
    // Extend with other URLs if needed
)
