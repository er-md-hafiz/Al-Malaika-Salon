package com.almalika.salon.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almalika.salon.core.result.UiState
import com.almalika.salon.domain.model.GalleryImage
import com.almalika.salon.domain.usecase.GetGalleryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val getGalleryUseCase: GetGalleryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<GalleryImage>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<GalleryImage>>> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    init {
        loadGallery()
    }

    fun selectCategory(catId: String) {
        _selectedCategory.value = catId
        loadGallery()
    }

    private fun loadGallery() {
        viewModelScope.launch {
            getGalleryUseCase(_selectedCategory.value)
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Failed to load gallery")
                }
                .collect { images ->
                    _uiState.value = UiState.Success(images)
                }
        }
    }
}
