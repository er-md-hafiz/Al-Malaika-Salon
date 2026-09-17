package com.almalika.salon.data.mapper

import com.almalika.salon.data.remote.dto.*
import com.almalika.salon.domain.model.*

fun SalonMasterConfigDto.toDomain(): SalonMasterConfig {
    return SalonMasterConfig(
        schemaVersion = schemaVersion ?: 1,
        configVersion = configVersion ?: 1,
        salon = salon?.toDomain() ?: SalonInfo("", "", "", "", "", "", ""),
        bottomNavigation = bottomNavigation?.map { it.toDomain() } ?: emptyList(),
        drawer = drawer?.map { it.toDomain() } ?: emptyList(),
        featureFlags = featureFlags ?: emptyMap(),
        whatsapp = whatsapp?.toDomain() ?: WhatsAppConfig(true, "", "", ""),
        contact = contact?.toDomain() ?: ContactInfo("", "", "", "", "", "", "", ""),
        share = share?.toDomain() ?: ShareConfig(true, "", ""),
        legal = legal?.toDomain() ?: LegalConfig("", "")
    )
}

fun SalonInfoDto.toDomain(): SalonInfo {
    return SalonInfo(
        name = name.orEmpty(),
        tagline = tagline.orEmpty(),
        logoUrl = logoUrl.orEmpty(),
        phone = phone.orEmpty(),
        whatsapp = whatsapp.orEmpty(),
        address = address.orEmpty(),
        aboutText = aboutText.orEmpty()
    )
}

fun NavItemDto.toDomain(): NavItem {
    return NavItem(
        id = id,
        title = title,
        icon = icon,
        route = route,
        enabled = enabled,
        order = order
    )
}

fun WhatsAppConfigDto.toDomain(): WhatsAppConfig {
    return WhatsAppConfig(
        enabled = enabled,
        number = number.orEmpty(),
        appointmentMessageTemplate = appointmentMessage.orEmpty(),
        homeServiceMessageTemplate = homeServiceMessage.orEmpty()
    )
}

fun ContactInfoDto.toDomain(): ContactInfo {
    return ContactInfo(
        phone = phone.orEmpty(),
        whatsapp = whatsapp.orEmpty(),
        email = email.orEmpty(),
        website = website.orEmpty(),
        instagram = instagram.orEmpty(),
        facebook = facebook.orEmpty(),
        youtube = youtube.orEmpty(),
        googleMaps = googleMaps.orEmpty()
    )
}

fun ShareConfigDto.toDomain(): ShareConfig {
    return ShareConfig(
        enabled = enabled,
        message = message.orEmpty(),
        playStoreUrl = playStoreUrl.orEmpty()
    )
}

fun LegalConfigDto.toDomain(): LegalConfig {
    return LegalConfig(
        termsUrl = termsUrl.orEmpty(),
        privacyUrl = privacyUrl.orEmpty()
    )
}

fun HomeConfigDto.toDomain(): HomeConfig {
    return HomeConfig(
        salon = salon?.toDomain() ?: SalonInfo("", "", "", "", "", "", ""),
        sections = sections?.map { it.toDomain() } ?: emptyList()
    )
}

fun HomeSectionDto.toDomain(): HomeSection {
    val sectionType = when (type.lowercase()) {
        "hero" -> SectionType.HERO
        "quick_actions" -> SectionType.QUICK_ACTIONS
        "services" -> SectionType.SERVICES
        "offers" -> SectionType.OFFERS
        "gallery" -> SectionType.GALLERY
        "locations" -> SectionType.LOCATIONS
        "about" -> SectionType.ABOUT
        "contact" -> SectionType.CONTACT
        "banner" -> SectionType.BANNER
        "spacer" -> SectionType.SPACER
        else -> SectionType.UNKNOWN
    }

    return HomeSection(
        id = id,
        type = sectionType,
        title = title.orEmpty(),
        subtitle = subtitle.orEmpty(),
        imageUrl = imageUrl.orEmpty(),
        actionText = actionText.orEmpty(),
        actionRoute = actionRoute.orEmpty(),
        sourceUrl = sourceUrl.orEmpty(),
        itemsToShow = itemsToShow ?: 6,
        enabled = enabled,
        order = order
    )
}

