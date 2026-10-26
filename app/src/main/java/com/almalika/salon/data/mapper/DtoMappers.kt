package com.almalika.salon.data.mapper

import com.almalika.salon.data.remote.dto.*
import com.almalika.salon.domain.model.*



fun ProfileMenuItemDto.toDomain(): ProfileMenuItem {
    return ProfileMenuItem(
        id = id?.toString().orEmpty(),
        title = title.orEmpty(),
        type = type.orEmpty(),
        enabled = enabled ?: true,
        icon = icon.orEmpty()
    )
}

fun ProfileDto.toDomain(): ProfileConfig {
    return ProfileConfig(
        appName = appName.orEmpty(),
        appVersion = appVersion.orEmpty(),
        shareMessage = shareMessage.orEmpty(),
        supportEmail = supportEmail.orEmpty(),
        supportPhone = supportPhone.orEmpty(),
        privacyPolicyUrl = privacyPolicyUrl.orEmpty(),
        termsUrl = termsUrl.orEmpty(),
        aboutUs = aboutUs.orEmpty(),
        darkModeEnabled = darkModeEnabled ?: true,
        notificationEnabled = notificationEnabled ?: true,
        menuItems = menuItems?.map { it.toDomain() } ?: emptyList()
    )
}

fun FranchiseDto.toDomain(): FranchiseConfig {
    return FranchiseConfig(
        enabled = enabled ?: false,
        title = title.orEmpty(),
        subtitle = subtitle.orEmpty(),
        description = description.orEmpty(),
        contactPerson = contactPerson.orEmpty(),
        contactNumber = contactNumber.orEmpty(),
        email = email.orEmpty(),
        whatsApp = whatsApp.orEmpty(),
        website = website.orEmpty()
    )
}

fun StartupConfigDto.toDomain(): StartupConfig {
    return StartupConfig(
        splashScreenId = splashScreenId?.ifBlank { "splash" } ?: "splash",
        onboardingScreenId = onboardingScreenId?.ifBlank { "onboarding" } ?: "onboarding",
        loginScreenId = loginScreenId?.ifBlank { "login" } ?: "login",
        homeScreenId = homeScreenId?.ifBlank { "home" } ?: "home",
        requireLogin = requireLogin ?: false
    )
}

fun SplashScreenDto.toDomain(): SplashScreenConfig {
    return SplashScreenConfig(
        enabled = enabled ?: true,
        logoUrl = logoUrl.orEmpty(),
        title = title.orEmpty(),
        tagline = tagline.orEmpty(),
        backgroundColor = backgroundColor?.ifBlank { "#121212" } ?: "#121212",
        textColor = textColor?.ifBlank { "#FFFFFF" } ?: "#FFFFFF",
        accentColor = accentColor?.ifBlank { "#D4AF37" } ?: "#D4AF37",
        minimumDurationMs = minimumDurationMs ?: 1500L,
        animationType = animationType?.ifBlank { "fade" } ?: "fade"
    )
}

fun OnboardingPageDto.toDomain(): OnboardingPage {
    return OnboardingPage(
        id = id.orEmpty(),
        order = order ?: 0,
        title = title.orEmpty(),
        description = description.orEmpty(),
        illustrationUrl = illustrationUrl?.ifBlank { null } ?: imageUrl.orEmpty(),
        nextLabel = nextLabel?.ifBlank { "Next" } ?: "Next",
        skipLabel = skipLabel?.ifBlank { "Skip" } ?: "Skip",
        getStartedLabel = getStartedLabel?.ifBlank { "Get Started" } ?: "Get Started"
    )
}

