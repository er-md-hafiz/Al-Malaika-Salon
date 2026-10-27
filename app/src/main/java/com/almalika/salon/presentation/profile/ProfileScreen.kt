package com.almalika.salon.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almalika.salon.core.action.AppAction
import com.almalika.salon.domain.model.AuthState
import com.almalika.salon.domain.model.SalonMasterConfig
import com.almalika.salon.presentation.auth.AuthViewModel

@Composable
fun ProfileScreen(
    master: SalonMasterConfig,
    authViewModel: AuthViewModel,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onAction: (AppAction) -> Unit,
    onSignedOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.authState.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out from Al-Malaika Salon?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        authViewModel.signOut {
                            onSignedOut()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section: User Profile Card
        item {
            UserProfileCard(
                authState = authState,
                onSignInClick = { onAction(AppAction.Login) },
                onRegisterClick = { onAction(AppAction.Register) },
                onSignOutClick = { showSignOutDialog = true }
            )
        }

        // Section: App Preferences (Theme & Notifications)
        item {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // Dark Mode Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = "Dark Mode",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dark Theme",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Royal Al-Malaika golden dark aesthetic",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onToggleDarkMode
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Notifications Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Push Notifications",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Appointment reminders & exclusive offers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                }
            }
        }

        // Section: Salon & Help
        item {
            Text(
                text = "Salon & Services",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
            )
        }

        item {
            ProfileMenuRow(
                title = "About Al-Malaika Salon",
                subtitle = master.salon.tagline.ifBlank { "Royal Grooming for Men" },
                icon = Icons.Default.Info,
                onClick = { onAction(AppAction.About) }
            )
        }

        item {
            ProfileMenuRow(
                title = "Our Branches & Locations",
                subtitle = master.salon.address.ifBlank { "Visit our grooming studio in Hazaribagh" },
                icon = Icons.Default.Place,
                onClick = { onAction(AppAction.Locations) }
            )
        }

        item {
            ProfileMenuRow(
                title = "Client Reviews & Testimonials",
                subtitle = "Read verified customer grooming feedback",
                icon = Icons.Default.RateReview,
                onClick = { onAction(AppAction.Reviews) }
            )
        }

        item {
            ProfileMenuRow(
                title = "Contact & Enquiries",
                subtitle = master.salon.phone.ifBlank { "Phone, WhatsApp & salon timings" },
                icon = Icons.Default.Call,
                onClick = { onAction(AppAction.Contact) }
            )
        }

        // Section: Legal & Policies
        item {
            Text(
                text = "Legal & Policies",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
            )
        }

        item {
            ProfileMenuRow(
                title = "Terms & Conditions",
                subtitle = "Service guidelines & booking policies",
                icon = Icons.Default.Description,
                onClick = { onAction(AppAction.Terms) }
            )
        }

        item {
            ProfileMenuRow(
                title = "Privacy Policy",
                subtitle = "How your information is protected",
                icon = Icons.Default.PrivacyTip,
                onClick = { onAction(AppAction.Privacy) }
            )
        }

        // Section: Community & Feedback
        item {
            Text(
                text = "Community",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
            )
        }

        item {
            val shareMsg = master.profile?.shareMessage?.ifBlank { master.share.message } ?: master.share.message
            ProfileMenuRow(
                title = "Share Application",
                subtitle = "Recommend Al-Malaika Salon to friends & family",
                icon = Icons.Default.Share,
                onClick = { onAction(AppAction.ShareApp(shareMsg, master.share.playStoreUrl)) }
            )
        }

        item {
            ProfileMenuRow(
                title = "Rate on Google Play",
                subtitle = "Support our barbers & salon team with a 5-star review",
                icon = Icons.Default.Star,
                onClick = { onAction(AppAction.RateApp) }
            )
        }

        // Section: App Version & Build Information
        item {
            AppInformationFooter(master = master)
        }
    }
}

@Composable
private fun UserProfileCard(
    authState: AuthState,
    onSignInClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            when (authState) {
                is AuthState.Authenticated -> {
                    val user = authState.user
                    val isGuest = user.isAnonymous
                    val displayName = user.displayName?.ifBlank { if (isGuest) "Guest User" else "Al-Malaika Member" }
                        ?: if (isGuest) "Guest User" else "Al-Malaika Member"
                    val emailText = user.email?.takeIf { it.isNotBlank() }
                        ?: if (isGuest) "Guest Browsing Mode" else "Member Account"

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isGuest) Icons.Default.Person else Icons.Default.AccountCircle,
                                contentDescription = "User Avatar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = emailText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = if (isGuest) "Guest Browsing" else "Registered Member",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isGuest) Icons.Default.Shield else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isGuest) {
                            TextButton(onClick = onSignInClick) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sign In / Register")
                            }
                        } else {
                            Text(
                                text = "Account Active",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = onSignOutClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign Out")
                        }
                    }
                }

                else -> {
                    // Unauthenticated state
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Guest Browsing",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Enjoy royal grooming services & appointments",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onSignInClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Sign In")
                        }
                        OutlinedButton(
                            onClick = onRegisterClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Create Account")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

@Composable
private fun AppInformationFooter(master: SalonMasterConfig) {
    val appTitle = master.profile?.appName?.ifBlank { master.salon.name } ?: master.salon.name
    val appVer = master.profile?.appVersion?.ifBlank { "1.0.0" } ?: "1.0.0"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = appTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = master.salon.tagline.ifBlank { "Royal Grooming for Men" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Version $appVer • Build 1 • Schema v${master.schemaVersion} • Config v${master.configVersion}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "© 2026 Al-Malaika Salon. All rights reserved.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
            )
        }
    }
}
