package com.almalika.salon.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
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
    when (section.type) {
        SectionType.HERO -> {
            HeroBannerSection(section = section, onAction = onAction)
        }

        SectionType.QUICK_ACTIONS -> {
            QuickActionsSection(onAction = onAction)
        }

        SectionType.OFFERS -> {
            if (offers.isNotEmpty()) {
                SectionHeader(
                    title = section.title.ifBlank { "Exclusive Offers" },
                    subtitle = section.subtitle.ifBlank { "Limited period deals" },
                    actionText = "All Offers",
                    onActionClick = { onAction(AppAction.Offers) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(offers.take(section.itemsToShow)) { offer ->
                        HomeOfferCard(offer = offer, onClick = { onAction(AppAction.SelectOffer(offer.id)) })
                    }
                }
            }
        }

        SectionType.SERVICES -> {
            val flatServices = services.flatMap { it.services }
            if (flatServices.isNotEmpty()) {
                SectionHeader(
                    title = section.title.ifBlank { "Popular Services" },
                    subtitle = section.subtitle.ifBlank { "Styling, care & aesthetics" },
                    actionText = "All Services",
                    onActionClick = { onAction(AppAction.Services) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(flatServices.take(section.itemsToShow)) { service ->
                        HomeServiceCard(service = service, onClick = { onAction(AppAction.SelectService(service.id)) })
                    }
                }
            }
        }

        SectionType.GALLERY -> {
            if (galleryImages.isNotEmpty()) {
                SectionHeader(
                    title = section.title.ifBlank { "Transformations" },
                    subtitle = section.subtitle.ifBlank { "Client styling highlights" },
                    actionText = "View Gallery",
                    onActionClick = { onAction(AppAction.Gallery) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(galleryImages.take(section.itemsToShow)) { img ->
                        HomeGalleryCard(image = img, onClick = { onAction(AppAction.OpenGalleryImage(img.id)) })
                    }
                }
            }
        }

        SectionType.LOCATIONS -> {
            if (branches.isNotEmpty()) {
                SectionHeader(
                    title = section.title.ifBlank { "Visit Our Branches" },
                    subtitle = section.subtitle.ifBlank { "Convenient locations in town" },
                    actionText = "All Locations",
                    onActionClick = { onAction(AppAction.Locations) }
                )
                branches.take(2).forEach { branch ->
                    HomeBranchCard(branch = branch, onAction = onAction)
                }
            }
        }

        SectionType.ABOUT -> {
            AboutSectionView(master = master, onAction = onAction)
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

        SectionType.BANNER -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                AsyncImage(
                    model = section.imageUrl,
                    contentDescription = section.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        SectionType.SPACER -> {
            Spacer(modifier = Modifier.height(16.dp))
        }

        SectionType.UNKNOWN -> {}
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
                Text(
                    text = "₹${service.price.toInt()}",
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
fun HomeBranchCard(branch: Branch, onAction: (AppAction) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = branch.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(text = branch.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Timings: ${branch.timings.ifBlank { "9:00 AM - 9:00 PM" }}", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(10.dp))
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