fun OnboardingConfigDto.toDomain(): OnboardingConfig {
    val skipAllowed = allowSkip ?: showSkip ?: true
    val nextTxt = nextButtonText?.ifBlank { null }
    val skipTxt = skipButtonText?.ifBlank { null }
    val finishTxt = finishButtonText?.ifBlank { null }

    return OnboardingConfig(
        enabled = enabled ?: true,
        showSkip = skipAllowed,
        pages = pages?.map { p ->
            val domainPage = p.toDomain()
            domainPage.copy(
                nextLabel = if (p.nextLabel.isNullOrBlank() && nextTxt != null) nextTxt else domainPage.nextLabel,
                skipLabel = if (p.skipLabel.isNullOrBlank() && skipTxt != null) skipTxt else domainPage.skipLabel,
                getStartedLabel = if (p.getStartedLabel.isNullOrBlank() && finishTxt != null) finishTxt else domainPage.getStartedLabel
            )
        }?.sortedBy { it.order } ?: emptyList()
    )
}

fun LoginConfigDto.toDomain(): LoginConfig {
    return LoginConfig(
        enabled = enabled ?: false,
        allowGuest = allowGuest ?: true,
        title = title?.ifBlank { "Welcome" } ?: "Welcome",
        subtitle = subtitle?.ifBlank { "Sign in to your account" } ?: "Sign in to your account",
        logoUrl = logoUrl.orEmpty(),
        phonePlaceholder = phonePlaceholder?.ifBlank { "Enter mobile number" } ?: "Enter mobile number",
        continueAsGuestLabel = continueAsGuestLabel?.ifBlank { "Continue as Guest" } ?: "Continue as Guest",
        loginButtonLabel = loginButtonLabel?.ifBlank { "Sign In" } ?: "Sign In"
    )
}

fun NavDestinationDto.toDomain(): NavItem {
    val rawRoute = route?.ifBlank { null } ?: destination?.ifBlank { null } ?: id.orEmpty()
    val normalizedRoute = when (rawRoute.lowercase().trim()) {
        "price_list", "prices" -> "services"
        "location" -> "locations"
        "franchise" -> "about"
        else -> rawRoute
    }
    return NavItem(
        id = id.orEmpty(),
        title = title.orEmpty(),
        icon = icon.orEmpty(),
        route = normalizedRoute,
        enabled = enabled ?: true,
        order = order ?: 0
    )
}

fun NavigationConfigDto.toDomain(): NavigationConfig {
    return NavigationConfig(
        drawerEnabled = drawerEnabled ?: true,
        bottomNavigationEnabled = bottomNavigationEnabled ?: true,
        drawerItems = drawerItems?.map { it.toDomain() }?.sortedBy { it.order } ?: emptyList(),
        bottomItems = bottomItems?.map { it.toDomain() }?.sortedBy { it.order } ?: emptyList()
    )
}

fun ThemeConfigDto.toDomain(): ThemeConfig {
    val prim = colors?.primary?.ifBlank { null } ?: primaryColor?.ifBlank { null } ?: "#D4AF37"
    val sec = colors?.secondary?.ifBlank { null } ?: secondaryColor?.ifBlank { null } ?: "#A67C1E"
    val bg = colors?.background?.ifBlank { null } ?: backgroundColor?.ifBlank { null } ?: "#18181B"
    val surf = colors?.surface?.ifBlank { null } ?: surfaceColor?.ifBlank { null } ?: "#27272A"
    val txt = colors?.textPrimary?.ifBlank { null } ?: textColor?.ifBlank { null } ?: "#FFFFFF"
    return ThemeConfig(
        themeMode = themeMode?.ifBlank { "dark" } ?: "dark",
        primaryColor = prim,
        secondaryColor = sec,
        backgroundColor = bg,
        surfaceColor = surf,
        textColor = txt
    )
}

// -------------------------------------------------------------
// Unified MenuResponseDto mappings for remote menu.json
// -------------------------------------------------------------

