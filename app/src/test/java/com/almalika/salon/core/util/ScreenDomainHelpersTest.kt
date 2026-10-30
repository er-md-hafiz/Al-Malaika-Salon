package com.almalika.salon.core.util

import com.almalika.salon.data.remote.config.GalleryItem
import com.almalika.salon.data.remote.config.Review
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenDomainHelpersTest {

    @Test
    fun testReviewStatsCalculation_17Reviews() {
        // 17 reviews: 10x5, 6x4, 1x3 = 77 / 17 = 4.53 -> "4.5 ★ · 17 reviews"
        val reviews = mutableListOf<Review>()
        repeat(10) { reviews.add(Review(id = it, rating = 5)) }
        repeat(6) { reviews.add(Review(id = 10 + it, rating = 4)) }
        reviews.add(Review(id = 16, rating = 3))

        val summary = ReviewStatsCalculator.computeSummary(reviews)
        assertEquals(17, summary.totalCount)
        assertEquals("4.5 ★ · 17 reviews", summary.formattedSummary)
    }

    @Test
    fun testUrlValidatorRejectsExampleDomainsAndLocalhost() {
        assertFalse(UrlValidator.isUsable("https://example.com/franchise"))
        assertFalse(UrlValidator.isUsable("https://example.org/form"))
        assertFalse(UrlValidator.isUsable("http://localhost:8080"))
        assertFalse(UrlValidator.isUsable("http://127.0.0.1/api"))
        assertFalse(UrlValidator.isUsable(""))
        assertFalse(UrlValidator.isUsable(null))

        assertTrue(UrlValidator.isUsable("https://almalaika.in/franchise"))
        assertTrue(UrlValidator.isUsable("tel:+919876543210"))
        assertTrue(UrlValidator.isUsable("mailto:contact@almalaika.in"))
    }

    @Test
    fun testGalleryCategoryResolver_AllNullHidesChips() {
        val allNullCategories = listOf(
            GalleryItem(id = 1, title = "Haircut 1", category = null),
            GalleryItem(id = 2, title = "Haircut 2", category = null)
        )
        assertFalse(GalleryCategoryResolver.shouldShowCategoryChips(allNullCategories))

        val mixedCategories = listOf(
            GalleryItem(id = 1, title = "Haircut 1", category = "Hair"),
            GalleryItem(id = 2, title = "Beard 1", category = null)
        )
        assertTrue(GalleryCategoryResolver.shouldShowCategoryChips(mixedCategories))
    }
}

