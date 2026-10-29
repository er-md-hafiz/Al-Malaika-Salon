package com.almalika.salon.presentation.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.almalika.salon.core.action.AppAction
import com.almalika.salon.core.result.UiState
import com.almalika.salon.domain.model.BookingField
import com.almalika.salon.domain.model.BookingFieldType
import com.almalika.salon.presentation.common.ErrorRetryView
import com.almalika.salon.presentation.common.ShimmerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: BookingViewModel,
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
                        text = data.config.title.ifBlank { "Book Salon Appointment" },
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
                        branches = data.branches.map { it.name },
                        services = data.services.map { it.name }
                    )
                }

                if (data.showBarberPicker && data.barbers.size > 1) {
                    item {
                        DropdownSelectorField(
                            label = "Select Stylist / Barber",
                            options = data.barbers.map { it.name },
                            selectedOption = formData["barber"].orEmpty().ifBlank { data.barbers.firstOrNull()?.name.orEmpty() },
                            onOptionSelected = { viewModel.updateField("barber", it) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val payload = viewModel.buildWhatsAppBookingPayload(data)
                            if (payload != null) {
                                onAction(AppAction.WhatsApp(payload.first, payload.second))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "Confirm Booking on WhatsApp",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicFormField(
    field: BookingField,
    currentValue: String,
    onValueChange: (String) -> Unit,
    branches: List<String>,
    services: List<String>
) {
    when (field.type) {
        BookingFieldType.BRANCH -> {
            DropdownSelectorField(
                label = field.label,
                options = branches,
                selectedOption = currentValue,
                onOptionSelected = onValueChange
            )
        }

        BookingFieldType.SERVICE -> {
            DropdownSelectorField(
                label = field.label,
                options = services,
                selectedOption = currentValue,
                onOptionSelected = onValueChange
            )
        }

        BookingFieldType.DROPDOWN -> {
            DropdownSelectorField(
                label = field.label,
                options = field.options,
                selectedOption = currentValue,
                onOptionSelected = onValueChange
            )
        }

        BookingFieldType.MULTILINE, BookingFieldType.ADDRESS -> {
            OutlinedTextField(
                value = currentValue,
                onValueChange = onValueChange,
                label = { Text(field.label) },
                placeholder = { Text(field.placeholder) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )
        }

        else -> {
            OutlinedTextField(
                value = currentValue,
                onValueChange = onValueChange,
                label = { Text(field.label) },
                placeholder = { Text(field.placeholder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelectorField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