fun MenuResponseDto.toDomain(): SalonMasterConfig {
    val salonDetail = salon
    val prof = profile
    val franch = franchise
    val navConfig = navigation?.toDomain() ?: NavigationConfig()

    val salonInfo = SalonInfo(
        name = salonDetail?.name?.trim().orEmpty().ifBlank { "Al-Malaika Salon" },
        tagline = salonDetail?.tagline?.trim().orEmpty(),
        logoUrl = banners?.firstOrNull()?.imageUrl.orEmpty(),
        phone = salonDetail?.mobileNumber?.trim().orEmpty().ifBlank { salonDetail?.mobileNumberDial?.trim().orEmpty() },
        whatsapp = salonDetail?.mobileNumber?.trim().orEmpty().ifBlank { salonDetail?.mobileNumberDial?.trim().orEmpty() },
        address = salonDetail?.address?.trim().orEmpty(),
        aboutText = salonDetail?.about?.trim().orEmpty()
    )

    val bottomNav = if (navConfig.bottomItems.isNotEmpty()) {
        navConfig.bottomItems
    } else {
        listOf(
            NavItem("home", "Home", "home", "home", true, 1),
            NavItem("services", "Services", "content_cut", "services", true, 2),
            NavItem("offers", "Offers", "local_offer", "offers", !offers.isNullOrEmpty(), 3),
            NavItem("booking", "Booking", "calendar_month", "booking", true, 4),
            NavItem("locations", "Locations", "place", "locations", true, 5)
        )
    }

    // Build drawer dynamically from navigation.drawerItems or fallback to profile.menuItems
    val drawerItems = if (navConfig.drawerItems.isNotEmpty()) {
        navConfig.drawerItems
    } else {
        val items = mutableListOf<NavItem>()
        var drawerOrder = 1
        items.add(NavItem("home", "Home", "home", "home", true, drawerOrder++))
        items.add(NavItem("services", "Services", "content_cut", "services", true, drawerOrder++))
        if (!offers.isNullOrEmpty()) {
            items.add(NavItem("offers", "Offers", "local_offer", "offers", true, drawerOrder++))
        }
        items.add(NavItem("booking", "Book Appointment", "calendar_month", "booking", true, drawerOrder++))
        if (homeServiceBooking?.enabled == true) {
            items.add(NavItem("home_service", "Home Service", "home_repair_service", "home_service", true, drawerOrder++))
        }
        if (!galleryItems.isNullOrEmpty()) {
            items.add(NavItem("gallery", "Gallery", "photo_library", "gallery", true, drawerOrder++))
        }
        items.add(NavItem("locations", "Locations", "place", "locations", true, drawerOrder++))
        items.add(NavItem("about", "About Us", "info", "about", true, drawerOrder++))
        items.add(NavItem("contact", "Contact Us", "call", "contact", true, drawerOrder++))
        items.add(NavItem("settings", "Settings", "settings", "settings", true, drawerOrder++))
        items.add(NavItem("share", "Share App", "share", "action:share", true, drawerOrder++))
        items.add(NavItem("rate", "Rate Us", "star", "action:rate", true, drawerOrder++))
        items.add(NavItem("terms", "Terms & Conditions", "description", "terms", true, drawerOrder++))
        items.add(NavItem("privacy", "Privacy Policy", "privacy_tip", "privacy", true, drawerOrder))
        items
    }

    val flags = featureFlags ?: mapOf(
        "booking" to true,
        "homeService" to (homeServiceBooking?.enabled ?: false),
        "offers" to (!offers.isNullOrEmpty()),
        "gallery" to (!galleryItems.isNullOrEmpty()),
        "multipleBranches" to false,
        "whatsappBooking" to true,
        "darkMode" to (prof?.darkModeEnabled ?: true),
        "notifications" to (prof?.notificationEnabled ?: true),
        "franchise" to (franch?.enabled ?: false)
    )

    val phoneNum = salonDetail?.mobileNumber?.trim().orEmpty().ifBlank { salonDetail?.mobileNumberDial?.trim().orEmpty() }

    val whatsAppConfig = WhatsAppConfig(
        enabled = phoneNum.isNotBlank() || !salonDetail?.whatsappUrl.isNullOrBlank(),
        number = phoneNum,
        appointmentMessageTemplate = "Hello {salonName}, I would like to book an appointment.\nName: {name}\nPhone: {phone}\nService: {service}\nDate: {date}\nTime: {time}\nBranch: {branch}\nMessage: {message}",
        homeServiceMessageTemplate = "Hello {salonName}, I would like to book a Home Service.\nName: {name}\nPhone: {phone}\nAddress: {address}\nService: {service}\nDate: {date}\nTime: {time}\nMessage: {message}"
    )

    val contactInfo = ContactInfo(
        phone = phoneNum,
        whatsapp = salonDetail?.whatsappUrl.orEmpty().ifBlank { phoneNum },
        email = salonDetail?.email.orEmpty().ifBlank { prof?.supportEmail.orEmpty() },
        website = salonDetail?.mapsSearchUrl.orEmpty(),
        instagram = salonDetail?.social?.instagram.orEmpty(),
        facebook = salonDetail?.social?.facebook.orEmpty(),
        youtube = "",
        googleMaps = salonDetail?.mapsSearchUrl.orEmpty().ifBlank { salonDetail?.mapsEmbedUrl.orEmpty() }
    )

    val shareConfig = ShareConfig(
        enabled = true,
        message = prof?.shareMessage.orEmpty().ifBlank { "Discover Al-Malaika Salon – For Men. Explore our grooming services and contact us for details." },
        playStoreUrl = salonDetail?.playStoreUrl.orEmpty().ifBlank { "https://play.google.com/store/apps/details?id=com.almalika.salon" }
    )

    val legalConfig = LegalConfig(
        termsUrl = prof?.termsUrl.orEmpty(),
        privacyUrl = prof?.privacyPolicyUrl.orEmpty(),
        aboutUsUrl = prof?.aboutUs.orEmpty()
    )

    return SalonMasterConfig(
        schemaVersion = metadata?.schemaVersion?.toIntOrNull() ?: 2,
        configVersion = 1,
        salon = salonInfo,
        bottomNavigation = bottomNav,
        drawer = drawerItems,
        featureFlags = flags,
        whatsapp = whatsAppConfig,
        contact = contactInfo,
        share = shareConfig,
        legal = legalConfig,
        profile = prof?.toDomain(),
        franchise = franch?.toDomain(),
        startup = startup?.toDomain() ?: StartupConfig(),
        splashScreen = splashScreen?.toDomain() ?: SplashScreenConfig(),
        onboarding = onboarding?.toDomain() ?: OnboardingConfig(),
        login = login?.toDomain() ?: LoginConfig(),
        navigation = navConfig,
        theme = theme?.toDomain() ?: ThemeConfig()
    )
}