fun ServiceCategoryDto.toDomain(): ServiceCategory {
    return ServiceCategory(
        id = id,
        name = name,
        icon = icon.orEmpty(),
        order = order,
        services = services?.map { it.toDomain() } ?: emptyList()
    )
}

fun ServiceDto.toDomain(): SalonService {
    return SalonService(
        id = id,
        name = name,
        description = description.orEmpty(),
        price = price,
        currency = currency,
        durationMinutes = durationMinutes,
        imageUrl = imageUrl.orEmpty(),
        featured = featured,
        bookingEnabled = bookingEnabled,
        homeServiceAvailable = homeServiceAvailable
    )
}

fun OfferDto.toDomain(): Offer {
    val type = when (discountType?.lowercase()) {
        "percentage" -> DiscountType.PERCENTAGE
        "fixed" -> DiscountType.FIXED
        else -> DiscountType.CUSTOM
    }

    return Offer(
        id = id,
        title = title,
        subtitle = subtitle.orEmpty(),
        description = description.orEmpty(),
        imageUrl = imageUrl.orEmpty(),
        discount = discount.orEmpty(),
        discountType = type,
        validFrom = validFrom.orEmpty(),
        validTill = validTill.orEmpty(),
        terms = terms.orEmpty(),
        ctaText = ctaText.orEmpty(),
        ctaAction = ctaAction.orEmpty(),
        enabled = enabled
    )
}

fun GalleryCategoryDto.toDomain(): GalleryCategory {
    return GalleryCategory(id = id, name = name)
}

fun GalleryImageDto.toDomain(): GalleryImage {
    return GalleryImage(
        id = id,
        categoryId = categoryId.orEmpty(),
        title = title.orEmpty(),
        description = description.orEmpty(),
        thumbnailUrl = thumbnailUrl.orEmpty(),
        imageUrl = imageUrl.orEmpty(),
        enabled = enabled,
        featured = featured,
        order = order
    )
}

fun BranchDto.toDomain(): Branch {
    return Branch(
        id = id,
        name = name,
        tagline = tagline.orEmpty(),
        address = address,
        phone = phone.orEmpty(),
        whatsapp = whatsapp.orEmpty(),
        latitude = latitude,
        longitude = longitude,
        googleMapsUrl = googleMapsUrl.orEmpty(),
        openingTime = openingTime.orEmpty(),
        closingTime = closingTime.orEmpty(),
        timings = timings.orEmpty(),
        enabled = enabled,
        isPrimary = isPrimary
    )
}

fun BookingConfigDto.toDomain(): BookingConfig {
    return BookingConfig(
        title = title.orEmpty(),
        subtitle = subtitle.orEmpty(),
        fields = fields?.map { it.toDomain() } ?: emptyList(),
        enabled = enabled
    )
}

fun BookingFieldDto.toDomain(): BookingField {
    val fieldType = when (type.lowercase()) {
        "phone" -> BookingFieldType.PHONE
        "email" -> BookingFieldType.EMAIL
        "date" -> BookingFieldType.DATE
        "time" -> BookingFieldType.TIME
        "service" -> BookingFieldType.SERVICE
        "branch" -> BookingFieldType.BRANCH
        "dropdown" -> BookingFieldType.DROPDOWN
        "multiline" -> BookingFieldType.MULTILINE
        "address" -> BookingFieldType.ADDRESS
        else -> BookingFieldType.TEXT
    }

    return BookingField(
        id = id,
        label = label,
        type = fieldType,
        placeholder = placeholder.orEmpty(),
        required = required,
        order = order,
        options = options ?: emptyList()
    )
}
