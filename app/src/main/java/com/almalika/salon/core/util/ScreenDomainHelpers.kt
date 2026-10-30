package com.almalika.salon.core.util

import com.almalika.salon.data.remote.config.GalleryItem
import com.almalika.salon.data.remote.config.Review

object ReviewStatsCalculator {
    data class ReviewSummary(
        val averageRating: Float,
        val totalCount: Int,
        val formattedSummary: String
    )

    /**
     * R7: Computes review statistics dynamically at runtime from reviews[].
     * Never trusts salon.rating or salon.totalReviews directly.
     */
    fun computeSummary(reviews: List<Review>): ReviewSummary {
        if (reviews.isEmpty()) {
            return ReviewSummary(0f, 0, "No reviews yet")
        }
        val count = reviews.size
        val avg = reviews.map { it.rating }.average().toFloat()
        val roundedAvg = String.format(java.util.Locale.US, "%.1f", avg)
        return ReviewSummary(
            averageRating = avg,
            totalCount = count,
            formattedSummary = "$roundedAvg ★ · $count reviews"
        )
    }
}

object UrlValidator {
    /**
     * R3: Validates whether a URL is safe and actionable.
     * Rejects empty, blank, localhost, 127.0.0.1, and example.* domains.
     */
    fun isUsable(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val trimmed = url.trim().lowercase()
        if (trimmed.contains("example.com") || trimmed.contains("example.org") || trimmed.contains("example.net")) {
            return false
        }
        if (trimmed.startsWith("http://localhost") || trimmed.startsWith("https://localhost") || trimmed.contains("127.0.0.1")) {
            return false
        }
        return trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("tel:") || trimmed.startsWith("mailto:")
    }
}

object GalleryCategoryResolver {
    /**
     * R2: If every item in galleryItems has category == null or blank,
     * the category filter chip row must be hidden completely.
     */
    fun shouldShowCategoryChips(items: List<GalleryItem>): Boolean {
        return items.any { !it.category.isNullOrBlank() }
    }
}

