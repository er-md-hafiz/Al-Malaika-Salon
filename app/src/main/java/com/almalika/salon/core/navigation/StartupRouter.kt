package com.almalika.salon.core.navigation

import com.almalika.salon.data.remote.config.FeatureFlags
import com.almalika.salon.data.remote.config.RemoteConfig
import com.almalika.salon.data.remote.config.ScreenConfig

data class AppNavigationState(
    val hasCompletedOnboarding: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentAppVersion: String = "1.0.0"
)

object StartupRouter {

    /**
     * StartupRouter branch order (R1 Fix):
     * 1. maintenanceMode -> "maintenance"
     * 2. forceUpdate || currentAppVersion < minimumAppVersion -> "force_update"
     * 3. showOnboardingOnFirstLaunch && !hasCompletedOnboarding -> "onboarding"
     * 4. requireLogin && !isAuthenticated && !allowGuestBrowsing -> "login"
     * 5. Only if no gate fired: splashScreen.nextDestination
     * 6. defaultDestination
     */
    fun resolve(config: RemoteConfig, state: AppNavigationState): String {
        // 1. Maintenance gate
        if (config.appConfig.maintenanceMode) {
            return "maintenance"
        }

        // 2. Force update gate
        if (config.appConfig.forceUpdate || isVersionOutdated(state.currentAppVersion, config.appConfig.minimumAppVersion)) {
            return "force_update"
        }

        // 3. Onboarding gate (R1 Fix: never trust splashScreen.nextDestination if onboarding pending)
        if (config.startup.showOnboardingOnFirstLaunch && !state.hasCompletedOnboarding) {
            return "onboarding"
        }

        // 4. Login gate
        if (config.startup.requireLogin && !state.isAuthenticated && !config.startup.allowGuestBrowsing) {
            return "login"
        }

        // 5. Next destination if splash allowed
        val next = config.splashScreen?.nextDestination
        if (!next.isNullOrBlank() && next != "splash" && next != "startup_router") {
            return next
        }

        // 6. Default destination fallback
        return config.startup.defaultDestination.ifBlank { "home" }
    }

    private fun isVersionOutdated(current: String, minimum: String): Boolean {
        if (minimum.isBlank() || minimum == "0.0.0") return false
        val currParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val minParts = minimum.split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(currParts.size, minParts.size)) {
            val c = currParts.getOrElse(i) { 0 }
            val m = minParts.getOrElse(i) { 0 }
            if (c < m) return true
            if (c > m) return false
        }
        return false
    }
}

object FeatureFlagsAggregator {

    /**
     * R5: Composite screen enabled resolver:
     * isScreenEnabled(id) = screens[id].enabled AND mappedFeatureFlag(id)
     */
    fun isScreenEnabled(screenId: String, screens: List<ScreenConfig>, flags: FeatureFlags): Boolean {
        val screen = screens.find { it.id == screenId }
        val isScreenFlagEnabled = screen?.enabled ?: true

        val isFeatureFlagEnabled = when (screenId) {
            "home" -> true
            "services" -> flags.quickServices || flags.popularServices
            "price_list" -> flags.priceList
            "gallery" -> flags.gallery
            "offers" -> flags.offers
            "reviews" -> flags.reviews
            "booking" -> flags.homeServiceBooking
            "franchise" -> flags.franchise
            "onboarding" -> flags.onboarding
            "login" -> flags.login
            else -> true
        }

        return isScreenFlagEnabled && isFeatureFlagEnabled
    }
}

object RouteRegistry {
    // Internal pseudo-routes that MUST NOT be exposed in drawer / bottom nav (R6)
    val InternalRoutes = setOf("splash", "onboarding", "startup_router", "maintenance", "force_update")

    fun getNavigableScreens(config: RemoteConfig): List<ScreenConfig> {
        return config.screens.filter { screen ->
            screen.enabled &&
            !InternalRoutes.contains(screen.id) &&
            !InternalRoutes.contains(screen.type) &&
            FeatureFlagsAggregator.isScreenEnabled(screen.id, config.screens, config.featureFlags)
        }
    }
}

