package com.almalika.salon.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.almalika.salon.core.action.AppAction
import com.almalika.salon.core.result.UiState
import com.almalika.salon.domain.model.*
import com.almalika.salon.presentation.common.ErrorRetryView
import com.almalika.salon.presentation.common.ShimmerCard
import com.almalika.salon.presentation.home.components.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    masterConfig: SalonMasterConfig,
    services: List<ServiceCategory>,
    offers: List<Offer>,
    galleryImages: List<GalleryImage>,
    branches: List<Branch>,
    onAction: (AppAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is UiState.Loading -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ShimmerCard(height = 200)
                ShimmerCard(height = 60)
                ShimmerCard(height = 140)
            }
        }

        is UiState.Error -> {
            ErrorRetryView(
                message = state.message,
                onRetry = { viewModel.loadHomeData() },
                modifier = modifier
            )
        }

        is UiState.Success -> {
            val homeConfig = state.data
            val activeSections = homeConfig.sections
                .filter { it.enabled }
                .sortedBy { it.order }

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(activeSections, key = { it.id }) { section ->
                    DynamicSectionRenderer(
                        section = section,
                        master = masterConfig,
                        services = services,
                        offers = offers,
                        galleryImages = galleryImages,
                        branches = branches,
                        onAction = onAction
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicSectionRenderer(
    section: HomeSection,
    master: SalonMasterConfig,
    services: List<ServiceCategory>,
    offers: List<Offer>,
    galleryImages: List<GalleryImage>,
    branches: List<Branch>,
    onAction: (AppAction) -> Unit
) {
    fun resolveDestinationAction(dest: String?): AppAction {
        return when (dest?.lowercase()?.trim()) {
            "services" -> AppAction.Services
            "offers" -> AppAction.Offers
            "reviews" -> AppAction.Reviews
            "gallery" -> AppAction.Gallery
            "locations", "location" -> AppAction.Locations
            "booking" -> AppAction.Booking
            "home_service", "homeservice" -> AppAction.HomeService
            "franchise" -> AppAction.Franchise
            "about" -> AppAction.About
            "contact" -> AppAction.Contact
            "settings" -> AppAction.Settings
            "profile" -> AppAction.Profile
            "login" -> AppAction.Login
            "register" -> AppAction.Register
            "terms" -> AppAction.Terms
            "privacy" -> AppAction.Privacy
            else -> AppAction.Services
        }
    }

    when (section.type) {
        SectionType.HERO -> {
            val isHomeBannersEnabled = master.featureFlags["homeBanners"] ?: true
            if (isHomeBannersEnabled && section.enabled) {
                HeroBannerSection(section = section, onAction = onAction)
            }
        }

        SectionType.QUICK_ACTIONS -> {
            val isQuickServicesEnabled = master.featureFlags["quickServices"] ?: true
            if (isQuickServicesEnabled) {
                val flatServices = services.flatMap { it.services }
                val targetCount = section.itemsToShow.coerceAtLeast(1)
                val visibleQuickServices = flatServices.take(targetCount)
                if (visibleQuickServices.isNotEmpty() || !section.hideWhenEmpty) {
                    val viewAllAction = if (section.showViewAll) {
                        { onAction(resolveDestinationAction(section.viewAllDestination ?: "services")) }
                    } else null

                    SectionHeader(
                        title = section.title.ifBlank { "Quick Services" },
                        subtitle = section.subtitle.ifBlank { null },
                        actionText = if (section.showViewAll) "View All" else null,
                        onActionClick = viewAllAction
                    )
                    HomeServiceGrid(
                        services = visibleQuickServices,
                        onServiceClick = { onAction(AppAction.SelectService(it.id)) }
                    )
                }
            }
        }

        SectionType.SERVICES -> {
            val isPopularServicesEnabled = master.featureFlags["popularServices"] ?: true
            if (isPopularServicesEnabled) {
                val flatServices = services.flatMap { it.services }
                val targetCount = section.itemsToShow.coerceAtLeast(1)
                val visibleServices = flatServices.take(targetCount)
                if (visibleServices.isNotEmpty() || !section.hideWhenEmpty) {
                    val viewAllAction = if (section.showViewAll) {
                        { onAction(resolveDestinationAction(section.viewAllDestination ?: "services")) }
                    } else null

                    SectionHeader(
                        title = section.title.ifBlank { "Popular Services" },
                        subtitle = section.subtitle.ifBlank { "Styling, care & aesthetics" },
                        actionText = if (section.showViewAll) "All Services" else null,
                        onActionClick = viewAllAction
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(visibleServices, key = { it.id }) { service ->
                            HomeServiceCard(service = service, onClick = { onAction(AppAction.SelectService(service.id)) })
                        }
                    }
                }
            }
        }

        SectionType.FEATURE_GRID -> {
            val whyChooseUsItems = master.whyChooseUs
            if (whyChooseUsItems.isNotEmpty() || !section.hideWhenEmpty) {
                SectionHeader(
                    title = section.title.ifBlank { "Why Choose Us" },
                    subtitle = section.subtitle.ifBlank { null },
                    actionText = null,
                    onActionClick = null
                )
                HomeFeatureGrid(items = whyChooseUsItems)
            }
        }

        SectionType.GALLERY -> {
            val isGalleryEnabled = master.featureFlags["gallery"] ?: true
            if (isGalleryEnabled) {
                val targetCount = section.itemsToShow.coerceAtLeast(1)
                val visibleGallery = galleryImages.take(targetCount)
                if (visibleGallery.isNotEmpty() || !section.hideWhenEmpty) {
                    val viewAllAction = if (section.showViewAll) {
                        { onAction(resolveDestinationAction(section.viewAllDestination ?: "gallery")) }
                    } else null

                    SectionHeader(
                        title = section.title.ifBlank { "Our Gallery" },
                        subtitle = section.subtitle.ifBlank { "Client styling highlights" },
                        actionText = if (section.showViewAll) "View Gallery" else null,
                        onActionClick = viewAllAction
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(visibleGallery, key = { it.id }) { img ->
                            HomeGalleryCard(image = img, onClick = { onAction(AppAction.OpenGalleryImage(img.id)) })
                        }
                    }
                }
            }
        }

        SectionType.OFFERS -> {
            val isOffersEnabled = master.featureFlags["offers"] ?: true
            if (isOffersEnabled) {
                val targetCount = section.itemsToShow.coerceAtLeast(1)
                val visibleOffers = offers.take(targetCount)
                if (visibleOffers.isNotEmpty() || !section.hideWhenEmpty) {
                    val viewAllAction = if (section.showViewAll) {
                        { onAction(resolveDestinationAction(section.viewAllDestination ?: "offers")) }
                    } else null

                    SectionHeader(
                        title = section.title.ifBlank { "Special Offers" },
                        subtitle = section.subtitle.ifBlank { "Limited period deals" },
                        actionText = if (section.showViewAll) "All Offers" else null,
                        onActionClick = viewAllAction
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(visibleOffers, key = { it.id }) { offer ->
                            HomeOfferCard(offer = offer, onClick = { onAction(AppAction.SelectOffer(offer.id)) })
                        }
                    }
                }
            }
        }

        SectionType.REVIEWS -> {
            val isReviewsEnabled = master.featureFlags["reviews"] ?: true
            if (isReviewsEnabled) {
                val viewAllAction = if (section.showViewAll) {
                    { onAction(resolveDestinationAction(section.viewAllDestination ?: "reviews")) }
                } else null

                SectionHeader(
                    title = section.title.ifBlank { "Customer Reviews" },
                    subtitle = section.subtitle.ifBlank { "Real client styling experiences" },
                    actionText = if (section.showViewAll) "All Reviews" else null,
                    onActionClick = viewAllAction
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onAction(resolveDestinationAction(section.actionRoute.ifBlank { "reviews" })) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = master.salon.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Royal Grooming & Client Satisfaction",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { onAction(resolveDestinationAction(section.actionRoute.ifBlank { "reviews" })) }) {
                            Text("View")
                        }
                    }
                }
            }
        }

        SectionType.LOCATIONS -> {
            if (branches.isNotEmpty() || !section.hideWhenEmpty) {
                SectionHeader(
                    title = section.title.ifBlank { "Visit Our Salon" },
                    subtitle = section.subtitle.ifBlank { "Convenient grooming studio" },
                    actionText = if (section.showViewAll) "All Locations" else null,
                    onActionClick = if (section.showViewAll) {
                        { onAction(resolveDestinationAction(section.viewAllDestination ?: "locations")) }
                    } else null
                )
                branches.take(section.itemsToShow.coerceAtLeast(1)).forEach { branch ->
                    HomeBranchCard(
                        branch = branch,
                        showAddress = section.showAddress,
                        showOpeningHours = section.showOpeningHours,
                        onAction = onAction
                    )
                }
            }
        }

        SectionType.ABOUT -> {
            if (master.whyChooseUs.isNotEmpty()) {
                SectionHeader(
                    title = section.title.ifBlank { "Why Choose Us" },
                    subtitle = section.subtitle.ifBlank { "Experience premier grooming excellence" }
                )
                HomeFeatureGrid(items = master.whyChooseUs)
            } else {
                AboutSectionView(master = master, onAction = onAction)
            }
        }

        SectionType.BANNER, SectionType.FRANCHISE_BANNER -> {
            if (section.id == "home_franchise" || section.dataSource == "franchise" || section.type == SectionType.FRANCHISE_BANNER) {
                val isFranchiseEnabled = (master.featureFlags["franchise"] ?: true) && (master.franchise?.enabled ?: false)
                if (isFranchiseEnabled || !section.hideWhenDisabled) {
                    HomeFranchiseBannerCard(
                        section = section,
                        franchise = master.franchise,
                        onAction = { onAction(resolveDestinationAction(section.actionRoute.ifBlank { "franchise" })) }
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    if (section.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = section.imageUrl,
                            contentDescription = section.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = section.title.ifBlank { "Special Announcement" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (section.subtitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = section.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (section.actionRoute.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                FilledTonalButton(
                                    onClick = { onAction(resolveDestinationAction(section.actionRoute)) }
                                ) {
                                    Text(section.actionText.ifBlank { "Learn More" })
                                }
                            }
                        }
                    }
                }
            }
        }

        SectionType.CONTACT -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = section.title.ifBlank { "Need Assistance?" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Talk to our salon consultants or reach us on WhatsApp for custom styling packages.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onAction(AppAction.Contact) },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Contact Salon")
                    }
                }
            }
        }

        SectionType.SPACER -> {
            Spacer(modifier = Modifier.height(16.dp))
        }

        SectionType.UNKNOWN -> {}
    }
}

