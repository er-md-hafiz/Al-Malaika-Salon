package com.almalika.salon.presentation.settings

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.almalika.salon.core.action.AppAction
import com.almalika.salon.domain.model.SalonMasterConfig

@Composable
fun SettingsScreen(
    master: SalonMasterConfig,
    onAction: (AppAction) -> Unit,
    isDarkMode: Boolean = true,
    onToggleDarkMode: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Preferences & App Info",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            SettingsRow(
                title = "My Account & Profile",
                subtitle = "Manage your login, guest status, and salon profile",
                icon = Icons.Default.AccountCircle,
                onClick = { onAction(AppAction.Account) }
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = "Dark Mode",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Dark Mode", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Royal Al-Malaika dark aesthetic",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onToggleDarkMode
                    )
                }
            }
        }

        item {
            SettingsRow(
                title = "Client Reviews",
                subtitle = "Read verified styling feedback & testimonials",
                icon = Icons.Default.RateReview,
                onClick = { onAction(AppAction.Reviews) }
            )
        }

        item {
            SettingsRow(
                title = "About Us",
                subtitle = "Learn more about our legacy & salon styling",
                icon = Icons.Default.Info,
                onClick = { onAction(AppAction.About) }
            )
        }

        item {
            val shareMsg = master.profile?.shareMessage?.ifBlank { master.share.message } ?: master.share.message
            SettingsRow(
                title = "Share Application",
                subtitle = "Recommend Al-Malaika Salon to family & friends",
                icon = Icons.Default.Share,
                onClick = {
                    onAction(AppAction.ShareApp(shareMsg, master.share.playStoreUrl))
                }
            )
        }

        item {
            SettingsRow(
                title = "Rate on Google Play",
                subtitle = "Leave your salon feedback & review",
                icon = Icons.Default.Star,
                onClick = { onAction(AppAction.RateApp) }
            )
        }

        item {
            SettingsRow(
                title = "Terms & Conditions",
                subtitle = "Read salon policies and service terms",
                icon = Icons.Default.Description,
                onClick = { onAction(AppAction.Terms) }
            )
        }

        item {
            SettingsRow(
                title = "Privacy Policy",
                subtitle = "How we protect and handle client privacy",
                icon = Icons.Default.PrivacyTip,
                onClick = { onAction(AppAction.Privacy) }
            )
        }

        item {
            val appTitle = master.profile?.appName?.ifBlank { master.salon.name } ?: master.salon.name
            val appVer = master.profile?.appVersion?.ifBlank { "1.0.0" } ?: "1.0.0"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = appTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Version $appVer • Config v${master.configVersion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
