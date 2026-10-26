package com.almalika.salon.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.almalika.salon.domain.model.SplashScreenConfig
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    config: SplashScreenConfig,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    val bgColor = remember(config.backgroundColor) {
        try {
            Color(android.graphics.Color.parseColor(config.backgroundColor))
        } catch (_: Exception) {
            Color(0xFF121212)
        }
    }

    val textColor = remember(config.textColor) {
        try {
            Color(android.graphics.Color.parseColor(config.textColor))
        } catch (_: Exception) {
            Color(0xFFFFFFFF)
        }
    }

    val accentColor = remember(config.accentColor) {
        try {
            Color(android.graphics.Color.parseColor(config.accentColor))
        } catch (_: Exception) {
            Color(0xFFD4AF37)
        }
    }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
        val remaining = (config.minimumDurationMs - 800).coerceAtLeast(400)
        delay(remaining)
        onFinish()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            if (config.logoUrl.isNotBlank()) {
                AsyncImage(
                    model = config.logoUrl,
                    contentDescription = config.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = config.title.ifBlank { "Al-Malaika Salon" },
                color = accentColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            )

            if (config.tagline.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = config.tagline,
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
