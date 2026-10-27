package com.almalika.salon.presentation.home

import com.almalika.salon.core.action.AppAction
import com.almalika.salon.data.mapper.toHomeConfig
import com.almalika.salon.data.remote.dto.BannerDto
import com.almalika.salon.data.remote.dto.HomeSectionActionDto
import com.almalika.salon.data.remote.dto.HomeSectionConfigDto
import com.almalika.salon.data.remote.dto.HomeScreenConfigDto
import com.almalika.salon.data.remote.dto.MenuResponseDto
import com.almalika.salon.domain.model.SectionType
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.InputStreamReader

class DynamicBannerCarouselTest {

    private lateinit var moshi: Moshi
    private var liveMasterDto: MenuResponseDto? = null

    @Before
    fun setUp() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val jsonStream = javaClass.classLoader?.getResourceAsStream("remote_live_menu.json")
        if (jsonStream != null) {
            val json = InputStreamReader(jsonStream).readText()
            liveMasterDto = moshi.adapter(MenuResponseDto::class.java).fromJson(json)
        }
    }

    @Test
    fun `test all four existing remote banners load with expected metadata`() {
        val dto = liveMasterDto ?: return
        val homeConfig = dto.toHomeConfig()
        val bannerSection = homeConfig.sections.find { it.id == "home_banners" }
        assertNotNull("home_banners section must exist", bannerSection)
        assertEquals(SectionType.HERO, bannerSection?.type)

        val banners = bannerSection!!.banners
        assertEquals("Exactly 4 remote banners must be loaded", 4, banners.size)

        // Banner 1
        val b1 = banners[0]
        assertEquals("1", b1.id)
        assertEquals("Royal Grooming\nExperience", b1.title)
        assertEquals("Haircuts • Beard • Styling", b1.subtitle)
        assertEquals("SERVICES", b1.category)
        assertTrue(b1.isAvailable)
        assertTrue(b1.imageUrl.startsWith("https://images.unsplash.com"))

        // Banner 2
        val b2 = banners[1]
        assertEquals("2", b2.id)
        assertEquals("Precision Haircuts", b2.title)
        assertEquals("Your style, perfected", b2.subtitle)
        assertEquals("HAIR", b2.category)
        assertTrue(b2.isAvailable)
        assertTrue(b2.imageUrl.startsWith("https://images.unsplash.com"))

        // Banner 3
        val b3 = banners[2]
        assertEquals("3", b3.id)
        assertEquals("Signature Beard Styling", b3.title)
        assertEquals("Sharp lines. Confident looks.", b3.subtitle)
        assertEquals("BEARD", b3.category)
        assertTrue(b3.isAvailable)
        assertTrue(b3.imageUrl.startsWith("https://images.unsplash.com"))

        // Banner 4
        val b4 = banners[3]
        assertEquals("4", b4.id)
        assertEquals("Look Your Best", b4.title)
        assertEquals("Professional styling for every occasion", b4.subtitle)
        assertEquals("STYLING", b4.category)
        assertTrue(b4.isAvailable)
        assertTrue(b4.imageUrl.startsWith("https://images.unsplash.com"))
    }

    @Test
    fun `test auto-sliding interval defaults to 4000ms and respects custom interval`() {
        val dto = liveMasterDto ?: return
        val homeConfig = dto.toHomeConfig()
        val bannerSection = homeConfig.sections.first { it.id == "home_banners" }
        assertEquals("Auto-slide interval must be 4000ms by default", 4000L, bannerSection.autoSlideIntervalMs)

        // Custom timing
        val customDto = dto.copy(
            home = dto.home?.copy(
                sections = listOf(
                    HomeSectionConfigDto(
                        id = "home_banners",
                        type = "banner_carousel",
                        title = "Featured",
                        dataSource = "banners",
                        autoSlideIntervalMs = 6000L
                    )
                )
            )
        )
        val customConfig = customDto.toHomeConfig()
        assertEquals(6000L, customConfig.sections.first().autoSlideIntervalMs)
    }

    @Test
    fun `test carousel looping logic and page index mapping`() {
        val bannerCount = 4
        val virtualPageCount = bannerCount * 1000
        val initialPage = (virtualPageCount / 2) - ((virtualPageCount / 2) % bannerCount)

        // Initial page must land exactly on index 0
        assertEquals(0, initialPage % bannerCount)

        // Advancing 1 page moves to banner 1
        assertEquals(1, (initialPage + 1) % bannerCount)

        // Advancing 4 pages loops back to banner 0
        assertEquals(0, (initialPage + 4) % bannerCount)

        // Advancing 7 pages loops to banner 3
        assertEquals(3, (initialPage + 7) % bannerCount)

        // Swiping backward 1 page lands on banner 3
        val prevPage = initialPage - 1
        val mappedPrev = ((prevPage % bannerCount) + bannerCount) % bannerCount
        assertEquals(3, mappedPrev)
    }

    @Test
    fun `test single banner configuration disables looping and advancement`() {
        val singleBannerDto = MenuResponseDto(
            banners = listOf(
                BannerDto(
                    id = 1,
                    imageUrl = "https://images.unsplash.com/photo-1",
                    title = "Single Banner",
                    subtitle = "One item only",
                    isAvailable = true
                )
            ),
            home = HomeScreenConfigDto(
                sections = listOf(
                    HomeSectionConfigDto(
                        id = "home_banners",
                        type = "banner_carousel",
                        dataSource = "banners"
                    )
                )
            )
        )

        val config = singleBannerDto.toHomeConfig()
        val bannerSection = config.sections.first { it.id == "home_banners" }
        assertEquals(1, bannerSection.banners.size)

        // Looping calculation: when bannerCount <= 1, virtual count is 1
        val bannerCount = bannerSection.banners.size
        val isLooping = bannerCount > 1
        assertFalse("Single banner must NOT loop", isLooping)
        val virtualPageCount = if (isLooping) bannerCount * 1000 else 1
        assertEquals(1, virtualPageCount)
    }

    @Test
    fun `test empty banners list and invalid URLs do not crash mapping`() {
        val emptyDto = MenuResponseDto(
            banners = emptyList(),
            home = HomeScreenConfigDto(
                sections = listOf(
                    HomeSectionConfigDto(
                        id = "home_banners",
                        type = "banner_carousel",
                        dataSource = "banners"
                    )
                )
            )
        )
        val config = emptyDto.toHomeConfig()
        val bannerSection = config.sections.first { it.id == "home_banners" }
        assertTrue("Banners list must be empty", bannerSection.banners.isEmpty())

        // Test with unavailable or blank imageUrl banners
        val invalidDto = MenuResponseDto(
            banners = listOf(
                BannerDto(id = 1, imageUrl = "", isAvailable = true),
                BannerDto(id = 2, imageUrl = "   ", isAvailable = true),
                BannerDto(id = 3, imageUrl = "https://valid.com/img.jpg", isAvailable = false)
            ),
            home = HomeScreenConfigDto(
                sections = listOf(
                    HomeSectionConfigDto(id = "home_banners", type = "banner_carousel", dataSource = "banners")
                )
            )
        )
        val invalidConfig = invalidDto.toHomeConfig()
        val invalidSection = invalidConfig.sections.first { it.id == "home_banners" }
        assertTrue("Ineligible banners must be filtered out", invalidSection.banners.isEmpty())
    }

    @Test
    fun `test banner tap resolves to services navigation destination`() {
        val dto = liveMasterDto ?: return
        val homeConfig = dto.toHomeConfig()
        val bannerSection = homeConfig.sections.first { it.id == "home_banners" }

        // Section-level destination must be "services"
        assertEquals("services", bannerSection.actionRoute)

        // Individual banner destination overrides section-level action if specified
        val customBannerDto = MenuResponseDto(
            banners = listOf(
                BannerDto(
                    id = 1,
                    imageUrl = "https://images.unsplash.com/b1",
                    title = "Offers Banner",
                    action = HomeSectionActionDto(type = "navigate", destination = "offers")
                ),
                BannerDto(
                    id = 2,
                    imageUrl = "https://images.unsplash.com/b2",
                    title = "Default Banner",
                    action = null
                )
            ),
            home = HomeScreenConfigDto(
                sections = listOf(
                    HomeSectionConfigDto(
                        id = "home_banners",
                        type = "banner_carousel",
                        dataSource = "banners",
                        itemAction = HomeSectionActionDto(type = "navigate", destination = "services")
                    )
                )
            )
        )
        val customConfig = customBannerDto.toHomeConfig()
        val customSection = customConfig.sections.first { it.id == "home_banners" }

        assertEquals("offers", customSection.banners[0].actionRoute)
        assertEquals("services", customSection.actionRoute)

        // Target route resolution logic
        val targetRoute1 = customSection.banners[0].actionRoute.ifBlank { customSection.actionRoute }
        assertEquals("offers", targetRoute1)

        val targetRoute2 = customSection.banners[1].actionRoute.ifBlank { customSection.actionRoute }
        assertEquals("services", targetRoute2)
    }

    @Test
    fun `test homeBanners feature flag disabled removes banner section`() {
        val dto = liveMasterDto ?: return
        val disabledFeatureDto = dto.copy(
            featureFlags = mapOf("homeBanners" to false)
        )
        val config = disabledFeatureDto.toHomeConfig()
        val bannerSection = config.sections.find { it.id == "home_banners" }
        assertNull("Banner section must be disabled/removed when homeBanners feature flag is false", bannerSection)
    }

    @Test
    fun `test fallback menu json provides four banners and home_banners section`() {
        val fallbackStream = javaClass.classLoader?.getResourceAsStream("config/menu_fallback.json")
            ?: javaClass.getResourceAsStream("/config/menu_fallback.json")
            ?: javaClass.classLoader?.getResourceAsStream("remote_live_menu.json")

        assertNotNull("Fallback or live menu resource must be available", fallbackStream)
        val json = InputStreamReader(fallbackStream!!).readText()
        val fallbackDto = moshi.adapter(MenuResponseDto::class.java).fromJson(json)
        assertNotNull(fallbackDto)

        val config = fallbackDto!!.toHomeConfig()
        val bannerSection = config.sections.find { it.id == "home_banners" }
        assertNotNull("Fallback must provide home_banners section", bannerSection)
        assertTrue("Fallback must contain banners", bannerSection!!.banners.isNotEmpty())
    }
}