fun MenuResponseDto.toHomeConfig(): HomeConfig {
    val salonInfo = SalonInfo(
        name = salon?.name?.trim().orEmpty().ifBlank { "Al-Malaika Salon" },
        tagline = salon?.tagline?.trim().orEmpty(),
        logoUrl = banners?.firstOrNull()?.imageUrl.orEmpty(),
        phone = salon?.mobileNumber?.trim().orEmpty().ifBlank { salon?.mobileNumberDial?.trim().orEmpty() },
        whatsapp = salon?.mobileNumber?.trim().orEmpty().ifBlank { salon?.mobileNumberDial?.trim().orEmpty() },
        address = salon?.address?.trim().orEmpty(),
        aboutText = salon?.about?.trim().orEmpty()
    )

    val configuredHomeSections = home?.sections
    if (!configuredHomeSections.isNullOrEmpty()) {
        val mappedSections = configuredHomeSections
            .filter { it.enabled ?: true }
            .sortedBy { it.order ?: 0 }
            .map { s ->
                val sectionType = when (s.type?.lowercase()?.trim()) {
                    "hero", "banner_carousel" -> SectionType.HERO
                    "quick_actions", "service_grid" -> SectionType.QUICK_ACTIONS
                    "services", "service_list_horizontal" -> SectionType.SERVICES
                    "offers", "offer_list" -> SectionType.OFFERS
                    "gallery", "gallery_preview" -> SectionType.GALLERY
                    "locations", "salon_location_card" -> SectionType.LOCATIONS
                    "about", "feature_grid", "why_choose_us" -> SectionType.ABOUT
                    "contact" -> SectionType.CONTACT
                    "banner", "franchise_banner" -> SectionType.BANNER
                    "reviews", "review_list" -> SectionType.REVIEWS
                    "spacer" -> SectionType.SPACER
                    else -> SectionType.UNKNOWN
                }

                val rawRoute = s.actionRoute?.ifBlank { null }
                    ?: s.viewAllDestination?.ifBlank { null }
                    ?: s.action?.destination?.ifBlank { null }
                    ?: s.itemAction?.destination?.ifBlank { null }
                    ?: ""

                val normalizedRoute = when (rawRoute.lowercase().trim()) {
                    "price_list", "prices" -> "services"
                    "location" -> "locations"
                    "franchise" -> "about"
                    else -> rawRoute
                }

                HomeSection(
                    id = s.id ?: "sec_${sectionType.name.lowercase()}",
                    type = sectionType,
                    title = s.title.orEmpty(),
                    subtitle = s.subtitle.orEmpty(),
                    imageUrl = s.imageUrl.orEmpty(),
                    actionText = s.actionText.orEmpty(),
                    actionRoute = normalizedRoute,
                    sourceUrl = s.sourceUrl.orEmpty(),
                    itemsToShow = s.itemsToShow ?: s.maxItems ?: 6,
                    enabled = s.enabled ?: true,
                    order = s.order ?: 0
                )
            }
        return HomeConfig(salon = salonInfo, sections = mappedSections)
    }

    val sections = mutableListOf<HomeSection>()
    var order = 1

    // 1. Hero Banner
    if (!banners.isNullOrEmpty()) {
        val hero = banners.first()
        sections.add(
            HomeSection(
                id = "sec_hero",
                type = SectionType.HERO,
                title = hero.title.orEmpty(),
                subtitle = hero.subtitle.orEmpty(),
                imageUrl = hero.imageUrl.orEmpty(),
                actionText = "Book Appointment",
                actionRoute = "booking",
                sourceUrl = "",
                itemsToShow = 1,
                enabled = true,
                order = order++
            )
        )
    }

    // 2. Quick Actions
    sections.add(
        HomeSection(
            id = "sec_quick_actions",
            type = SectionType.QUICK_ACTIONS,
            title = "Quick Actions",
            subtitle = "",
            imageUrl = "",
            actionText = "",
            actionRoute = "",
            sourceUrl = "",
            itemsToShow = 5,
            enabled = true,
            order = order++
        )
    )

    // 3. Popular Services
    if (!popularServices.isNullOrEmpty() || !quickServices.isNullOrEmpty()) {
        sections.add(
            HomeSection(
                id = "sec_services",
                type = SectionType.SERVICES,
                title = "Popular Services",
                subtitle = "Precision grooming and styling",
                imageUrl = "",
                actionText = "View All Services",
                actionRoute = "services",
                sourceUrl = "",
                itemsToShow = 6,
                enabled = true,
                order = order++
            )
        )
    }

    // 4. Promo Banner
    if (banners != null && banners.size > 1) {
        val banner = banners[1]
        sections.add(
            HomeSection(
                id = "sec_banner",
                type = SectionType.BANNER,
                title = banner.title.orEmpty(),
                subtitle = banner.subtitle.orEmpty(),
                imageUrl = banner.imageUrl.orEmpty(),
                actionText = "",
                actionRoute = "",
                sourceUrl = "",
                itemsToShow = 1,
                enabled = true,
                order = order++
            )
        )
    }

    // 5. Offers (only if present)
    if (!offers.isNullOrEmpty()) {
        sections.add(
            HomeSection(
                id = "sec_offers",
                type = SectionType.OFFERS,
                title = "Exclusive Offers",
                subtitle = "Special packages & styling vouchers",
                imageUrl = "",
                actionText = "All Offers",
                actionRoute = "offers",
                sourceUrl = "",
                itemsToShow = 4,
                enabled = true,
                order = order++
            )
        )
    }

    // 6. Gallery
    if (!galleryItems.isNullOrEmpty()) {
        sections.add(
            HomeSection(
                id = "sec_gallery",
                type = SectionType.GALLERY,
                title = "Salon Gallery",
                subtitle = "Client styling highlights",
                imageUrl = "",
                actionText = "View Gallery",
                actionRoute = "gallery",
                sourceUrl = "",
                itemsToShow = 6,
                enabled = true,
                order = order++
            )
        )
    }

    // 7. Locations
    sections.add(
        HomeSection(
            id = "sec_locations",
            type = SectionType.LOCATIONS,
            title = "Visit Our Salon",
            subtitle = "Convenient grooming studio in Hazaribagh",
            imageUrl = "",
            actionText = "Get Directions",
            actionRoute = "locations",
            sourceUrl = "",
            itemsToShow = 1,
            enabled = true,
            order = order++
        )
    )

    // 8. About
    sections.add(
        HomeSection(
            id = "sec_about",
            type = SectionType.ABOUT,
            title = "About Us",
            subtitle = "Al-Malaika Salon Story",
            imageUrl = "",
            actionText = "",
            actionRoute = "about",
            sourceUrl = "",
            itemsToShow = 1,
            enabled = true,
            order = order++
        )
    )

    // 9. Contact
    sections.add(
        HomeSection(
            id = "sec_contact",
            type = SectionType.CONTACT,
            title = "Contact Us",
            subtitle = "Enquire or book by phone",
            imageUrl = "",
            actionText = "Contact Now",
            actionRoute = "contact",
            sourceUrl = "",
            itemsToShow = 1,
            enabled = true,
            order = order++
        )
    )

    return HomeConfig(
        salon = salonInfo,
        sections = sections
    )
}

