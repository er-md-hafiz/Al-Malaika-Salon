package com.almalika.salon.presentation.homeservice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almalika.salon.core.result.UiState
import com.almalika.salon.domain.model.*
import com.almalika.salon.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeServiceScreenData(
    val config: BookingConfig,
    val masterConfig: SalonMasterConfig,
    val services: List<SalonService>
)

@HiltViewModel
class HomeServiceViewModel @Inject constructor(
    private val getHomeServiceConfigUseCase: GetHomeServiceConfigUseCase,
    private val getMasterConfigUseCase: GetMasterConfigUseCase,
    private val getServicesUseCase: GetServicesUseCase,
    private val formatWhatsAppMessageUseCase: FormatWhatsAppMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeServiceScreenData>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeServiceScreenData>> = _uiState.asStateFlow()

    private val _formData = MutableStateFlow<Map<String, String>>(emptyMap())
    val formData: StateFlow<Map<String, String>> = _formData.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                getHomeServiceConfigUseCase(),
                getMasterConfigUseCase(),
                getServicesUseCase()
            ) { config, master, categories ->
                val homeServices = categories.flatMap { it.services }.filter { it.homeServiceAvailable }
                HomeServiceScreenData(config, master, homeServices)
            }
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Failed to initialize home service")
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

    fun buildWhatsAppPayload(data: HomeServiceScreenData): Pair<String, String>? {
        val master = data.masterConfig
        val whatsappNumber = master.whatsapp.number.ifBlank { master.salon.whatsapp }
        if (whatsappNumber.isBlank()) return null

        val template = master.whatsapp.homeServiceMessageTemplate.ifBlank {
            "Hello {salonName}, I would like to book a Home Service.\nName: {name}\nPhone: {phone}\nAddress: {address}\nService: {service}\nDate: {date}\nTime: {time}\nMessage: {message}"
        }

        val message = formatWhatsAppMessageUseCase(
            template = template,
            salonName = master.salon.name,
            values = _formData.value
        )

        return Pair(whatsappNumber, message)
    }
}
