package com.almalika.salon.domain.model

data class SalonMasterConfig(
    val schemaVersion: Int,
    val configVersion: Int,
    val salon: SalonInfo,
    val bottomNavigation: List<NavItem>,
    val drawer: List<NavItem>,
    val featureFlags: Map<String, Boolean>,
    val whatsapp: WhatsAppConfig,
    val contact: ContactInfo,
    val share: ShareConfig,
    val legal: LegalConfig
)

data class SalonInfo(
    val name: String,
    val tagline: String,
    val logoUrl: String,
    val phone: String,
    val whatsapp: String,
    val address: String,
    val aboutText: String
)

data class NavItem(
    val id: String,
    val title: String,
    val icon: String,
    val route: String,
    val enabled: Boolean,
    val order: Int
)

data class WhatsAppConfig(
    val enabled: Boolean,
    val number: String,
    val appointmentMessageTemplate: String,
    val homeServiceMessageTemplate: String
)

data class ContactInfo(
    val phone: String,
    val whatsapp: String,
    val email: String,
    val website: String,
    val instagram: String,
    val facebook: String,
    val youtube: String,
    val googleMaps: String
)

data class ShareConfig(
    val enabled: Boolean,
    val message: String,
    val playStoreUrl: String
)

data class LegalConfig(
    val termsUrl: String,
    val privacyUrl: String
)

data class HomeConfig(
    val salon: SalonInfo,
    val sections: List<HomeSection>
)

enum class SectionType {
    HERO,
    QUICK_ACTIONS,
    SERVICES,
    OFFERS,
    GALLERY,
    LOCATIONS,
    ABOUT,
    CONTACT,
    BANNER,
    SPACER,
    UNKNOWN
}

data class HomeSection(
    val id: String,
    val type: SectionType,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val actionText: String,
    val actionRoute: String,
    val sourceUrl: String,
    val itemsToShow: Int,
    val enabled: Boolean,
    val order: Int
)

data class ServiceCategory(
    val id: String,
    val name: String,
    val icon: String,
    val order: Int,
    val services: List<SalonService>
)

data class SalonService(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String,
    val durationMinutes: Int,
    val imageUrl: String,
    val featured: Boolean,
    val bookingEnabled: Boolean,
    val homeServiceAvailable: Boolean
)

enum class DiscountType {
    PERCENTAGE,
    FIXED,
    CUSTOM
}

data class Offer(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val imageUrl: String,
    val discount: String,
    val discountType: DiscountType,
    val validFrom: String,
    val validTill: String,
    val terms: String,
    val ctaText: String,
    val ctaAction: String,
    val enabled: Boolean
) {
    val isExpired: Boolean
        get() {
            if (validTill.isBlank()) return false
            return try {
                val now = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                now > validTill
            } catch (e: Exception) {
                false
            }
        }
}

data class GalleryCategory(
    val id: String,
    val name: String
)

data class GalleryImage(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val imageUrl: String,
    val enabled: Boolean,
    val featured: Boolean,
    val order: Int
)

data class Branch(
    val id: String,
    val name: String,
    val tagline: String,
    val address: String,
    val phone: String,
    val whatsapp: String,
    val latitude: Double?,
    val longitude: Double?,
    val googleMapsUrl: String,
    val openingTime: String,
    val closingTime: String,
    val timings: String,
    val enabled: Boolean,
    val isPrimary: Boolean
)

enum class BookingFieldType {
    TEXT,
    PHONE,
    EMAIL,
    DATE,
    TIME,
    SERVICE,
    BRANCH,
    DROPDOWN,
    MULTILINE,
    ADDRESS
}

data class BookingField(
    val id: String,
    val label: String,
    val type: BookingFieldType,
    val placeholder: String,
    val required: Boolean,
    val order: Int,
    val options: List<String>
)

data class BookingConfig(
    val title: String,
    val subtitle: String,
    val fields: List<BookingField>,
    val enabled: Boolean
)
