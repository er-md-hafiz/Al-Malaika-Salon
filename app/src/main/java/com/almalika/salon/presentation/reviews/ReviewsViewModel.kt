package com.almalika.salon.presentation.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almalika.salon.core.result.UiState
import com.almalika.salon.domain.model.Review
import com.almalika.salon.domain.usecase.GetReviewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val getReviewsUseCase: GetReviewsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Review>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Review>>> = _uiState.asStateFlow()

    init {
        loadReviews()
    }

    fun loadReviews() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getReviewsUseCase()
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Failed to load salon reviews")
                }
                .collect { reviews ->
                    _uiState.value = UiState.Success(reviews)
                }
        }
    }
}
