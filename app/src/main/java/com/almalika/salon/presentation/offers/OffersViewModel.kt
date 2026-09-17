package com.almalika.salon.presentation.offers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almalika.salon.core.result.UiState
import com.almalika.salon.domain.model.Offer
import com.almalika.salon.domain.usecase.GetOffersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OffersViewModel @Inject constructor(
    private val getOffersUseCase: GetOffersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Offer>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Offer>>> = _uiState.asStateFlow()

    init {
        loadOffers()
    }

    fun loadOffers() {
        viewModelScope.launch {
            getOffersUseCase(filterExpired = true)
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Failed to load offers")
                }
                .collect { offers ->
                    _uiState.value = UiState.Success(offers)
                }
        }
    }
}
