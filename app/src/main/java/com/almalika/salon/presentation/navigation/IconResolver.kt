package com.almalika.salon.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconResolver {
    fun resolve(iconName: String): ImageVector {
        return when (iconName.lowercase().trim()) {
            "home", "home_filled" -> Icons.Default.Home
            "content_cut", "scissors" -> Icons.Default.ContentCut
            "local_offer", "offer" -> Icons.Default.LocalOffer
            "calendar_month", "booking", "calendar" -> Icons.Default.CalendarMonth
            "home_repair_service", "homeservice" -> Icons.Default.HomeRepairService
            "photo_library", "gallery" -> Icons.Default.PhotoLibrary
            "place", "locations", "branch", "map", "location_on", "location" -> Icons.Default.Place
            "info", "about" -> Icons.Default.Info
            "call", "contact", "phone" -> Icons.Default.Call
            "settings" -> Icons.Default.Settings
            "share" -> Icons.Default.Share
            "star", "rate" -> Icons.Default.Star
            "rate_review", "review", "reviews" -> Icons.Default.RateReview
            "description", "terms" -> Icons.Default.Description
            "privacy_tip", "privacy" -> Icons.Default.PrivacyTip
            "dark_mode" -> Icons.Default.DarkMode
            "notifications" -> Icons.Default.Notifications
            "health_and_safety" -> Icons.Default.HealthAndSafety
            "currency_rupee" -> Icons.Default.CurrencyRupee
            "workspace_premium" -> Icons.Default.WorkspacePremium
            "face" -> Icons.Default.Face
            "brush" -> Icons.Default.Brush
            "inventory_2" -> Icons.Default.Inventory2
            "palette" -> Icons.Default.Palette
            "spa" -> Icons.Default.Spa
            "child_care" -> Icons.Default.ChildCare
            "account", "profile", "person", "user" -> Icons.Default.AccountCircle
            "store", "storefront" -> Icons.Default.Storefront
            else -> Icons.Default.Circle
        }
    }
}