fun MenuResponseDto.toServiceCategories(): List<ServiceCategory> {
    val allServices = mutableListOf<SalonService>()

    popularServices?.forEach { s ->
        allServices.add(
            SalonService(
                id = s.id?.toString() ?: s.name.orEmpty().lowercase().replace(" ", "_"),
                name = s.name.orEmpty(),
                description = s.description.orEmpty(),
                price = s.price ?: 0.0,
                currency = "INR",
                durationMinutes = 30,
                imageUrl = s.imageUrl.orEmpty(),
                featured = true,
                bookingEnabled = s.isAvailable ?: true,
                homeServiceAvailable = s.serviceType.equals("HOME", ignoreCase = true)
            )
        )
    }

    quickServices?.forEach { s ->
        if (allServices.none { it.name.equals(s.name, ignoreCase = true) }) {
            allServices.add(
                SalonService(
                    id = s.id?.toString() ?: s.name.orEmpty().lowercase().replace(" ", "_"),
                    name = s.name.orEmpty(),
                    description = s.description.orEmpty(),
                    price = s.price ?: 0.0,
                    currency = "INR",
                    durationMinutes = 30,
                    imageUrl = s.imageUrl.orEmpty(),
                    featured = false,
                    bookingEnabled = s.isAvailable ?: true,
                    homeServiceAvailable = s.serviceType.equals("HOME", ignoreCase = true)
                )
            )
        }
    }

    priceList?.forEach { p ->
        if (allServices.none { it.name.equals(p.name, ignoreCase = true) }) {
            allServices.add(
                SalonService(
                    id = p.id?.toString() ?: p.name.orEmpty().lowercase().replace(" ", "_"),
                    name = p.name.orEmpty(),
                    description = p.description.orEmpty(),
                    price = p.price ?: 0.0,
                    currency = "INR",
                    durationMinutes = 30,
                    imageUrl = "",
                    featured = false,
                    bookingEnabled = true,
                    homeServiceAvailable = false
                )
            )
        }
    }

    val categoryDefs = listOf(
        "HAIR" to "Hair Styling & Cut",
        "BEARD" to "Beard Grooming",
        "SHAVING" to "Shaving",
        "STYLING" to "Styling",
        "PACKAGE" to "Packages & Combos",
        "COLOUR" to "Hair Colour",
        "MASSAGE" to "Head & Scalp Massage",
        "KIDS" to "Kids Haircut"
    )

    val result = mutableListOf<ServiceCategory>()
    var order = 1

    categoryDefs.forEach { (catKey, catDisplayName) ->
        val matching = allServices.filter { service ->
            popularServices?.any { it.name.equals(service.name, ignoreCase = true) && it.category.equals(catKey, ignoreCase = true) } == true ||
            quickServices?.any { it.name.equals(service.name, ignoreCase = true) && it.category.equals(catKey, ignoreCase = true) } == true ||
            priceList?.any { it.name.equals(service.name, ignoreCase = true) && it.category.equals(catKey, ignoreCase = true) } == true
        }

        if (matching.isNotEmpty()) {
            result.add(
                ServiceCategory(
                    id = catKey.lowercase(),
                    name = catDisplayName,
                    icon = when (catKey) {
                        "HAIR" -> "content_cut"
                        "BEARD" -> "face"
                        "SHAVING" -> "brush"
                        "PACKAGE" -> "inventory_2"
                        "COLOUR" -> "palette"
                        "MASSAGE" -> "spa"
                        "KIDS" -> "child_care"
                        else -> "style"
                    },
                    order = order++,
                    services = matching
                )
            )
        }
    }

    val coveredNames = result.flatMap { it.services }.map { it.name.lowercase() }.toSet()
    val unassigned = allServices.filter { it.name.lowercase() !in coveredNames }
    if (unassigned.isNotEmpty()) {
        result.add(
            ServiceCategory(
                id = "general",
                name = "General Grooming",
                icon = "content_cut",
                order = order++,
                services = unassigned
            )
        )
    }

    return result
}

