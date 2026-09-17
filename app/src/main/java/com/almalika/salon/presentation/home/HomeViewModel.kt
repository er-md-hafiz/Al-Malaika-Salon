package com.almalika.salon.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almalika.salon.core.result.UiState
import com.almalika.salon.domain.model.HomeConfig
import com.almalika.salon.domain.usecase.GetHomeConfigUseCase
import com.almalika.salon.domain.usecase.RefreshConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeConfigUseCase: GetHomeConfigUseCase,
    private val refreshConfigUseCase: RefreshConfigUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeConfig>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeConfig>> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadHomeData()
        syncConfigInBackground()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            getHomeConfigUseCase()
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Failed to load home content")
                }
                .collect { config ->
                    _uiState.value = UiState.Success(config)
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            refreshConfigUseCase(force = true)
            _isRefreshing.value = false
        }
    }

    private fun syncConfigInBackground() {
        viewModelScope.launch {
            refreshConfigUseCase(force = false)
        }
    }
}
