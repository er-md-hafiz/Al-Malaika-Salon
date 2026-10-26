package com.almalika.salon.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import com.almalika.salon.domain.model.ThemeConfig

val GoldPrimary = Color(0xFFD4AF37)
val GoldSecondary = Color(0xFFA67C1E)
val CharcoalDark = Color(0xFF18181B)
val CharcoalSurface = Color(0xFF27272A)
val WarmBeige = Color(0xFFFBF8F2)

fun parseHexColor(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        Color(android.graphics.Color.parseColor(hex.trim()))
    } catch (_: Exception) {
        fallback
    }
}

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = GoldSecondary,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFE2C475),
    onSecondary = Color.Black,
    background = CharcoalDark,
    surface = CharcoalSurface,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF8C6D1F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E7C4),
    onPrimaryContainer = Color(0xFF2E2000),
    secondary = Color(0xFF6B5D3F),
    onSecondary = Color.White,
    background = WarmBeige,
    surface = Color.White,
    onBackground = Color(0xFF1E1C16),
    onSurface = Color(0xFF1E1C16)
)

@Composable
fun AlMalaikaSalonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeConfig: ThemeConfig? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        val primary = parseHexColor(themeConfig?.primaryColor, GoldPrimary)
        val secondary = parseHexColor(themeConfig?.secondaryColor, GoldSecondary)
        val background = parseHexColor(themeConfig?.backgroundColor, CharcoalDark)
        val surface = parseHexColor(themeConfig?.surfaceColor, CharcoalSurface)
        val onSurface = parseHexColor(themeConfig?.textColor, Color.White)
        darkColorScheme(
            primary = primary,
            onPrimary = Color.Black,
            primaryContainer = secondary,
            onPrimaryContainer = Color.White,
            secondary = Color(0xFFE2C475),
            onSecondary = Color.Black,
            background = background,
            surface = surface,
            onBackground = onSurface,
            onSurface = onSurface
        )
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
