package com.almalika.salon.data

import com.almalika.salon.data.mapper.*
import com.almalika.salon.data.remote.dto.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.almalika.salon.presentation.navigation.IconResolver
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MenuConfigIntegrationTest {

    private lateinit var moshi: Moshi

    @Before
    fun setUp() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun `test menu json deserialization with full data structure`() {
        val sampleJson = """
        {
          "metadata": {
            "title": "Al-Malaika Life Style Salon & Academy",
            "description": "Exclusive salon menu",
            "version": "1.0.0",
            "lastUpdated": "2026-03-23"
          },
          "salon": {
            "name": "Al-Malaika Life Style Salon & Academy",
            "tagline": "A Complete Family Salon & Academy",
            "about": "Premium styling and aesthetic grooming in Hazaribagh.",
            "logo": "https://er-md-hafiz.github.io/api.almalaikasalon/logo.png",
            "address": "Alfalah Colony, Pugmil, Hazaribagh, Jharkhand 825319",
            "mobileNumber": "9708101242",
            "mobileNumberDial": "+919708101242",
            "whatsappUrl": "https://wa.me/919708101242",
            "instagramUrl": "https://instagram.com/almalaikasalon",
            "facebookUrl": "https://facebook.com/almalaikasalon",
            "youtubeUrl": "https://youtube.com/almalaikasalon",
            "mapsSearchUrl": "https://maps.google.com/?cid=12345"
          },
          "banners": [
            {
              "id": 1,
              "title": "Festive Glamour",
              "subtitle": "Book ahead",
              "imageUrl": "https://example.com/b1.jpg",
              "actionUrl": "booking"
            }
          ],
          "quickServices": [
            {
              "id": 1,
              "name": "Royal Shave",
              "price": 150.0,
              "category": "Shaving",
              "gender": "Men"
            }
          ],
          "popularServices": [],
          "homeServices": [],
          "homeServiceBooking": {
            "enabled": false,
            "title": "Home Service",
            "bookingMessage": "Home service is currently paused."
          },
          "priceList": [
            {
              "id": 101,
              "name": "Hydra Facial",
              "price": 1200.0,
              "category": "Skin Care",
              "gender": "Unisex"
            }
          ],
          "offers": [],
          "reviews": [],
          "barbers": [],
          "galleryItems": [
            {
              "id": 1,
              "title": "Bridal Updo",
              "imageUrl": "https://example.com/g1.jpg",
              "category": "Hair"
            }
          ],
          "whyChooseUs": [],
          "timeSlots": ["10:00 AM", "11:00 AM"],
          "profile": {
            "appName": "Al-Malaika Salon",
            "appVersion": "1.0.0",
            "shareMessage": "Experience exquisite styling at Al-Malaika Salon!",
            "supportEmail": "support@almalaikasalon.com",
            "supportPhone": "+919708101242",
            "privacyPolicyUrl": "https://er-md-hafiz.github.io/api.almalaikasalon/privacy.html",
            "termsUrl": "https://er-md-hafiz.github.io/api.almalaikasalon/terms.html",
            "aboutUs": "https://er-md-hafiz.github.io/api.almalaikasalon/aboutus.html",
            "darkModeEnabled": true,
            "notificationEnabled": true,
            "menuItems": [
              {
                "id": "menu_about",
                "title": "About Us",
                "route": "about",
                "icon": "info",
                "enabled": true,
                "order": 1
              }
            ]
          },
          "franchise": {
            "enabled": false,
            "title": "Franchise Opportunity",
            "description": "Coming soon"
          }
        }
        """.trimIndent()

        val adapter = moshi.adapter(MenuResponseDto::class.java)
        val dto = adapter.fromJson(sampleJson)

        assertNotNull(dto)
        assertEquals("Al-Malaika Life Style Salon & Academy", dto?.salon?.name)
        assertEquals(false, dto?.homeServiceBooking?.enabled)
        assertEquals(false, dto?.franchise?.enabled)
        assertEquals("https://er-md-hafiz.github.io/api.almalaikasalon/privacy.html", dto?.profile?.privacyPolicyUrl)
        assertEquals("https://er-md-hafiz.github.io/api.almalaikasalon/terms.html", dto?.profile?.termsUrl)
        assertEquals("https://er-md-hafiz.github.io/api.almalaikasalon/aboutus.html", dto?.profile?.aboutUs)

        // Test Domain Mapping
        val master = dto!!.toDomain()
        assertEquals("Al-Malaika Life Style Salon & Academy", master.salon.name)
        assertEquals("9708101242", master.salon.phone)
        assertEquals("9708101242", master.salon.whatsapp)
        assertEquals("https://er-md-hafiz.github.io/api.almalaikasalon/terms.html", master.legal.termsUrl)
        assertEquals("https://er-md-hafiz.github.io/api.almalaikasalon/privacy.html", master.legal.privacyUrl)
        assertEquals("https://er-md-hafiz.github.io/api.almalaikasalon/aboutus.html", master.legal.aboutUsUrl)
        assertEquals(false, master.featureFlags["homeService"])
        assertEquals(false, master.featureFlags["franchise"])
        assertEquals("Experience exquisite styling at Al-Malaika Salon!", master.share.message)

        // Test Home Config Mapping
        val homeConfig = dto.toHomeConfig()
        assertTrue(homeConfig.sections.isNotEmpty())
        assertEquals("Festive Glamour", homeConfig.sections.first().title)

        // Test Booking Config Mapping
        val homeBookingConfig = dto.toHomeServiceConfig()
        assertEquals(false, homeBookingConfig.enabled)
        assertEquals("Home service is currently paused.", homeBookingConfig.bookingMessage)

        val salonBookingConfig = toBookingConfig()
        assertEquals(true, salonBookingConfig.enabled)
        assertTrue(salonBookingConfig.fields.isNotEmpty())

        // Test Services Mapping
        val services = dto.toServiceCategories()
        assertTrue(services.isNotEmpty())
        val flatServices = services.flatMap { it.services }
        assertTrue(flatServices.any { it.name == "Royal Shave" && it.price == 150.0 })
        assertTrue(flatServices.any { it.name == "Hydra Facial" && it.price == 1200.0 })

        // Test Branches Mapping
        val branches = dto.toBranches()
        assertTrue(branches.isNotEmpty())
        assertEquals("Al-Malaika Life Style Salon & Academy", branches.first().name)
    }

    @Test
    fun `test menu json deserialization with empty or missing fields handles safely without crashing`() {
        val minimalJson = "{}"
        val adapter = moshi.adapter(MenuResponseDto::class.java)
        val dto = adapter.fromJson(minimalJson)

        assertNotNull(dto)
        val master = dto!!.toDomain()
        assertEquals("Al-Malaika Salon", master.salon.name)
        assertEquals("", master.legal.termsUrl)
        assertEquals("", master.legal.privacyUrl)
        assertEquals("", master.legal.aboutUsUrl)
        assertEquals(false, master.featureFlags["homeService"])
        assertEquals(false, master.featureFlags["franchise"])

        val branches = dto.toBranches()
        assertTrue(branches.isEmpty())

        val homeBooking = dto.toHomeServiceConfig()
        assertEquals(false, homeBooking.enabled)
    }

    @Test
    fun `test dynamic screen configuration mapping for splash, onboarding, login, navigation and home`() {
        val json = """
        {
          "startup": {
            "splashScreenId": "splash",
            "onboardingScreenId": "onboarding",
            "loginScreenId": "login",
            "homeScreenId": "home",
            "requireLogin": false
          },
          "splashScreen": {
            "enabled": true,
            "title": "Al-Malaika Salon",
            "tagline": "Royal Grooming",
            "minimumDurationMs": 2000
          },
          "onboarding": {
            "enabled": true,
            "showSkip": true,
            "pages": [
              {
                "id": "p1",
                "order": 1,
                "title": "Style",
                "description": "Best haircuts",
                "nextLabel": "Next Page"
              }
            ]
          },
          "login": {
            "enabled": true,
            "allowGuest": true,
            "title": "Welcome Back"
          },
          "navigation": {
            "drawerEnabled": true,
            "bottomNavigationEnabled": true,
            "drawerItems": [
              { "id": "home", "title": "Dashboard", "icon": "home", "route": "home", "enabled": true, "order": 1 }
            ],
            "bottomItems": [
              { "id": "home", "title": "Dashboard", "icon": "home", "route": "home", "enabled": true, "order": 1 }
            ]
          },
          "home": {
            "enabled": true,
            "screenTitle": "Custom Home",
            "sections": [
              { "id": "s1", "type": "hero", "title": "Hero Section", "enabled": true, "order": 1 }
            ]
          }
        }
        """

        val adapter = moshi.adapter(MenuResponseDto::class.java)
        val dto = adapter.fromJson(json)
        assertNotNull(dto)

        val master = dto!!.toDomain()
        assertEquals(true, master.splashScreen.enabled)
        assertEquals("Al-Malaika Salon", master.splashScreen.title)
        assertEquals(2000L, master.splashScreen.minimumDurationMs)

        assertEquals(true, master.onboarding.enabled)
        assertEquals(1, master.onboarding.pages.size)
        assertEquals("Next Page", master.onboarding.pages[0].nextLabel)

        assertEquals(true, master.login.enabled)
        assertEquals(true, master.login.allowGuest)
        assertEquals("Welcome Back", master.login.title)

        assertEquals(1, master.bottomNavigation.size)
        assertEquals("Dashboard", master.bottomNavigation[0].title)

        assertEquals(1, master.drawer.size)
        assertEquals("Dashboard", master.drawer[0].title)

        val homeConfig = dto.toHomeConfig()
        assertEquals(1, homeConfig.sections.size)
        assertEquals("Hero Section", homeConfig.sections[0].title)
    }

    @Test
    fun `test bundled menu_fallback json file is well formed and parses cleanly`() {
        val filePaths = listOf(
            java.io.File("src/main/assets/config/menu_fallback.json"),
            java.io.File("app/src/main/assets/config/menu_fallback.json")
        )
        val fallbackFile = filePaths.firstOrNull { it.exists() }
        assertNotNull("menu_fallback.json must exist in assets", fallbackFile)

        val json = fallbackFile!!.readText()
        val adapter = moshi.adapter(MenuResponseDto::class.java)
        val dto = adapter.fromJson(json)
        assertNotNull(dto)

        val master = dto!!.toDomain()
        assertEquals("Al-Malaika Salon", master.salon.name)
        assertEquals("+919708101242", master.salon.phone)
        assertEquals("contact@almalaikasalon.in", master.contact.email)
        assertEquals(true, master.splashScreen.enabled)
        assertTrue(master.onboarding.pages.isNotEmpty())
        assertTrue(master.drawer.isNotEmpty())
        assertTrue(master.bottomNavigation.isNotEmpty())

        val homeConfig = dto.toHomeConfig()
        assertTrue(homeConfig.sections.isNotEmpty())
        assertEquals("dark", master.theme.themeMode)
        assertEquals("#D4AF37", master.theme.primaryColor)
    }

    @Test
    fun `test reviews parsing with populated and empty arrays`() {
        val populatedJson = """
        {
          "reviews": [
            {
              "id": "rev_1",
              "author": "Mohammad Zaid",
              "rating": 5.0,
              "comment": "Outstanding styling and hair cut service!",
              "date": "2026-03-15",
              "avatarUrl": "https://example.com/avatar.jpg"
            }
          ]
        }
        """.trimIndent()
        val adapter = moshi.adapter(MenuResponseDto::class.java)
        val populatedDto = adapter.fromJson(populatedJson)
        assertNotNull(populatedDto)
        val reviews = populatedDto!!.toReviews()
        assertEquals(1, reviews.size)
        assertEquals("Mohammad Zaid", reviews[0].author)
        assertEquals(5.0, reviews[0].rating, 0.01)
        assertEquals("Outstanding styling and hair cut service!", reviews[0].comment)

        val emptyJson = """{"reviews": []}"""
        val emptyDto = adapter.fromJson(emptyJson)
        assertNotNull(emptyDto)
        assertTrue(emptyDto!!.toReviews().isEmpty())
    }

    @Test
    fun `test offers parsing with populated and empty arrays`() {
        val populatedJson = """
        {
          "offers": [
            {
              "id": "off_1",
              "title": "Festive Groom Package",
              "subtitle": "Flat 25% Off",
              "description": "Haircut, Beard Styling and Facial",
              "discount": "25% OFF",
              "discountType": "percentage",
              "validTill": "2026-12-31",
              "enabled": true
            }
          ]
        }
        """.trimIndent()
        val adapter = moshi.adapter(MenuResponseDto::class.java)
        val populatedDto = adapter.fromJson(populatedJson)
        assertNotNull(populatedDto)
        val offers = populatedDto!!.toOffers()
        assertEquals(1, offers.size)
        assertEquals("Festive Groom Package", offers[0].title)
        assertEquals("25% OFF", offers[0].discount)
        assertEquals(false, offers[0].isExpired)

        val emptyJson = """{"offers": []}"""
        val emptyDto = adapter.fromJson(emptyJson)
        assertNotNull(emptyDto)
        assertTrue(emptyDto!!.toOffers().isEmpty())
    }

    @Test
    fun `test theme configuration parsing and defaults`() {
        val customThemeJson = """
        {
          "theme": {
            "themeMode": "light",
            "primaryColor": "#D4AF37",
            "secondaryColor": "#A67C1E",
            "backgroundColor": "#FBF8F2",
            "surfaceColor": "#FFFFFF",
            "textColor": "#1E1C16"
          }
        }
        """.trimIndent()
        val adapter = moshi.adapter(MenuResponseDto::class.java)
        val dto = adapter.fromJson(customThemeJson)
        assertNotNull(dto)
        val master = dto!!.toDomain()
        assertEquals("light", master.theme.themeMode)
        assertEquals("#D4AF37", master.theme.primaryColor)
        assertEquals("#FBF8F2", master.theme.backgroundColor)

        val missingThemeJson = """{}"""
        val missingDto = adapter.fromJson(missingThemeJson)
        assertNotNull(missingDto)
        val defaultMaster = missingDto!!.toDomain()
        assertEquals("dark", defaultMaster.theme.themeMode)
        assertEquals("#D4AF37", defaultMaster.theme.primaryColor)
    }

    @Test
    fun `test icon resolver mappings and unknown icon fallback`() {
        val homeIcon = IconResolver.resolve("home")
        assertEquals(Icons.Default.Home, homeIcon)

        val cutIcon = IconResolver.resolve("content_cut")
        assertEquals(Icons.Default.ContentCut, cutIcon)

        val reviewIcon = IconResolver.resolve("rate_review")
        assertEquals(Icons.Default.RateReview, reviewIcon)

        val premiumIcon = IconResolver.resolve("workspace_premium")
        assertEquals(Icons.Default.WorkspacePremium, premiumIcon)

        val fallbackIcon = IconResolver.resolve("unknown_arbitrary_icon_xyz")
        assertEquals(Icons.Default.Circle, fallbackIcon)

        val storeIcon = IconResolver.resolve("store")
        assertEquals(Icons.Default.Storefront, storeIcon)
    }

    @Test
    fun `test remote live menu schema mapping for colors, sections, nav, and onboarding`() {
        val remoteJson = """
        {
          "theme": {
            "themeMode": "system",
            "colors": {
              "primary": "#D4AF37",
              "secondary": "#171717",
              "background": "#101010",
              "surface": "#1C1C1C",
              "textPrimary": "#FFFFFF"
            }
          },
          "navigation": {
            "drawerItems": [
              { "id": "drawer_prices", "title": "Price List", "icon": "currency_rupee", "type": "navigate", "destination": "price_list" },
              { "id": "drawer_location", "title": "Location", "icon": "location_on", "type": "navigate", "destination": "location" },
              { "id": "drawer_franchise", "title": "Franchise", "icon": "store", "type": "navigate", "destination": "franchise" }
            ],
            "bottomItems": [
              { "id": "bottom_home", "title": "Home", "icon": "home", "selectedIcon": "home_filled", "destination": "home", "order": 1 },
              { "id": "bottom_services", "title": "Service", "icon": "content_cut", "selectedIcon": "content_cut", "destination": "services", "order": 2 },
              { "id": "bottom_offers", "title": "Offers", "icon": "local_offer", "selectedIcon": "local_offer", "destination": "offers", "order": 3 },
              { "id": "bottom_booking", "title": "Booking", "icon": "calendar_month", "selectedIcon": "calendar_month", "destination": "booking", "order": 4 },
              { "id": "bottom_profile", "title": "Profile", "icon": "account_circle", "selectedIcon": "account_circle", "destination": "profile", "order": 5 }
            ]
          },
          "onboarding": {
            "allowSkip": true,
            "nextButtonText": "Forward",
            "finishButtonText": "Start Now",
            "pages": [
              {
                "id": "welcome",
                "title": "Welcome",
                "description": "Royal Grooming",
                "imageUrl": "https://example.com/welcome.jpg"
              }
            ]
          },
          "home": {
            "sections": [
              { "id": "home_banners", "type": "banner_carousel", "title": "Featured", "order": 1 },
              { "id": "home_quick", "type": "service_grid", "title": "Quick Services", "order": 2, "maxItems": 6 },
              { "id": "home_popular", "type": "service_list_horizontal", "title": "Popular", "order": 3, "maxItems": 8 },
              { "id": "home_why", "type": "feature_grid", "title": "Why Choose Us", "order": 4 },
              { "id": "home_gal", "type": "gallery_preview", "title": "Gallery", "order": 5, "maxItems": 3 },
              { "id": "home_offers", "type": "offer_list", "title": "Offers", "order": 6 },
              { "id": "home_reviews", "type": "review_list", "title": "Reviews", "order": 7 },
              { "id": "home_loc", "type": "salon_location_card", "title": "Location", "order": 8, "action": { "destination": "location" } },
              { "id": "home_fran", "type": "franchise_banner", "title": "Franchise", "order": 9, "action": { "destination": "franchise" } }
            ]
          }
        }
        """.trimIndent()

        val adapter = moshi.adapter(MenuResponseDto::class.java)
        val dto = adapter.fromJson(remoteJson)
        assertNotNull(dto)

        val master = dto!!.toDomain()
        assertEquals("#D4AF37", master.theme.primaryColor)
        assertEquals("#171717", master.theme.secondaryColor)
        assertEquals("#101010", master.theme.backgroundColor)

        // Navigation drawer routes should be normalized
        val drawerPrices = master.drawer.find { it.id == "drawer_prices" }
        assertNotNull(drawerPrices)
        assertEquals("price_list", drawerPrices!!.route)

        val drawerLocation = master.drawer.find { it.id == "drawer_location" }
        assertNotNull(drawerLocation)
        assertEquals("locations", drawerLocation!!.route)

        val drawerFranchise = master.drawer.find { it.id == "drawer_franchise" }
        assertNotNull(drawerFranchise)
        assertEquals("about", drawerFranchise!!.route)

        // Bottom nav: Exactly 5 items in order
        assertEquals(5, master.bottomNavigation.size)
        assertEquals("home", master.bottomNavigation[0].route)
        assertEquals("services", master.bottomNavigation[1].route)
        assertEquals("offers", master.bottomNavigation[2].route)
        assertEquals("booking", master.bottomNavigation[3].route)
        assertEquals("profile", master.bottomNavigation[4].route)

        // Onboarding
        assertEquals(true, master.onboarding.showSkip)
        val firstPage = master.onboarding.pages.first()
        assertEquals("https://example.com/welcome.jpg", firstPage.illustrationUrl)
        assertEquals("Forward", firstPage.nextLabel)
        assertEquals("Start Now", firstPage.getStartedLabel)

        // Home sections
        val homeConfig = dto.toHomeConfig()
        val sections = homeConfig.sections
        assertEquals(9, sections.size)
        assertEquals(com.almalika.salon.domain.model.SectionType.HERO, sections[0].type)
        assertEquals(com.almalika.salon.domain.model.SectionType.QUICK_ACTIONS, sections[1].type)
        assertEquals(6, sections[1].itemsToShow)
        assertEquals(com.almalika.salon.domain.model.SectionType.SERVICES, sections[2].type)
        assertEquals(8, sections[2].itemsToShow)
        assertEquals(com.almalika.salon.domain.model.SectionType.ABOUT, sections[3].type)
        assertEquals(com.almalika.salon.domain.model.SectionType.GALLERY, sections[4].type)
        assertEquals(3, sections[4].itemsToShow)
        assertEquals(com.almalika.salon.domain.model.SectionType.OFFERS, sections[5].type)
        assertEquals(com.almalika.salon.domain.model.SectionType.REVIEWS, sections[6].type)
        assertEquals(com.almalika.salon.domain.model.SectionType.LOCATIONS, sections[7].type)
        assertEquals("locations", sections[7].actionRoute)
        assertEquals(com.almalika.salon.domain.model.SectionType.BANNER, sections[8].type)
        assertEquals("about", sections[8].actionRoute)
    }
}
