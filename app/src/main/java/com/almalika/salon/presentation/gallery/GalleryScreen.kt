package com.almalika.salon.presentation.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.almalika.salon.core.action.AppAction
import com.almalika.salon.core.result.UiState
import com.almalika.salon.domain.model.GalleryImage
import com.almalika.salon.presentation.common.EmptyView
import com.almalika.salon.presentation.common.ErrorRetryView
import com.almalika.salon.presentation.common.ShimmerCard

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onAction: (AppAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var activeImageForPreview by remember { mutableStateOf<GalleryImage?>(null) }

    val categories = listOf(
        Pair("all", "All Works"),
        Pair("hair", "Hair"),
        Pair("bridal", "Bridal"),
        Pair("interior", "Salon Studio")
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Categories Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (catId, catName) ->
                FilterChip(
                    selected = selectedCategory == catId,
                    onClick = { viewModel.selectCategory(catId) },
                    label = { Text(catName) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        when (val state = uiState) {
            is UiState.Loading -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(6) {
                        ShimmerCard(height = 140)
                    }
                }
            }

            is UiState.Error -> {
                ErrorRetryView(
                    message = state.message,
                    onRetry = { viewModel.selectCategory(selectedCategory) }
                )
            }

            is UiState.Success -> {
                val images = state.data
                if (images.isEmpty()) {
                    EmptyView(
                        title = "No gallery images found",
                        subtitle = "Select another category to view salon transformations."
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(images, key = { it.id }) { image ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clickable { activeImageForPreview = image },
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
                    }
                }
            }
        }
    }

    // Full Screen Zoom/Preview Dialog
    if (activeImageForPreview != null) {
        val image = activeImageForPreview!!
        Dialog(onDismissRequest = { activeImageForPreview = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    AsyncImage(
                        model = image.imageUrl,
                        contentDescription = image.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (image.title.isNotBlank()) {
                        Text(
                            text = image.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (image.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = image.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { activeImageForPreview = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