fun MenuResponseDto.toOffers(): List<Offer> {
    return offers?.map {
        Offer(
            id = it.id?.toString().orEmpty(),
            title = it.title.orEmpty(),
            subtitle = it.subtitle.orEmpty(),
            description = it.description.orEmpty(),
            imageUrl = it.imageUrl.orEmpty(),
            discount = it.discount.orEmpty(),
            discountType = when (it.discountType?.lowercase()) {
                "percentage" -> DiscountType.PERCENTAGE
                "fixed" -> DiscountType.FIXED
                else -> DiscountType.CUSTOM
            },
            validFrom = it.validFrom.orEmpty(),
            validTill = it.validTill.orEmpty(),
            terms = it.terms.orEmpty(),
            ctaText = it.ctaText.orEmpty().ifBlank { "Book Offer" },
            ctaAction = it.ctaAction.orEmpty().ifBlank { "booking" },
            enabled = it.enabled ?: true
        )
    } ?: emptyList()
}

fun ReviewDto.toDomain(): Review {
    return Review(
        id = id?.toString().orEmpty(),
        author = author.orEmpty().ifBlank { "Verified Client" },
        rating = rating ?: 5.0,
        comment = comment.orEmpty(),
        date = date.orEmpty(),
        avatarUrl = avatarUrl.orEmpty()
    )
}

