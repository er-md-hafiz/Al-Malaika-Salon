package com.almalika.salon.presentation.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almalika.salon.core.result.UiState
import com.almalika.salon.domain.model.Branch
import com.almalika.salon.domain.usecase.GetLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val getLocationsUseCase: GetLocationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Branch>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Branch>>> = _uiState.asStateFlow()

    init {
        loadBranches()
    }

    fun loadBranches() {
        viewModelScope.launch {
            getLocationsUseCase()
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Failed to load branches")
                }
                .collect { branches ->
                    _uiState.value = UiState.Success(branches)
                }
        }
    }
}
