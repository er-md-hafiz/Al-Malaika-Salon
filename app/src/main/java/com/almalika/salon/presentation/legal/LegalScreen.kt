package com.almalika.salon.presentation.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PrivacyTip
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
fun TermsScreen(
    master: SalonMasterConfig,
    onAction: (AppAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LegalContentScreen(
        title = "Terms & Conditions",
        subtitle = "Salon policies, appointment standards, and service agreements",
        icon = Icons.Default.Description,
        sections = listOf(
            PolicySection(
                title = "1. Appointments & Punctuality",
                content = "Clients are encouraged to arrive 5–10 minutes prior to scheduled appointments. In case of delay exceeding 15 minutes, the salon reserves the right to reschedule or shorten the service to respect succeeding bookings."
            ),
            PolicySection(
                title = "2. Cancellations & Rescheduling",
                content = "Please notify us at least 2 hours in advance via WhatsApp or phone call if you need to reschedule or cancel your appointment. We appreciate your cooperation in helping us manage our stylists' schedules."
            ),
            PolicySection(
                title = "3. Health, Hygiene & Allergies",
                content = "Please inform our stylists before any chemical treatment (hair coloring, keratin, skin therapies, etc.) if you have known skin allergies, sensitivities, or medical conditions. A patch test is recommended 24 hours prior to technical treatments."
            ),
            PolicySection(
                title = "4. Pricing & Special Offers",
                content = "All prices displayed in our services menu and app are indicative and subject to change without prior notice. Promotional vouchers and discounts cannot be combined unless explicitly stated."
            ),
            PolicySection(
                title = "5. Valuables & Salon Conduct",
                content = "Al-Malaika Salon takes utmost care of client comfort; however, the management cannot be held liable for personal belongings left unattended on salon premises."
            )
        ),
        webUrl = master.legal.termsUrl,
        onAction = onAction,
        modifier = modifier
    )
}

@Composable
fun PrivacyScreen(
    master: SalonMasterConfig,
    onAction: (AppAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LegalContentScreen(
        title = "Privacy Policy",
        subtitle = "Our commitment to client privacy, data confidentiality, and security",
        icon = Icons.Default.PrivacyTip,
        sections = listOf(
            PolicySection(
                title = "1. Information We Collect",
                content = "We collect basic contact details provided during booking—such as your name, contact phone number, chosen branch, and service preferences—exclusively to fulfill appointment requests."
            ),
            PolicySection(
                title = "2. How We Use Your Data",
                content = "Your information is used solely to confirm appointment dates, send service reminders, provide support, and improve your overall salon experience. We do not sell or trade personal information to any third parties."
            ),
            PolicySection(
                title = "3. Push Notifications & Permissions",
                content = "If granted, notification permissions are used to send booking updates, exclusive festive offers, and operating hour notices. You can enable or disable notifications at any time from device Settings."
            ),
            PolicySection(
                title = "4. Data Security",
                content = "We implement industry-standard security safeguards to protect your personal information against unauthorized access, loss, or misuse."
            ),
            PolicySection(
                title = "5. Contacting Us",
                content = "For questions or concerns regarding our privacy practices, please contact the salon administration via our Contact Us screen or email us directly."
            )
        ),
        webUrl = master.legal.privacyUrl,
        onAction = onAction,
        modifier = modifier
    )
}

private data class PolicySection(
    val title: String,
    val content: String
)

@Composable
private fun LegalContentScreen(
    title: String,
    subtitle: String,
    icon: ImageVector,
    sections: List<PolicySection>,
    webUrl: String,
    onAction: (AppAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(sections.size) { index ->
            val section = sections[index]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = section.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }
            }
        }

        if (webUrl.isNotBlank()) {
            item {
                OutlinedButton(
                    onClick = { onAction(AppAction.OpenUrl(webUrl, title)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Full Document on Website")
                }
            }
        }
    }
}