@Composable
fun HomeServiceGrid(
    services: List<SalonService>,
    onServiceClick: (SalonService) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = services.chunked(2)
        rows.forEach { rowServices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowServices.forEach { service ->
                    Box(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onServiceClick(service) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (service.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = service.imageUrl,
                                        contentDescription = service.name,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = service.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val formattedPrice = com.almalika.salon.core.currency.CurrencyFormatter.format(service.price)
                                    Text(
                                        text = formattedPrice,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
                if (rowServices.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun HomeFeatureGrid(
    items: List<WhyChooseUsItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = items.chunked(2)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    Card(
                        modifier = Modifier
                            .weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val iconVector = com.almalika.salon.presentation.navigation.IconResolver.resolve(item.icon)
                            Icon(
                                imageVector = iconVector,
                                contentDescription = item.title,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (item.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun HomeFranchiseBannerCard(
    section: HomeSection,
    franchise: FranchiseConfig?,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onAction),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column {
            val bannerImg = section.imageUrl.ifBlank { franchise?.bannerImage.orEmpty() }
            if (bannerImg.isNotBlank()) {
                AsyncImage(
                    model = bannerImg,
                    contentDescription = section.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = section.title.ifBlank { franchise?.title.orEmpty().ifBlank { "Franchise Opportunities" } },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                val subtitleText = section.subtitle.ifBlank { franchise?.subtitle.orEmpty().ifBlank { franchise?.description.orEmpty() } }
                if (subtitleText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(section.actionText.ifBlank { "Explore Franchise" })
                }
            }
        }
    }
}

@Composable
fun HomeOfferCard(offer: Offer, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            AsyncImage(
                model = offer.imageUrl,
                contentDescription = offer.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = offer.discount,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = offer.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun HomeServiceCard(service: SalonService, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = service.imageUrl,
                contentDescription = service.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val formattedPrice = com.almalika.salon.core.currency.CurrencyFormatter.format(service.price)
                Text(
                    text = formattedPrice,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun HomeGalleryCard(image: GalleryImage, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp)
    ) {
        AsyncImage(
            model = image.thumbnailUrl.ifBlank { image.imageUrl },
            contentDescription = image.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun HomeBranchCard(
    branch: Branch,
    showAddress: Boolean = true,
    showOpeningHours: Boolean = true,
    onAction: (AppAction) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = branch.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            if (showAddress && branch.address.isNotBlank()) {
                Text(text = branch.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
            }
            if (showOpeningHours) {
                Text(text = "Timings: ${branch.timings.ifBlank { "9:00 AM - 9:00 PM" }}", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(10.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onAction(AppAction.OpenMap(branch.googleMapsUrl, branch.latitude, branch.longitude)) },
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Directions", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = { onAction(AppAction.Call(branch.phone)) },
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Call Branch", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
