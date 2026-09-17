package com.almalika.salon.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconResolver {
    fun resolve(iconName: String): ImageVector {
        return when (iconName.lowercase().trim()) {
            "home" -> Icons.Default.Home
            "content_cut", "scissors" -> Icons.Default.ContentCut
            "local_offer", "offer" -> Icons.Default.LocalOffer
            "calendar_month", "booking", "calendar" -> Icons.Default.CalendarMonth
            "home_repair_service", "homeservice" -> Icons.Default.HomeRepairService
            "photo_library", "gallery" -> Icons.Default.PhotoLibrary
            "place", "locations", "branch", "map" -> Icons.Default.Place
            "info", "about" -> Icons.Default.Info
            "call", "contact" -> Icons.Default.Call
            "settings" -> Icons.Default.Settings
            "share" -> Icons.Default.Share
            "star", "rate" -> Icons.Default.Star
            "description", "terms" -> Icons.Default.Description
            "privacy_tip", "privacy" -> Icons.Default.PrivacyTip
            else -> Icons.Default.Circle
        }
    }
}
