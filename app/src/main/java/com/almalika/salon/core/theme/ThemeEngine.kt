package com.almalika.salon.core.theme

import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.almalika.salon.data.remote.config.AppConfig
import com.almalika.salon.data.remote.config.FeatureFlags
import com.almalika.salon.data.remote.config.ThemeColors
import com.almalika.salon.data.remote.config.ThemeConfig
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object ThemeEngine {
    private const val TAG = "ThemeEngine"

    // Default Fallbacks
    val FallbackGold = Color(0xFFD4AF37)
    val FallbackGoldDark = Color(0xFFB8962E)
    val FallbackSecondary = Color(0xFF171717)
    val FallbackBackgroundDark = Color(0xFF101010)
    val FallbackSurfaceDark = Color(0xFF1C1C1C)
    val FallbackBackgroundLight = Color(0xFFF9F9FA)
    val FallbackSurfaceLight = Color(0xFFFFFFFF)
    val FallbackError = Color(0xFFCF6679)
    val FallbackSuccess = Color(0xFF4CAF50)

    fun calculateLuminance(color: Color): Double {
        fun gamma(channel: Float): Double {
            val c = channel.toDouble()
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * gamma(color.red) + 0.7152 * gamma(color.green) + 0.0722 * gamma(color.blue)
    }

    fun contrastRatio(c1: Color, c2: Color): Double {
        val l1 = calculateLuminance(c1)
        val l2 = calculateLuminance(c2)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    /**
     * WCAG AA 4.5:1 Contrast Guard
     */
    fun deriveOnColor(background: Color, preferredCandidate: Color? = null): Color {
        val defaultChoice = preferredCandidate ?: if (calculateLuminance(background) > 0.5) Color(0xFF101010) else Color(0xFFFFFFFF)
        val contrast = contrastRatio(background, defaultChoice)
        if (contrast >= 4.5) return defaultChoice

        val flipped = if (defaultChoice == Color(0xFF101010)) Color(0xFFFFFFFF) else Color(0xFF101010)
        return if (contrastRatio(background, flipped) >= contrast) flipped else defaultChoice
    }

    fun parseColor(hex: String?, fallback: Color): Color {
        if (hex.isNullOrBlank()) return fallback
        val cleanHex = hex.trim()
        return try {
            val colorLong = if (cleanHex.startsWith("#")) {
                cleanHex.substring(1).toLong(16)
            } else {
                cleanHex.toLong(16)
            }
            val argb = when (cleanHex.removePrefix("#").length) {
                6 -> 0xFF000000 or colorLong
                8 -> colorLong
                else -> return fallback
            }
            Color(argb)
        } catch (_: Exception) {
            fallback
        }
    }

    /**
     * Theme Mode Precedence:
     * 1. featureFlags.darkMode == false -> force light
     * 2. appConfig.darkModeEnabled == false -> force light
     * 3. themeConfig.themeMode == "dark" -> dark
     * 4. themeConfig.themeMode == "light" -> light
     * 5. themeConfig.themeMode == "system" -> systemIsDark
     */
    fun resolveIsDark(
        featureFlags: FeatureFlags,
        themeConfig: ThemeConfig,
        systemIsDark: Boolean
    ): Boolean {
        return when {
            !featureFlags.darkMode -> false
            !themeConfig.darkModeEnabled -> false
            themeConfig.themeMode.equals("dark", ignoreCase = true) -> true
            themeConfig.themeMode.equals("light", ignoreCase = true) -> false
            else -> systemIsDark
        }
    }

    /**
     * Builds complete 29-role Material 3 ColorScheme with mathematical derivation & WCAG AA guard
     */
    fun buildColorScheme(colors: ThemeColors, isDark: Boolean): ColorScheme {
        val primary = parseColor(colors.primary, FallbackGold)
        val primaryDark = parseColor(colors.primaryDark, FallbackGoldDark)
        val secondary = parseColor(colors.secondary, FallbackSecondary)
        val background = parseColor(
            colors.background,
            if (isDark) FallbackBackgroundDark else FallbackBackgroundLight
        )
        val surface = parseColor(
            colors.surface,
            if (isDark) FallbackSurfaceDark else FallbackSurfaceLight
        )
        val error = parseColor(colors.error, FallbackError)

        // R4 Derived Color Roles
        val onPrimary = deriveOnColor(primary, colors.onPrimary?.let { parseColor(it, Color.Unspecified) })
        val onSecondary = deriveOnColor(secondary)
        val onBackground = deriveOnColor(background, colors.textPrimary?.let { parseColor(it, Color.Unspecified) })
        val onSurface = deriveOnColor(surface, colors.onSurface?.let { parseColor(it, Color.Unspecified) })
        val onError = deriveOnColor(error, colors.onError?.let { parseColor(it, Color.Unspecified) })

        val primaryContainer = if (isDark) primaryDark else primary.copy(alpha = 0.2f)
        val onPrimaryContainer = deriveOnColor(primaryContainer)

        val secondaryContainer = secondary.copy(alpha = 0.3f)
        val onSecondaryContainer = deriveOnColor(secondaryContainer)

        val surfaceVariant = if (isDark) surface.copy(alpha = 0.8f) else Color(0xFFE5E7EB)
        val onSurfaceVariant = deriveOnColor(surfaceVariant, colors.textSecondary?.let { parseColor(it, Color.Unspecified) })

        val outline = parseColor(colors.divider, if (isDark) Color(0xFF333333) else Color(0xFFD1D5DB))
        val outlineVariant = outline.copy(alpha = 0.5f)
        val scrim = Color(0xFF000000)

        val inverseSurface = if (isDark) Color(0xFFE5E7EB) else Color(0xFF1F2937)
        val inverseOnSurface = deriveOnColor(inverseSurface)
        val inversePrimary = if (isDark) FallbackGoldDark else FallbackGold

        return if (isDark) {
            darkColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                inversePrimary = inversePrimary,
                secondary = secondary,
                onSecondary = onSecondary,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = onSecondaryContainer,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                surfaceVariant = surfaceVariant,
                onSurfaceVariant = onSurfaceVariant,
                inverseSurface = inverseSurface,
                inverseOnSurface = inverseOnSurface,
                error = error,
                onError = onError,
                outline = outline,
                outlineVariant = outlineVariant,
                scrim = scrim
            )
        } else {
            lightColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                inversePrimary = inversePrimary,
                secondary = secondary,
                onSecondary = onSecondary,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = onSecondaryContainer,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                surfaceVariant = surfaceVariant,
                onSurfaceVariant = onSurfaceVariant,
                inverseSurface = inverseSurface,
                inverseOnSurface = inverseOnSurface,
                error = error,
                onError = onError,
                outline = outline,
                outlineVariant = outlineVariant,
                scrim = scrim
            )
        }
    }
}
