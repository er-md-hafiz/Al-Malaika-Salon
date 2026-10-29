package com.almalika.salon.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.almalika.salon.domain.model.ThemeConfig
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object ThemeLuminance {

    /**
     * Calculates the relative luminance of a color per WCAG 2.1 specs.
     * https://www.w3.org/TR/WCAG21/#dfn-relative-luminance
     */
    fun calculateLuminance(color: Color): Double {
        fun gamma(channel: Float): Double {
            val c = channel.toDouble()
            return if (c <= 0.03928) {
                c / 12.92
            } else {
                ((c + 0.055) / 1.055).pow(2.4)
            }
        }

        val r = gamma(color.red)
        val g = gamma(color.green)
        val b = gamma(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    /**
     * Calculates the contrast ratio between two colors per WCAG 2.1.
     * https://www.w3.org/TR/WCAG21/#dfn-contrast-ratio
     */
    fun contrastRatio(c1: Color, c2: Color): Double {
        val l1 = calculateLuminance(c1)
        val l2 = calculateLuminance(c2)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    /**
     * Derives an on-color (e.g. onPrimary, onSurface, onError) ensuring WCAG AA contrast (>= 4.5:1).
     */
    fun deriveOnColor(background: Color): Color {
        val lum = calculateLuminance(background)
        val preferred = if (lum > 0.5) Color(0xFF101010) else Color(0xFFFFFFFF)
        val contrast = contrastRatio(background, preferred)
        return if (contrast >= 4.5) {
            preferred
        } else {
            // Flip if contrast is insufficient
            val inverted = if (preferred == Color(0xFF101010)) Color(0xFFFFFFFF) else Color(0xFF101010)
            if (contrastRatio(background, inverted) >= contrast) inverted else preferred
        }
    }
}

fun parseHexColor(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        Color(android.graphics.Color.parseColor(hex.trim()))
    } catch (_: Exception) {
        fallback
    }
}

val DefaultGoldPrimary = Color(0xFFD4AF37)
val DefaultGoldSecondary = Color(0xFFA67C1E)
val DefaultCharcoalDark = Color(0xFF101010)
val DefaultCharcoalSurface = Color(0xFF1C1C1C)
val DefaultTextPrimary = Color(0xFFFFFFFF)
val DefaultTextSecondary = Color(0xFFBDBDBD)
val DefaultDivider = Color(0xFF333333)
val DefaultError = Color(0xFFCF6679)
val DefaultSuccess = Color(0xFF4CAF50)

@Composable
fun AlMalaikaSalonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeConfig: ThemeConfig? = null,
    content: @Composable () -> Unit
) {
    val primary = parseHexColor(themeConfig?.primaryColor, DefaultGoldPrimary)
    val primaryDark = parseHexColor(themeConfig?.secondaryColor, DefaultGoldSecondary)

    // Day (Light) vs Night (Dark) mode palettes
    // When darkTheme is false (Day/Light mode), use crisp light background & surfaces
    val background = if (darkTheme) {
        parseHexColor(themeConfig?.backgroundColor, DefaultCharcoalDark)
    } else {
        Color(0xFFF9F9FA)
    }

    val surface = if (darkTheme) {
        parseHexColor(themeConfig?.surfaceColor, DefaultCharcoalSurface)
    } else {
        Color(0xFFFFFFFF)
    }

    val surfaceVariant = if (darkTheme) {
        Color(0xFF242426)
    } else {
        Color(0xFFF0F0F2)
    }

    val textPrimary = if (darkTheme) {
        parseHexColor(themeConfig?.textColor, DefaultTextPrimary)
    } else {
        Color(0xFF1A1A1A)
    }

    val divider = if (darkTheme) {
        parseHexColor(themeConfig?.colors?.divider, DefaultDivider)
    } else {
        Color(0xFFE2E2E6)
    }

    val errorColor = parseHexColor(themeConfig?.colors?.error, DefaultError)
    val successColor = parseHexColor(themeConfig?.colors?.success, DefaultSuccess)

    // Dynamic M3 Contrast Role Derivation via Luminance Math
    val onPrimary = themeConfig?.colors?.onPrimary?.let { parseHexColor(it, ThemeLuminance.deriveOnColor(primary)) }
        ?: ThemeLuminance.deriveOnColor(primary)
    val onSurface = if (darkTheme) {
        themeConfig?.colors?.onSurface?.let { parseHexColor(it, ThemeLuminance.deriveOnColor(surface)) } ?: textPrimary
    } else {
        Color(0xFF1A1A1A)
    }
    val onBackground = ThemeLuminance.deriveOnColor(background)
    val onError = themeConfig?.colors?.onError?.let { parseHexColor(it, ThemeLuminance.deriveOnColor(errorColor)) }
        ?: ThemeLuminance.deriveOnColor(errorColor)
    val onSurfaceVariant = if (darkTheme) {
        parseHexColor(themeConfig?.colors?.textSecondary, DefaultTextSecondary)
    } else {
        Color(0xFF6B6B70)
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryDark,
            onPrimaryContainer = onPrimary,
            secondary = primaryDark,
            onSecondary = onPrimary,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outlineVariant = divider,
            error = errorColor,
            onError = onError,
            tertiary = successColor
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryDark,
            onPrimaryContainer = onPrimary,
            secondary = primaryDark,
            onSecondary = onPrimary,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outlineVariant = divider,
            error = errorColor,
            onError = onError,
            tertiary = successColor
        )
    }

    val cornerRadius = (themeConfig?.cornerRadiusDp ?: 12).dp
    val shapes = Shapes(
        small = RoundedCornerShape((themeConfig?.buttonCornerRadiusDp ?: 10).dp),
        medium = RoundedCornerShape(cornerRadius),
        large = RoundedCornerShape(cornerRadius + 4.dp)
    )

    // Dynamic Status Bar Icon Luminance Control
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                val useSystem = themeConfig?.useSystemStatusBarIcons ?: false
                if (!useSystem) {
                    val bgLuminance = ThemeLuminance.calculateLuminance(background)
                    insetsController.isAppearanceLightStatusBars = bgLuminance > 0.5
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        shapes = shapes,
        content = content
    )
}
