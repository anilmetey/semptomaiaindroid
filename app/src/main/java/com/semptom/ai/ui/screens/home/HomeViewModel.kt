package com.semptom.ai.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semptom.ai.data.repository.ModelRepository
import com.semptom.ai.data.repository.SymptomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isModelReady: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val modelRepository: ModelRepository,
    private val symptomRepository: SymptomRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun initialize() {
        viewModelScope.launch {
            try {
                // Initialize ML model and demo data
                modelRepository.initialize()
                symptomRepository.initializeData()
                _uiState.value = _uiState.value.copy(isModelReady = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isModelReady = false,
                    error = e.message
                )
            }
        }
    }
}
