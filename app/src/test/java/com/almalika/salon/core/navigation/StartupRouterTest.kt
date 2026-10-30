package com.almalika.salon.core.navigation

import com.almalika.salon.data.remote.config.AppConfig
import com.almalika.salon.data.remote.config.FeatureFlags
import com.almalika.salon.data.remote.config.RemoteConfig
import com.almalika.salon.data.remote.config.ScreenConfig
import com.almalika.salon.data.remote.config.SplashScreenConfig
import com.almalika.salon.data.remote.config.StartupConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupRouterTest {

    @Test
    fun testR1Mitigation_onboardingOnFirstLaunchOverridesSplashScreenNextHome() {
        val config = RemoteConfig(
            startup = StartupConfig(showOnboardingOnFirstLaunch = true),
            splashScreen = SplashScreenConfig(nextDestination = "home")
        )
        val state = AppNavigationState(hasCompletedOnboarding = false)
        val route = StartupRouter.resolve(config, state)
        assertEquals("onboarding", route)
    }

    @Test
    fun testMaintenanceGateHasHighestPriority() {
        val config = RemoteConfig(
            appConfig = AppConfig(maintenanceMode = true)
        )
        val state = AppNavigationState(hasCompletedOnboarding = false)
        val route = StartupRouter.resolve(config, state)
        assertEquals("maintenance", route)
    }

    @Test
    fun testForceUpdateGate() {
        val config = RemoteConfig(
            appConfig = AppConfig(forceUpdate = true)
        )
        val state = AppNavigationState()
        val route = StartupRouter.resolve(config, state)
        assertEquals("force_update", route)
    }

    @Test
    fun testR5CompositeFeatureFlagsResolver() {
        val screens = listOf(
            ScreenConfig(id = "reviews", enabled = true, type = "reviews")
        )
        val flags = FeatureFlags(reviews = false)
        val isEnabled = FeatureFlagsAggregator.isScreenEnabled("reviews", screens, flags)
        assertFalse(isEnabled)
    }

    @Test
    fun testR6InternalScreensExcludedFromNavigableScreens() {
        val screens = listOf(
            ScreenConfig(id = "splash", enabled = true, type = "splash"),
            ScreenConfig(id = "startup_router", enabled = true, type = "startup_router"),
            ScreenConfig(id = "home", enabled = true, type = "home"),
            ScreenConfig(id = "gallery", enabled = true, type = "gallery")
        )
        val config = RemoteConfig(screens = screens)
        val navigable = RouteRegistry.getNavigableScreens(config)
        assertEquals(2, navigable.size)
        assertTrue(navigable.none { it.id == "splash" || it.id == "startup_router" })
    }
}

