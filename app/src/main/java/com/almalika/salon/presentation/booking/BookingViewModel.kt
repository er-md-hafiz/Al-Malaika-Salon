package com.almalika.salon.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almalika.salon.core.result.UiState
import com.almalika.salon.domain.model.*
import com.almalika.salon.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingScreenData(
    val config: BookingConfig,
    val masterConfig: SalonMasterConfig,
    val branches: List<Branch>,
    val services: List<SalonService>
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val getBookingConfigUseCase: GetBookingConfigUseCase,
    private val getMasterConfigUseCase: GetMasterConfigUseCase,
    private val getLocationsUseCase: GetLocationsUseCase,
    private val getServicesUseCase: GetServicesUseCase,
    private val formatWhatsAppMessageUseCase: FormatWhatsAppMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<BookingScreenData>>(UiState.Loading)
    val uiState: StateFlow<UiState<BookingScreenData>> = _uiState.asStateFlow()

    private val _formData = MutableStateFlow<Map<String, String>>(emptyMap())
    val formData: StateFlow<Map<String, String>> = _formData.asStateFlow()

    init {
        loadBookingDependencies()
    }

    private fun loadBookingDependencies() {
        viewModelScope.launch {
            combine(
                getBookingConfigUseCase(),
                getMasterConfigUseCase(),
                getLocationsUseCase(),
                getServicesUseCase()
            ) { config, master, branches, categories ->
                val allServices = categories.flatMap { it.services }
                BookingScreenData(config, master, branches, allServices)
            }
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Failed to initialize booking")
                }
                .collect { data ->
                    _uiState.value = UiState.Success(data)
                }
        }
    }

    fun updateField(fieldId: String, value: String) {
        _formData.value = _formData.value.toMutableMap().apply {
            put(fieldId, value)
        }
    }

    fun buildWhatsAppBookingPayload(data: BookingScreenData): Pair<String, String>? {
        val master = data.masterConfig
        val whatsappNumber = master.whatsapp.number.ifBlank { master.salon.whatsapp }
        if (whatsappNumber.isBlank()) return null

        val template = master.whatsapp.appointmentMessageTemplate.ifBlank {
            "Hello {salonName}, I would like to book an appointment.\nName: {name}\nPhone: {phone}\nService: {service}\nDate: {date}\nTime: {time}\nBranch: {branch}\nMessage: {message}"
        }

        val message = formatWhatsAppMessageUseCase(
            template = template,
            salonName = master.salon.name,
            values = _formData.value
        )

        return Pair(whatsappNumber, message)
    }
}
