package com.almalika.salon.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almalika.salon.core.action.AppAction
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
    private val refreshConfigUseCase: RefreshConfigUseCase,
    val analyticsHelper: com.almalika.salon.core.analytics.AnalyticsHelper,
    val crashlyticsHelper: com.almalika.salon.core.crashlytics.CrashlyticsHelper
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
                    crashlyticsHelper.recordException(e)
                    analyticsHelper.logApiError("app_shell_init", errorType = e.javaClass.simpleName)
                    _uiState.value = UiState.Error(e.message ?: "Failed to initialize app shell")
                }
                .collect { data ->
                    _uiState.value = UiState.Success(data)
                }
        }
    }

    private fun triggerBackgroundSync() {
        viewModelScope.launch {
            try {
                refreshConfigUseCase(force = false)
            } catch (e: Exception) {
                crashlyticsHelper.recordException(e)
            }
        }
    }

    fun logScreen(route: String) {
        analyticsHelper.logScreenView(screenName = route)
        crashlyticsHelper.setCustomKey("current_screen", route)
    }

    fun logAction(action: AppAction) {
        when (action) {
            is AppAction.Booking -> analyticsHelper.logBookingStarted()
            is AppAction.HomeService -> analyticsHelper.logBookingStarted("home_service")
            is AppAction.Call -> analyticsHelper.logSelectContent("button", "call_salon")
            is AppAction.WhatsApp -> analyticsHelper.logBookingCompleted(branch = "whatsapp_inquiry")
            is AppAction.ShareApp -> analyticsHelper.logSelectContent("share", "share_app")
            is AppAction.RateApp -> analyticsHelper.logSelectContent("rate", "rate_app")
            is AppAction.OpenMap -> analyticsHelper.logSelectContent("map", "branch_map")
            else -> {}
        }
    }
}
