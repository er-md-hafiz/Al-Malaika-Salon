package com.almalika.salon.presentation.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almalika.salon.core.result.UiState
import com.almalika.salon.domain.model.ServiceCategory
import com.almalika.salon.domain.usecase.GetServicesUseCase
import com.almalika.salon.domain.usecase.RefreshConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val getServicesUseCase: GetServicesUseCase,
    private val refreshConfigUseCase: RefreshConfigUseCase,
    private val analyticsHelper: com.almalika.salon.core.analytics.AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ServiceCategory>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ServiceCategory>>> = _uiState.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    init {
        loadServices()
    }

    fun loadServices() {
        viewModelScope.launch {
            getServicesUseCase()
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Failed to load services")
                }
                .collect { categories ->
                    _uiState.value = UiState.Success(categories)
                    if (_selectedCategoryId.value == null && categories.isNotEmpty()) {
                        _selectedCategoryId.value = categories.first().id
                    }
                }
        }
    }

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
        analyticsHelper.logSelectContent(contentType = "service_category", itemId = categoryId)
    }

    fun refresh() {
        viewModelScope.launch {
            refreshConfigUseCase(force = true)
        }
    }
}
