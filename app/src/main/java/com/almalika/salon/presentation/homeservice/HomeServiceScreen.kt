package com.almalika.salon.presentation.homeservice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.almalika.salon.core.action.AppAction
import com.almalika.salon.core.result.UiState
import com.almalika.salon.presentation.booking.DynamicFormField
import com.almalika.salon.presentation.common.ErrorRetryView
import com.almalika.salon.presentation.common.ShimmerCard

@Composable
fun HomeServiceScreen(
    viewModel: HomeServiceViewModel,
    onAction: (AppAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val formData by viewModel.formData.collectAsState()

    when (val state = uiState) {
        is UiState.Loading -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ShimmerCard(height = 50)
                ShimmerCard(height = 50)
                ShimmerCard(height = 50)
            }
        }

        is UiState.Error -> {
            ErrorRetryView(
                message = state.message,
                onRetry = {},
                modifier = modifier
            )
        }

        is UiState.Success -> {
            val data = state.data
            val fields = data.config.fields.sortedBy { it.order }

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = data.config.title.ifBlank { "Salon Services At Your Doorstep" },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    if (data.config.subtitle.isNotBlank()) {
                        Text(
                            text = data.config.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(fields, key = { it.id }) { field ->
                    DynamicFormField(
                        field = field,
                        currentValue = formData[field.id].orEmpty(),
                        onValueChange = { viewModel.updateField(field.id, it) },
                        branches = emptyList(),
                        services = data.services.map { it.name }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val payload = viewModel.buildWhatsAppPayload(data)
                            if (payload != null) {
                                onAction(AppAction.WhatsApp(payload.first, payload.second))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "Book Home Service on WhatsApp",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
