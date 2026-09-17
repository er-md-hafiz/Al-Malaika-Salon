package com.almalika.salon.presentation.contact

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.almalika.salon.core.action.AppAction
import com.almalika.salon.domain.model.SalonMasterConfig

@Composable
fun ContactScreen(
    master: SalonMasterConfig,
    onAction: (AppAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val contact = master.contact

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Get in Touch",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "We're here to answer queries and schedule appointments.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (contact.phone.isNotBlank()) {
            item {
                ContactActionTile(
                    title = "Phone Call",
                    subtitle = contact.phone,
                    icon = Icons.Default.Call,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { onAction(AppAction.Call(contact.phone)) }
                )
            }
        }

        if (contact.whatsapp.isNotBlank()) {
            item {
                ContactActionTile(
                    title = "WhatsApp Chat",
                    subtitle = "Instant replies for booking and inquiries",
                    icon = Icons.Default.Chat,
                    tint = Color(0xFF25D366),
                    onClick = { onAction(AppAction.WhatsApp(contact.whatsapp)) }
                )
            }
        }

        if (contact.googleMaps.isNotBlank()) {
            item {
                ContactActionTile(
                    title = "Google Maps",
                    subtitle = master.salon.address,
                    icon = Icons.Default.Place,
                    tint = Color(0xFFEA4335),
                    onClick = { onAction(AppAction.OpenMap(contact.googleMaps)) }
                )
            }
        }

        if (contact.instagram.isNotBlank()) {
            item {
                ContactActionTile(
                    title = "Instagram",
                    subtitle = "Follow our recent haircuts & bridal styles",
                    icon = Icons.Default.PhotoCamera,
                    tint = Color(0xFFE1306C),
                    onClick = { onAction(AppAction.OpenUrl(contact.instagram, "Instagram")) }
                )
            }
        }

        if (contact.facebook.isNotBlank()) {
            item {
                ContactActionTile(
                    title = "Facebook",
                    subtitle = "Connect on our official page",
                    icon = Icons.Default.ThumbUp,
                    tint = Color(0xFF1877F2),
                    onClick = { onAction(AppAction.OpenUrl(contact.facebook, "Facebook")) }
                )
            }
        }

        if (contact.youtube.isNotBlank()) {
            item {
                ContactActionTile(
                    title = "YouTube",
                    subtitle = "Watch styling guides & video transformations",
                    icon = Icons.Default.VideoLibrary,
                    tint = Color(0xFFFF0000),
                    onClick = { onAction(AppAction.OpenUrl(contact.youtube, "YouTube")) }
                )
            }
        }
    }
}

@Composable
fun ContactActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tint.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = title, tint = tint)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
