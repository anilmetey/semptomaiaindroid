package com.semptom.ai.ui.screens.symptoms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semptom.ai.data.repository.SymptomRepository
import com.semptom.ai.domain.model.Symptom
import com.semptom.ai.domain.model.toggleSelection
import com.semptom.ai.domain.model.SymptomCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class SymptomUiState(
    val symptoms: List<Symptom> = emptyList(),
    val filteredSymptoms: List<Symptom> = emptyList(),
    val selectedSymptoms: List<Symptom> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: SymptomCategory? = null,
    val navigateToNext: Boolean = false
)

@HiltViewModel
class SymptomSelectionViewModel @Inject constructor(
    private val symptomRepository: SymptomRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SymptomUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSymptoms()
    }

    fun loadSymptoms() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val symptoms = symptomRepository.getSymptoms()
                _uiState.update { state ->
                    state.copy(
                        symptoms = symptoms,
                        filteredSymptoms = filterSymptoms(symptoms, state.searchQuery, state.selectedCategory),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message ?: "Semptomlar yüklenirken hata oluştu",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleSymptom(symptom: Symptom) {
        _uiState.update { state ->
            val updatedSymptoms = state.symptoms.map { 
                if (it.id == symptom.id) it.toggleSelection() else it 
            }
            val selected = updatedSymptoms.filter { it.isSelected }
            
            state.copy(
                symptoms = updatedSymptoms,
                filteredSymptoms = filterSymptoms(updatedSymptoms, state.searchQuery, state.selectedCategory),
                selectedSymptoms = selected
            )
        }
    }

    fun searchSymptoms(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredSymptoms = filterSymptoms(state.symptoms, query, state.selectedCategory)
            )
        }
    }

    fun filterByCategory(category: SymptomCategory?) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = if (state.selectedCategory == category) null else category,
                filteredSymptoms = filterSymptoms(state.symptoms, state.searchQuery, category)
            )
        }
    }

    private fun filterSymptoms(
        symptoms: List<Symptom>,
        query: String,
        category: SymptomCategory?
    ): List<Symptom> {
        return symptoms.filter { symptom ->
            val matchesSearch = symptom.name.contains(query, ignoreCase = true) ||
                              symptom.category.displayName.contains(query, ignoreCase = true)
            val matchesCategory = category == null || symptom.category == category
            matchesSearch && matchesCategory
        }.sortedBy { it.name }
    }
    
    fun updateSymptomSeverity(symptomId: String, newSeverity: Int) {
        _uiState.update { state ->
            val clamped = newSeverity.coerceIn(1, 5)
            val updatedSymptoms = state.symptoms.map { symptom ->
                if (symptom.id == symptomId) {
                    symptom.copy(severity = clamped)
                } else {
                    symptom
                }
            }
            val selected = updatedSymptoms.filter { it.isSelected }
            state.copy(
                symptoms = updatedSymptoms,
                filteredSymptoms = filterSymptoms(updatedSymptoms, state.searchQuery, state.selectedCategory),
                selectedSymptoms = selected
            )
        }
    }

    fun saveSelectedSymptoms() {
        val selected = _uiState.value.selectedSymptoms
        viewModelScope.launch {
            try {
                symptomRepository.saveSelectedSymptoms(selected)
            } catch (_: Exception) {
                // ignore for demo persistence
            }
        }
    }

    fun validateSelection(): Boolean {
        return if (_uiState.value.selectedSymptoms.isEmpty()) {
            _uiState.update { it.copy(error = "En az bir semptom seçmelisiniz") }
            false
        } else {
            true
        }
    }
    
    fun getSelectedSymptoms() = _uiState.value.selectedSymptoms
    
    fun proceedToNext() {
        _uiState.update { it.copy(navigateToNext = true) }
    }
    
    fun navigationHandled() {
        _uiState.update { it.copy(navigateToNext = false) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up any resources if needed
    }
}
