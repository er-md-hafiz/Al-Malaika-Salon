package com.almalika.salon.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almalika.salon.core.result.UiState
import com.almalika.salon.domain.model.*
import com.almalika.salon.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppShellData(
    val masterConfig: SalonMasterConfig,
    val services: List<ServiceCategory>,
    val offers: List<Offer>,
    val galleryImages: List<GalleryImage>,
    val branches: List<Branch>
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getMasterConfigUseCase: GetMasterConfigUseCase,
    private val getServicesUseCase: GetServicesUseCase,
    private val getOffersUseCase: GetOffersUseCase,
    private val getGalleryUseCase: GetGalleryUseCase,
    private val getLocationsUseCase: GetLocationsUseCase,
    private val refreshConfigUseCase: RefreshConfigUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AppShellData>>(UiState.Loading)
    val uiState: StateFlow<UiState<AppShellData>> = _uiState.asStateFlow()

    init {
        loadData()
        triggerBackgroundSync()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                getMasterConfigUseCase(),
                getServicesUseCase(),
                getOffersUseCase(),
                getGalleryUseCase(),
                getLocationsUseCase()
            ) { master, services, offers, gallery, branches ->
                AppShellData(master, services, offers, gallery, branches)
            }
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Failed to initialize app shell")
                }
                .collect { data ->
                    _uiState.value = UiState.Success(data)
                }
        }
    }

    private fun triggerBackgroundSync() {
        viewModelScope.launch {
            refreshConfigUseCase(force = false)
        }
    }
}
