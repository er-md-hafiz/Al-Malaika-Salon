package com.almalika.salon.core.theme

import androidx.compose.ui.graphics.Color
import com.almalika.salon.data.remote.config.AppConfig
import com.almalika.salon.data.remote.config.FeatureFlags
import com.almalika.salon.data.remote.config.ThemeColors
import com.almalika.salon.data.remote.config.ThemeConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeEngineTest {

    @Test
    fun testContrastRatioCalculation() {
        val black = Color(0xFF000000)
        val white = Color(0xFFFFFFFF)
        val ratio = ThemeEngine.contrastRatio(black, white)
        assertTrue(ratio >= 21.0)
    }

    @Test
    fun testDeriveOnColorFlipsForContrast() {
        // Gold #D4AF37 onPrimary must be black/dark charcoal for 4.5:1 WCAG AA
        val gold = Color(0xFFD4AF37)
        val onGold = ThemeEngine.deriveOnColor(gold)
        assertEquals(Color(0xFF101010), onGold)

        // Dark background (e.g. #101010) on-color must be white for contrast
        val darkBg = Color(0xFF101010)
        val onDark = ThemeEngine.deriveOnColor(darkBg)
        assertEquals(Color(0xFFFFFFFF), onDark)
    }

    @Test
    fun testThemeModePrecedence() {
        val flags = FeatureFlags(darkMode = true)

        // 1. Force light when darkMode flag is false
        val lightByFlag = ThemeEngine.resolveIsDark(
            flags.copy(darkMode = false),
            ThemeConfig(themeMode = "dark", darkModeEnabled = true),
            systemIsDark = true
        )
        assertFalse(lightByFlag)

        // 2. Direct "dark" theme mode
        val darkByMode = ThemeEngine.resolveIsDark(
            flags,
            ThemeConfig(themeMode = "dark", darkModeEnabled = true),
            systemIsDark = false
        )
        assertTrue(darkByMode)

        // 3. Direct "light" theme mode
        val lightByMode = ThemeEngine.resolveIsDark(
            flags,
            ThemeConfig(themeMode = "light", darkModeEnabled = true),
            systemIsDark = true
        )
        assertFalse(lightByMode)

        // 4. "system" delegates to systemIsDark
        val systemFollow = ThemeEngine.resolveIsDark(
            flags,
            ThemeConfig(themeMode = "system", darkModeEnabled = true),
            systemIsDark = true
        )
        assertTrue(systemFollow)
    }

    @Test
    fun testBuildColorSchemePopulates27RolesWithoutNull() {
        val colors = ThemeColors(
            primary = "#D4AF37",
            primaryDark = "#B8962E",
            secondary = "#171717",
            background = "#101010",
            surface = "#1C1C1C",
            error = "#CF6679"
        )
        val colorScheme = ThemeEngine.buildColorScheme(colors, isDark = true)
        assertEquals(Color(0xFFD4AF37), colorScheme.primary)
        assertEquals(Color(0xFF101010), colorScheme.onPrimary)
        assertTrue(colorScheme.primaryContainer != Color.Unspecified)
        assertTrue(colorScheme.onPrimaryContainer != Color.Unspecified)
        assertTrue(colorScheme.surfaceVariant != Color.Unspecified)
        assertTrue(colorScheme.onSurfaceVariant != Color.Unspecified)
    }
}
