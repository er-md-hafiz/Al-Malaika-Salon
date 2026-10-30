package com.almalika.salon.core.home

import com.almalika.salon.data.remote.config.Banner
import com.almalika.salon.data.remote.config.FeatureFlags
import com.almalika.salon.data.remote.config.FranchiseConfig
import com.almalika.salon.data.remote.config.HomeConfig
import com.almalika.salon.data.remote.config.HomeSection
import com.almalika.salon.data.remote.config.RemoteConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeSectionResolverTest {

    @Test
    fun testSectionsOrderRespected() {
        val sections = listOf(
            HomeSection(id = "sec2", type = "banner_carousel", order = 2, enabled = true),
            HomeSection(id = "sec1", type = "service_grid", order = 1, enabled = true)
        )
        val config = RemoteConfig(
            home = HomeConfig(sections = sections),
            banners = listOf(Banner(id = "1", title = "B1"))
        )
        val visible = HomeSectionResolver.getVisibleSections(config)
        assertEquals("sec1", visible[0].id)
        assertEquals("sec2", visible[1].id)
    }

    @Test
    fun testDisabledSectionExcluded() {
        val sections = listOf(
            HomeSection(id = "sec1", type = "banner_carousel", order = 1, enabled = false)
        )
        val config = RemoteConfig(home = HomeConfig(sections = sections))
        val visible = HomeSectionResolver.getVisibleSections(config)
        assertTrue(visible.isEmpty())
    }

    @Test
    fun testHideWhenEmptyDataSourceMissing() {
        val sections = listOf(
            HomeSection(id = "sec1", type = "service_grid", dataSource = "quickServices", hideWhenEmpty = true)
        )
        val config = RemoteConfig(
            home = HomeConfig(sections = sections),
            quickServices = emptyList()
        )
        val visible = HomeSectionResolver.getVisibleSections(config)
        assertTrue(visible.isEmpty())
    }

    @Test
    fun testFranchiseBannerGatedByFeatureFlagAndAvailability() {
        val sections = listOf(
            HomeSection(id = "franchise_banner", type = "franchise_banner", dataSource = "franchise", hideWhenDisabled = true)
        )
        val configDisabled = RemoteConfig(
            home = HomeConfig(sections = sections),
            featureFlags = FeatureFlags(franchise = false),
            franchise = FranchiseConfig(enabled = false)
        )
        val visibleDisabled = HomeSectionResolver.getVisibleSections(configDisabled)
        assertTrue(visibleDisabled.isEmpty())

        val configEnabled = RemoteConfig(
            home = HomeConfig(sections = sections),
            featureFlags = FeatureFlags(franchise = true),
            franchise = FranchiseConfig(enabled = true)
        )
        val visibleEnabled = HomeSectionResolver.getVisibleSections(configEnabled)
        assertEquals(1, visibleEnabled.size)
    }
}

