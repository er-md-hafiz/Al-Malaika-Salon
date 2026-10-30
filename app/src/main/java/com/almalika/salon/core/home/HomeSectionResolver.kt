package com.almalika.salon.core.home

import com.almalika.salon.data.remote.config.HomeSection
import com.almalika.salon.data.remote.config.RemoteConfig

object HomeSectionResolver {

    /**
     * Resolves whether a section should be rendered based on:
     * 1. section.enabled
     * 2. Mapped feature flag
     * 3. DataSource existence (and hideWhenEmpty / hideWhenDisabled rules)
     */
    fun shouldRenderSection(section: HomeSection, config: RemoteConfig): Boolean {
        if (!section.enabled) return false

        // Feature flag gating
        val flagEnabled = when (section.type) {
            "banner_carousel" -> config.featureFlags.homeBanners
            "service_grid" -> config.featureFlags.quickServices
            "service_list_horizontal" -> config.featureFlags.popularServices
            "feature_grid" -> true
            "gallery_preview" -> config.featureFlags.gallery
            "offer_list" -> config.featureFlags.offers
            "review_list" -> config.featureFlags.reviews
            "salon_location_card" -> true
            "franchise_banner" -> config.featureFlags.franchise && (config.franchise?.enabled ?: false)
            else -> true
        }
        if (!flagEnabled) {
            return false
        }

        // DataSource presence and emptiness
        val ds = section.dataSource
        if (ds != null) {
            val hasData = when (ds) {
                "banners" -> config.banners.isNotEmpty()
                "quickServices" -> config.quickServices.isNotEmpty()
                "popularServices" -> config.popularServices.isNotEmpty()
                "homeServices" -> config.homeServices.isNotEmpty()
                "whyChooseUs" -> config.whyChooseUs.isNotEmpty()
                "gallery", "galleryItems" -> config.galleryItems.isNotEmpty()
                "offers" -> config.offers.isNotEmpty()
                "reviews" -> config.reviews.isNotEmpty()
                "franchise" -> config.franchise != null && (config.franchise.enabled)
                "salon" -> config.salon != null
                else -> false
            }

            if (!hasData && section.hideWhenEmpty) {
                return false
            }
        }

        return true
    }

    /**
     * Returns the ordered, validated list of sections ready for rendering
     */
    fun getVisibleSections(config: RemoteConfig): List<HomeSection> {
        return config.home.sections
            .filter { shouldRenderSection(it, config) }
            .sortedBy { it.order }
    }
}

