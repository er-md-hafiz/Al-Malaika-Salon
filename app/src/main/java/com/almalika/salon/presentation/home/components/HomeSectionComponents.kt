package com.almalika.salon.presentation.home.components

import android.os.SystemClock
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.almalika.salon.core.action.AppAction
import com.almalika.salon.domain.model.HomeBanner
import com.almalika.salon.domain.model.HomeSection
import com.almalika.salon.domain.model.SalonMasterConfig
import com.almalika.salon.presentation.theme.DefaultGoldPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroBannerSection(
    section: HomeSection,
    onAction: (AppAction) -> Unit
) {
    val eligibleBanners = remember(section.banners, section.imageUrl) {
        val list = section.banners.filter { it.isAvailable && it.imageUrl.isNotBlank() }
        if (list.isNotEmpty()) {
            list
        } else if (section.imageUrl.isNotBlank()) {
            listOf(
                HomeBanner(
                    id = "fallback",
                    imageUrl = section.imageUrl,
                    title = section.title.ifBlank { "Royal Grooming Experience" },
                    subtitle = section.subtitle,
                    actionRoute = section.actionRoute.ifBlank { "services" }
                )
            )
        } else {
            emptyList()
        }
    }

    if (eligibleBanners.isEmpty()) {
        return
    }

    val bannerCount = eligibleBanners.size
    val isLooping = bannerCount > 1
    val virtualPageCount = if (isLooping) bannerCount * 1000 else 1
    val initialPage = if (isLooping) (virtualPageCount / 2) - ((virtualPageCount / 2) % bannerCount) else 0

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { virtualPageCount }
    )
    val coroutineScope = rememberCoroutineScope()

    // Lifecycle observer to pause auto-advancement when paused/backgrounded
    val lifecycleOwner = LocalLifecycleOwner.current
    var isResumed by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isResumed = (event == Lifecycle.Event.ON_RESUME)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val autoSlideIntervalMs = if (section.autoSlideIntervalMs > 0L) section.autoSlideIntervalMs else 4000L

    // Safe auto-advance loop every 4s
    LaunchedEffect(pagerState, bannerCount, isResumed, autoSlideIntervalMs) {
        if (bannerCount > 1 && isResumed) {
            while (isActive) {
                delay(autoSlideIntervalMs)
                if (isResumed && !pagerState.isScrollInProgress) {
                    pagerState.animateScrollToPage(
                        page = pagerState.currentPage + 1,
                        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                    )
                }
            }
        }
    }

    // Debounced banner tap handler to prevent duplicate navigation
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val onBannerClick: (HomeBanner) -> Unit = { banner ->
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTime >= 500L) {
            lastClickTime = now
            val targetRoute = if (banner.actionRoute.isNotBlank()) {
                banner.actionRoute
            } else {
                section.actionRoute.ifBlank { "services" }
            }
            val appAction = when (targetRoute.lowercase().trim()) {
                "services" -> AppAction.Services
                "offers" -> AppAction.Offers
                "booking" -> AppAction.Booking
                "gallery" -> AppAction.Gallery
                "locations", "location" -> AppAction.Locations
                "about" -> AppAction.About
                "contact" -> AppAction.Contact
                "profile" -> AppAction.Profile
                "settings" -> AppAction.Settings
                "reviews" -> AppAction.Reviews
                else -> AppAction.Services
            }
            onAction(appAction)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp)),
            key = { page ->
                val actualIndex = page % bannerCount
                eligibleBanners[actualIndex].id.ifBlank { page.toString() }
            }
        ) { page ->
            val banner = eligibleBanners[page % bannerCount]
            BannerItemCard(
                banner = banner,
                onClick = { onBannerClick(banner) }
            )
        }

        // Indicators for multi-banner carousels
        if (bannerCount > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val activeIndex = pagerState.currentPage % bannerCount
                repeat(bannerCount) { index ->
                    val isSelected = activeIndex == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 22.dp else 6.dp,
                        animationSpec = tween(durationMillis = 300),
                        label = "indicator_width"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.45f)
                            )
                            .clickable {
                                coroutineScope.launch {
                                    val current = pagerState.currentPage
                                    val mod = current % bannerCount
                                    pagerState.animateScrollToPage(current + (index - mod))
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun BannerItemCard(
    banner: HomeBanner,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(banner.imageUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = banner.title.ifBlank { "Salon Banner" },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Scrim gradient for optimal legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.88f)
                            ),
                            startY = 40f
                        )
                    )
            )

            // Category tag
            if (banner.category.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = banner.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Title and Subtitle positioned safely away from bottom-right indicators
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp, end = 90.dp)
            ) {
                Text(
                    text = banner.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    ),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (banner.subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = banner.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onAction: (AppAction) -> Unit,
    showHomeService: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuickActionItem("Haircut", Icons.Default.ContentCut) { onAction(AppAction.Services) }
        QuickActionItem("Offers", Icons.Default.LocalOffer) { onAction(AppAction.Offers) }
        QuickActionItem("Book", Icons.Default.CalendarMonth) { onAction(AppAction.Booking) }
        if (showHomeService) {
            QuickActionItem("Home Service", Icons.Default.HomeRepairService) { onAction(AppAction.HomeService) }
        }
        QuickActionItem("Gallery", Icons.Default.PhotoLibrary) { onAction(AppAction.Gallery) }
    }
}

@Composable
fun QuickActionItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = "View All",
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun AboutSectionView(
    master: SalonMasterConfig,
    onAction: (AppAction) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = master.salon.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            if (master.salon.tagline.isNotBlank()) {
                Text(
                    text = master.salon.tagline,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (master.salon.aboutText.isNotBlank()) master.salon.aboutText else "Alfalah Colony, Pugmil, Hazaribagh, Jharkhand 825319",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { onAction(AppAction.Call(master.contact.phone.ifBlank { master.salon.phone })) },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call")
                }
                Button(
                    onClick = { onAction(AppAction.WhatsApp(master.whatsapp.number.ifBlank { master.salon.whatsapp })) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("WhatsApp", color = Color.White)
                }
            }
        }
    }
}