fun MenuResponseDto.toReviews(): List<Review> {
    return reviews?.map { it.toDomain() } ?: emptyList()
}

fun MenuResponseDto.toGalleryImages(): List<GalleryImage> {
    return galleryItems?.mapIndexed { index, item ->
        GalleryImage(
            id = item.id?.toString() ?: "gallery_$index",
            categoryId = "all",
            title = item.title.orEmpty(),
            description = "",
            thumbnailUrl = item.imageUrl.orEmpty(),
            imageUrl = item.imageUrl.orEmpty(),
            enabled = true,
            featured = index < 2,
            order = index
        )
    } ?: emptyList()
}

fun MenuResponseDto.toBranches(): List<Branch> {
    val s = salon ?: return emptyList()
    val phoneNum = s.mobileNumber?.trim().orEmpty().ifBlank { s.mobileNumberDial?.trim().orEmpty() }
    val hoursParts = s.openingHours?.split("–", "-")?.map { it.trim() }
    val openTime = hoursParts?.getOrNull(0)?.ifBlank { "08:00 AM" } ?: "08:00 AM"
    val closeTime = hoursParts?.getOrNull(1)?.ifBlank { "09:30 PM" } ?: "09:30 PM"

    return listOf(
        Branch(
            id = s.id?.toString() ?: "branch_main",
            name = s.name.orEmpty().ifBlank { "Al-Malaika Salon" },
            tagline = s.tagline.orEmpty(),
            address = s.address.orEmpty().ifBlank { "Alfalah Colony, Pugmil, Hazaribagh, Jharkhand 825319" },
            phone = phoneNum,
            whatsapp = phoneNum,
            latitude = s.latitude ?: 23.9988,
            longitude = s.longitude ?: 85.3612,
            googleMapsUrl = s.mapsSearchUrl.orEmpty().ifBlank { s.mapsEmbedUrl.orEmpty() },
            openingTime = openTime,
            closingTime = closeTime,
            timings = "${s.openingHours.orEmpty()} (${s.workingDays.orEmpty()})".trim(),
            enabled = true,
            isPrimary = true
        )
    )
}

