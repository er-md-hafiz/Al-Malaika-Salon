package com.almalika.salon.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: T? = null,
    @Json(name = "errorCode") val errorCode: String? = null,
    @Json(name = "timestamp") val timestamp: String? = null,
    @Json(name = "version") val version: String? = null
)

@JsonClass(generateAdapter = true)
data class SalonMasterConfigDto(
    @Json(name = "schemaVersion") val schemaVersion: Int? = 1,
    @Json(name = "configVersion") val configVersion: Int? = 1,
    @Json(name = "salon") val salon: SalonInfoDto? = null,
    @Json(name = "bottomNavigation") val bottomNavigation: List<NavItemDto>? = null,
    @Json(name = "drawer") val drawer: List<NavItemDto>? = null,
    @Json(name = "featureFlags") val featureFlags: Map<String, Boolean>? = null,
    @Json(name = "whatsapp") val whatsapp: WhatsAppConfigDto? = null,
    @Json(name = "contact") val contact: ContactInfoDto? = null,
    @Json(name = "share") val share: ShareConfigDto? = null,
    @Json(name = "legal") val legal: LegalConfigDto? = null,
    @Json(name = "screens") val screens: Map<String, ScreenConfigDto>? = null
)

@JsonClass(generateAdapter = true)
data class SalonInfoDto(
    @Json(name = "name") val name: String? = null,
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "logoUrl") val logoUrl: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "whatsapp") val whatsapp: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "aboutText") val aboutText: String? = null
)

@JsonClass(generateAdapter = true)
data class NavItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "icon") val icon: String,
    @Json(name = "route") val route: String,
    @Json(name = "enabled") val enabled: Boolean = true,
    @Json(name = "order") val order: Int = 0
)

@JsonClass(generateAdapter = true)
data class ScreenConfigDto(
    @Json(name = "enabled") val enabled: Boolean = true,
    @Json(name = "configUrl") val configUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class WhatsAppConfigDto(
    @Json(name = "enabled") val enabled: Boolean = true,
    @Json(name = "number") val number: String? = null,
    @Json(name = "appointmentMessage") val appointmentMessage: String? = null,
    @Json(name = "homeServiceMessage") val homeServiceMessage: String? = null
)

@JsonClass(generateAdapter = true)
data class ContactInfoDto(
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "whatsapp") val whatsapp: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "website") val website: String? = null,
    @Json(name = "instagram") val instagram: String? = null,
    @Json(name = "facebook") val facebook: String? = null,
    @Json(name = "youtube") val youtube: String? = null,
    @Json(name = "googleMaps") val googleMaps: String? = null
)

@JsonClass(generateAdapter = true)
data class ShareConfigDto(
    @Json(name = "enabled") val enabled: Boolean = true,
    @Json(name = "message") val message: String? = null,
    @Json(name = "playStoreUrl") val playStoreUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class LegalConfigDto(
    @Json(name = "termsUrl") val termsUrl: String? = null,
    @Json(name = "privacyUrl") val privacyUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class HomeConfigDto(
    @Json(name = "salon") val salon: SalonInfoDto? = null,
    @Json(name = "sections") val sections: List<HomeSectionDto>? = null
)

@JsonClass(generateAdapter = true)
data class HomeSectionDto(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "title") val title: String? = null,
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "actionText") val actionText: String? = null,
    @Json(name = "actionRoute") val actionRoute: String? = null,
    @Json(name = "sourceUrl") val sourceUrl: String? = null,
    @Json(name = "itemsToShow") val itemsToShow: Int? = null,
    @Json(name = "enabled") val enabled: Boolean = true,
    @Json(name = "order") val order: Int = 0
)

@JsonClass(generateAdapter = true)
data class ServicesConfigDto(
    @Json(name = "categories") val categories: List<ServiceCategoryDto>? = null
)

@JsonClass(generateAdapter = true)
data class ServiceCategoryDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "order") val order: Int = 0,
    @Json(name = "services") val services: List<ServiceDto>? = null
)

@JsonClass(generateAdapter = true)
data class ServiceDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "price") val price: Double = 0.0,
    @Json(name = "currency") val currency: String = "INR",
    @Json(name = "durationMinutes") val durationMinutes: Int = 30,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "featured") val featured: Boolean = false,
    @Json(name = "bookingEnabled") val bookingEnabled: Boolean = true,
    @Json(name = "homeServiceAvailable") val homeServiceAvailable: Boolean = false
)

@JsonClass(generateAdapter = true)
data class OffersConfigDto(
    @Json(name = "offers") val offers: List<OfferDto>? = null
)

@JsonClass(generateAdapter = true)
data class OfferDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "discount") val discount: String? = null,
    @Json(name = "discountType") val discountType: String? = null,
    @Json(name = "validFrom") val validFrom: String? = null,
    @Json(name = "validTill") val validTill: String? = null,
    @Json(name = "terms") val terms: String? = null,
    @Json(name = "ctaText") val ctaText: String? = null,
    @Json(name = "ctaAction") val ctaAction: String? = null,
    @Json(name = "enabled") val enabled: Boolean = true
)

@JsonClass(generateAdapter = true)
data class GalleryConfigDto(
    @Json(name = "categories") val categories: List<GalleryCategoryDto>? = null,
    @Json(name = "images") val images: List<GalleryImageDto>? = null
)

@JsonClass(generateAdapter = true)
data class GalleryCategoryDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class GalleryImageDto(
    @Json(name = "id") val id: String,
    @Json(name = "categoryId") val categoryId: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "thumbnailUrl") val thumbnailUrl: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "enabled") val enabled: Boolean = true,
    @Json(name = "featured") val featured: Boolean = false,
    @Json(name = "order") val order: Int = 0
)

@JsonClass(generateAdapter = true)
data class LocationsConfigDto(
    @Json(name = "branches") val branches: List<BranchDto>? = null
)

@JsonClass(generateAdapter = true)
data class BranchDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "address") val address: String,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "whatsapp") val whatsapp: String? = null,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "googleMapsUrl") val googleMapsUrl: String? = null,
    @Json(name = "openingTime") val openingTime: String? = null,
    @Json(name = "closingTime") val closingTime: String? = null,
    @Json(name = "timings") val timings: String? = null,
    @Json(name = "enabled") val enabled: Boolean = true,
    @Json(name = "isPrimary") val isPrimary: Boolean = false
)

@JsonClass(generateAdapter = true)
data class BookingConfigDto(
    @Json(name = "title") val title: String? = null,
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "fields") val fields: List<BookingFieldDto>? = null,
    @Json(name = "enabled") val enabled: Boolean = true
)

@JsonClass(generateAdapter = true)
data class BookingFieldDto(
    @Json(name = "id") val id: String,
    @Json(name = "label") val label: String,
    @Json(name = "type") val type: String,
    @Json(name = "placeholder") val placeholder: String? = null,
    @Json(name = "required") val required: Boolean = true,
    @Json(name = "order") val order: Int = 0,
    @Json(name = "options") val options: List<String>? = null
)
