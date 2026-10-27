package com.almalika.salon.data

import com.almalika.salon.data.mapper.*
import com.almalika.salon.data.remote.dto.MenuResponseDto
import com.almalika.salon.domain.model.SectionType
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * End-to-end automated test validating the live menu endpoint and JSON schema for Al-Malaika Salon:
 * https://er-md-hafiz.github.io/api.almalaikasalon/menu.json
 */
class LiveMenuEndpointValidationTest {

    private lateinit var moshi: Moshi
    private lateinit var liveJsonContent: String
    private var liveDto: MenuResponseDto? = null
    private lateinit var rawJsonMap: Map<String, Any?>

    @Before
    fun setUp() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val resourceStream = javaClass.classLoader?.getResourceAsStream("remote_live_menu.json")
        liveJsonContent = if (resourceStream != null) {
            resourceStream.bufferedReader().use { it.readText() }
        } else {
            val candidateFiles = listOf(
                File("downloaded_menu.json"),
                File("../downloaded_menu.json"),
                File("src/test/resources/remote_live_menu.json"),
                File("src/main/assets/config/menu_fallback.json")
            )
            val validFile = candidateFiles.firstOrNull { it.exists() && it.length() > 0 }
            assertNotNull("A valid menu JSON file must exist in test environment", validFile)
            validFile!!.readText()
        }

        val mapAdapter = moshi.adapter<Map<String, Any?>>(
            Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        )
        rawJsonMap = mapAdapter.fromJson(liveJsonContent) ?: emptyMap()

