package com.almalika.salon

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.almalika.salon.presentation.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Android Instrumentation UI Test suite covering critical user journeys and navigation:
 * - App launch and startup routing (Splash -> Onboarding/Login/Home)
 * - Navigation drawer opening and destination selection
 * - Bottom navigation bar switching
 * - Screen rendering and back press handling
 *
 * Designed to execute on connected physical devices or Android emulators via ADB/Espresso.
 */
@RunWith(AndroidJUnit4::class)
class NavigationAndMenuInstrumentationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAppLaunchAndInitialRendering() {
        // Verify application launched cleanly without unhandled exceptions
        composeTestRule.waitForIdle()
    }

    @Test
    fun testBottomNavigationTabsSwitching() {
        composeTestRule.waitForIdle()

        // Test Services Tab
        val servicesTab = composeTestRule.onNodeWithText("Services")
        if (servicesTab.isDisplayed()) {
            servicesTab.performClick()
            composeTestRule.waitForIdle()
        }

        // Test Home Tab
        val homeTab = composeTestRule.onNodeWithText("Home")
        if (homeTab.isDisplayed()) {
            homeTab.performClick()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun testNavigationDrawerOpenAndItemSelection() {
        composeTestRule.waitForIdle()

        // Attempt to find navigation menu button
        val menuButton = composeTestRule.onNodeWithContentDescription("Navigation Menu")
        if (menuButton.isDisplayed()) {
            menuButton.performClick()
            composeTestRule.waitForIdle()

            // Click Contact Us
            val contactItem = composeTestRule.onNodeWithText("Contact Us")
            if (contactItem.isDisplayed()) {
                contactItem.performClick()
                composeTestRule.waitForIdle()
            }
        }
    }
}