fun MenuResponseDto.toBookingConfig(): BookingConfig {
    return BookingConfig(
        title = "Book Salon Appointment",
        subtitle = "Select your desired styling service and preferred schedule",
        fields = listOf(
            BookingField("name", "Your Full Name", BookingFieldType.TEXT, "Enter your name", true, 1, emptyList()),
            BookingField("phone", "Contact Number", BookingFieldType.PHONE, "Enter 10-digit mobile number", true, 2, emptyList()),
            BookingField("service", "Select Service", BookingFieldType.SERVICE, "Choose service", true, 3, emptyList()),
            BookingField("branch", "Select Branch", BookingFieldType.BRANCH, "Choose branch", true, 4, emptyList()),
            BookingField("date", "Appointment Date", BookingFieldType.DATE, "Select date", true, 5, emptyList()),
            BookingField("time", "Appointment Time", BookingFieldType.TIME, "Select time", true, 6, emptyList()),
            BookingField("message", "Notes / Special Requests", BookingFieldType.MULTILINE, "Optional notes for the stylist", false, 7, emptyList())
        ),
        enabled = true,
        bookingMessage = "Contact Al-Malaika Salon to enquire about available services."
    )
}

fun MenuResponseDto.toHomeServiceConfig(): BookingConfig {
    val isEnabled = homeServiceBooking?.enabled ?: false
    val message = homeServiceBooking?.bookingMessage.orEmpty().ifBlank {
        "Contact Al-Malaika Salon to enquire about available services."
    }

    return BookingConfig(
        title = "Home Salon Service",
        subtitle = "Professional salon care delivered at your home",
        fields = listOf(
            BookingField("name", "Your Full Name", BookingFieldType.TEXT, "Enter your name", true, 1, emptyList()),
            BookingField("phone", "Contact Number", BookingFieldType.PHONE, "Enter 10-digit mobile number", true, 2, emptyList()),
            BookingField("address", "Home Address", BookingFieldType.ADDRESS, "Enter your residential address", true, 3, emptyList()),
            BookingField("service", "Select Service", BookingFieldType.SERVICE, "Choose service", true, 4, emptyList()),
            BookingField("date", "Preferred Date", BookingFieldType.DATE, "Select date", true, 5, emptyList()),
            BookingField("time", "Preferred Time", BookingFieldType.TIME, "Select time", true, 6, emptyList()),
            BookingField("message", "Special Instructions", BookingFieldType.MULTILINE, "Any landmarks or instructions", false, 7, emptyList())
        ),
        enabled = isEnabled,
        bookingMessage = message
    )
}