        val adapter = moshi.adapter(MenuResponseDto::class.java)
        liveDto = adapter.fromJson(liveJsonContent)
        assertNotNull("Live menu JSON must parse without deserialization failure", liveDto)
    }

    @Test
    fun testLiveMenuMetadataAndAppConfig() {
        val dto = liveDto!!
        assertNotNull(dto.metadata)
        assertEquals("1.2.0", dto.metadata?.schemaVersion)

        @Suppress("UNCHECKED_CAST")
        val rawMeta = rawJsonMap["metadata"] as? Map<String, Any?>
        assertNotNull(rawMeta)
        assertEquals(3.0, (rawMeta?.get("uiConfigVersion") as? Number)?.toDouble() ?: 0.0, 0.01)

        val master = dto.toDomain()
        assertEquals("Al-Malaika Salon", master.salon.name)
        assertEquals("Pugmil, Hazaribagh, Jharkhand, India", master.salon.address)
        // Defect in live endpoint: salon.mobileNumber is empty in remote JSON
        assertEquals("", master.salon.phone)
    }

    @Test
    fun testDiscoveredScreensInventoryAndAttributes() {
        @Suppress("UNCHECKED_CAST")
        val screensList = rawJsonMap["screens"] as? List<Map<String, Any?>>
        assertNotNull("Screens list must be configured in live menu JSON", screensList)
        assertTrue(screensList!!.isNotEmpty())

        val screenIds = screensList.mapNotNull { it["id"] as? String }.toSet()

        val expectedScreens = listOf(
            "home", "services", "price_list", "gallery", "offers", "reviews",
            "location", "contact", "franchise", "settings", "about", "privacy",
            "terms", "login", "register", "email_link_sent"
        )
        for (expected in expectedScreens) {
            assertTrue("Screen id '$expected' must be configured in screens list", screenIds.contains(expected))
        }

        // Verify ID uniqueness
        assertEquals("All screen IDs must be strictly unique", screensList.size, screenIds.size)
    }

    @Test
    fun testNavigationDrawerDiscoveredItemsAndNormalization() {
        val dto = liveDto!!
        val master = dto.toDomain()
        val drawer = master.drawer
        assertTrue("Drawer must contain mapped navigation items", drawer.isNotEmpty())

        // Ensure critical destinations exist and are normalized to app routes
        val servicesItem = drawer.find { it.id == "drawer_services" || it.route == "services" }
        assertNotNull("Services must exist in drawer", servicesItem)
        assertEquals("services", servicesItem?.route)

        val pricesItem = drawer.find { it.id == "drawer_prices" }
        assertNotNull("Price list item must be discovered in drawer", pricesItem)
        assertEquals("price_list", pricesItem?.route) // price_list destination in drawer

        val locationItem = drawer.find { it.id == "drawer_location" }
        assertNotNull("Location item must be discovered in drawer", locationItem)
        assertEquals("locations", locationItem?.route) // location normalized to locations

        val franchiseItem = drawer.find { it.id == "drawer_franchise" }
        assertNotNull("Franchise item must be discovered in drawer", franchiseItem)
        assertEquals("about", franchiseItem?.route) // franchise normalized to about

        val contactItem = drawer.find { it.id == "drawer_contact" }
        assertNotNull("Contact item must be discovered in drawer", contactItem)
        assertEquals("contact", contactItem?.route)

        val settingsItem = drawer.find { it.id == "drawer_settings" }
        assertNotNull("Settings item must be discovered in drawer", settingsItem)
        assertEquals("settings", settingsItem?.route)

        val privacyItem = drawer.find { it.id == "drawer_privacy" }
        assertNotNull("Privacy item must be discovered in drawer", privacyItem)
        assertEquals("privacy", privacyItem?.route)

        val termsItem = drawer.find { it.id == "drawer_terms" }
        assertNotNull("Terms item must be discovered in drawer", termsItem)
        assertEquals("terms", termsItem?.route)

        val accountItem = drawer.find { it.id == "drawer_account" }
        assertNotNull("Account item must be discovered in drawer", accountItem)
        assertEquals("login", accountItem?.route)
    }

    @Test
    fun testNavigationBottomItemsDiscoveredAndMapped() {
        val dto = liveDto!!
        val master = dto.toDomain()
        val bottomNav = master.bottomNavigation
        assertTrue("Bottom navigation items must be configured", bottomNav.isNotEmpty())

        // The bottom navigation must contain exactly these five destinations, in this order: Home, Service, Offers, Booking, Profile
        assertEquals(5, bottomNav.size)
        val expectedRoutes = listOf("home", "services", "offers", "booking", "profile")
        assertEquals(expectedRoutes, bottomNav.map { it.route })

        val bottomIds = bottomNav.map { it.id }.toSet()
        assertTrue(bottomIds.contains("bottom_home"))
        assertTrue(bottomIds.contains("bottom_services"))
        assertTrue(bottomIds.contains("bottom_offers"))
        assertTrue(bottomIds.contains("bottom_booking"))
        assertTrue(bottomIds.contains("bottom_profile"))

        assertFalse("bottom_prices must be completely removed from bottom navigation", bottomIds.contains("bottom_prices"))
        assertFalse("bottom_gallery must be completely removed from bottom navigation", bottomIds.contains("bottom_gallery"))
        assertFalse("bottom_contact must be completely removed from bottom navigation", bottomIds.contains("bottom_contact"))

        val profileBottom = bottomNav.find { it.id == "bottom_profile" }
        assertEquals("profile", profileBottom?.route)
    }

    @Test
    fun testHomeSectionsDiscoveryAndTypeResolution() {
        val dto = liveDto!!
        val homeConfig = dto.toHomeConfig()
        val sections = homeConfig.sections
        assertTrue("Home screen must contain discovered sections", sections.isNotEmpty())
        assertEquals(9, sections.size)

        assertEquals("home_banners", sections[0].id)
        assertEquals(SectionType.HERO, sections[0].type)

        assertEquals("home_quick_services", sections[1].id)
        assertEquals(SectionType.QUICK_ACTIONS, sections[1].type)
        assertEquals(6, sections[1].itemsToShow)

        assertEquals("home_popular_services", sections[2].id)
        assertEquals(SectionType.SERVICES, sections[2].type)
        assertEquals(8, sections[2].itemsToShow)

        assertEquals("home_why_choose_us", sections[3].id)
        assertEquals(SectionType.ABOUT, sections[3].type)

        assertEquals("home_gallery", sections[4].id)
        assertEquals(SectionType.GALLERY, sections[4].type)
        assertEquals(3, sections[4].itemsToShow)

        assertEquals("home_offers", sections[5].id)
        assertEquals(SectionType.OFFERS, sections[5].type)

        assertEquals("home_reviews", sections[6].id)
        assertEquals(SectionType.REVIEWS, sections[6].type)

        assertEquals("home_location", sections[7].id)
        assertEquals(SectionType.LOCATIONS, sections[7].type)
        assertEquals("locations", sections[7].actionRoute)

        assertEquals("home_franchise", sections[8].id)
        assertEquals(SectionType.BANNER, sections[8].type)
        assertEquals("about", sections[8].actionRoute)
    }

    @Test
    fun testServicesAndPriceListCatalogIntegrity() {
        val dto = liveDto!!
        val serviceCategories = dto.toServiceCategories()
        assertTrue("Service categories must be populated from catalog", serviceCategories.isNotEmpty())

        val allFlatServices = serviceCategories.flatMap { it.services }
        assertTrue("Services catalog must contain configured items", allFlatServices.size >= 11)

        val classicHaircut = allFlatServices.find { it.name == "Classic Haircut" }
        assertNotNull(classicHaircut)
        assertEquals(150.0, classicHaircut!!.price, 0.01)

        val beardTrim = allFlatServices.find { it.name == "Beard Trim" }
        assertNotNull(beardTrim)
        assertEquals(80.0, beardTrim!!.price, 0.01)

        val cleanShave = allFlatServices.find { it.name == "Clean Shave" }
        assertNotNull(cleanShave)
        assertEquals(100.0, cleanShave!!.price, 0.01)

        val hairColour = allFlatServices.find { it.name == "Hair Colour" }
        assertNotNull(hairColour)
        assertEquals(699.0, hairColour!!.price, 0.01)

        val completePackage = allFlatServices.find { it.name == "Complete Grooming Package" }
        assertNotNull(completePackage)
        assertEquals(399.0, completePackage!!.price, 0.01)
    }

    @Test
    fun testGalleryItemsAndBarbersIntegrity() {
        val dto = liveDto!!
        val gallery = dto.toGalleryImages()
        assertTrue("Gallery items must be discovered", gallery.isNotEmpty())
        assertEquals(3, gallery.size)
        assertTrue(gallery.all { it.imageUrl.startsWith("https://") })

        assertNotNull(dto.barbers)
        assertEquals(1, dto.barbers!!.size)
        assertEquals("Any Available", dto.barbers!![0].name)
    }

    @Test
    fun testOnboardingPagesAndStartupRouting() {
        val dto = liveDto!!
        val master = dto.toDomain()
        assertTrue(master.onboarding.enabled)
        assertTrue(master.onboarding.showSkip)
        assertEquals(3, master.onboarding.pages.size)

        val page1 = master.onboarding.pages[0]
        assertEquals("welcome", page1.id)
        assertTrue(page1.illustrationUrl.startsWith("https://"))

        assertEquals("splash", master.startup.splashScreenId)
        assertEquals("home", master.startup.homeScreenId)
    }

    @Test
    fun testExternalUrlsFormattingAndCompliance() {
        val dto = liveDto!!
        val master = dto.toDomain()

        // Privacy and terms must be HTTPS
        assertTrue(master.legal.privacyUrl.startsWith("https://"))
        assertTrue(master.legal.termsUrl.startsWith("https://"))
        assertTrue(master.legal.aboutUsUrl.startsWith("https://"))

        // WhatsApp number verification:
        // Note: live salon.mobileNumber is empty string, which safely results in disabled WhatsApp config
        assertFalse("WhatsApp config should be disabled when salon phone is blank", master.whatsapp.enabled)
        assertEquals("", master.whatsapp.number)

        // However franchise contact is populated in DTO
        val franchise = dto.franchise
        assertNotNull(franchise)
        assertEquals("09708101242", franchise?.contactNumber)
        assertEquals("919708101242", franchise?.whatsApp)

        // Google Maps URL
        val branches = dto.toBranches()
        assertTrue(branches.isNotEmpty())
        assertTrue(branches[0].googleMapsUrl.contains("google.com/maps") || branches[0].googleMapsUrl.startsWith("https://maps.google.com"))
    }

    @Test
    fun testNegativeScenariosSafelyHandledWithoutCrashing() {
        val adapter = moshi.adapter(MenuResponseDto::class.java)

        // 1. Missing / null fields
        val emptyDto = adapter.fromJson("{}")
        assertNotNull(emptyDto)
        val defaultMaster = emptyDto!!.toDomain()
        assertNotNull(defaultMaster)
        assertEquals("Al-Malaika Salon", defaultMaster.salon.name)
        assertTrue(emptyDto.toOffers().isEmpty())
        assertTrue(emptyDto.toReviews().isEmpty())
        assertTrue(emptyDto.toGalleryImages().isEmpty())
        assertTrue(emptyDto.toServiceCategories().isEmpty())

        // 2. Corrupted / unknown fields in JSON
        val rogueJson = """
            {
               "unknown_future_field_99": { "nested": 12345 },
               "screens": [
                   { "id": "unknown_screen_xyz", "type": "alien_type", "enabled": true }
               ]
            }
        """.trimIndent()
        val rogueDto = adapter.fromJson(rogueJson)
        assertNotNull(rogueDto)
        val rogueMaster = rogueDto!!.toDomain()
        assertNotNull(rogueMaster)
    }
}
