package com.almalika.salon.presentation.franchise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.almalika.salon.core.action.AppAction
import com.almalika.salon.domain.model.FranchiseConfig
import com.almalika.salon.domain.model.SalonMasterConfig

@Composable
fun FranchiseScreen(
    master: SalonMasterConfig,
    onAction: (AppAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val franchise: FranchiseConfig? = master.franchise

    if (franchise == null || !franchise.enabled) {
        // Safe Empty State when franchise data source is missing or disabled
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Franchise Opportunities Coming Soon",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Al-Malaika Salon expansion and franchise details will be announced shortly. Thank you for your interest in partnering with us.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (franchise.bannerImage.isNotBlank()) {
            AsyncImage(
                model = franchise.bannerImage,
                contentDescription = franchise.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = franchise.title.ifBlank { "Franchise Opportunities" },
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        if (franchise.subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = franchise.subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (franchise.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = franchise.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (franchise.website.isNotBlank() || franchise.whatsApp.isNotBlank()) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (franchise.website.isNotBlank()) {
                        onAction(AppAction.OpenUrl(franchise.website))
                    } else {
                        onAction(AppAction.WhatsApp(franchise.whatsApp, "Hello, I am interested in Al-Malaika Salon franchise."))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apply for Franchise")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

