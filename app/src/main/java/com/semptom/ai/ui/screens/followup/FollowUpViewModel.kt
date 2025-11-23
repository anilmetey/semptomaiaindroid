package com.semptom.ai.ui.screens.followup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semptom.ai.data.repository.ModelRepository
import com.semptom.ai.data.repository.ProfileRepository
import com.semptom.ai.domain.model.FollowUpQuestion
import com.semptom.ai.domain.model.SymptomSelection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FollowUpUiState(
    val questions: List<FollowUpQuestion> = emptyList(),
    val answers: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isAnalyzing: Boolean = false
)

@HiltViewModel
class FollowUpViewModel @Inject constructor(
    private val modelRepository: ModelRepository,
    private val profileRepository: ProfileRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FollowUpUiState())
    val uiState: StateFlow<FollowUpUiState> = _uiState.asStateFlow()
    
    private var symptomSelections: List<SymptomSelection> = emptyList()
    
    fun loadFollowUpQuestions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Get selected symptoms from previous screen
            // In a real app, you'd pass this via navigation or shared ViewModel
            val symptoms = modelRepository.getSymptoms().take(3) // Placeholder

            // TODO: Map symptoms to real follow-up questions when model is ready
            val allQuestions: List<FollowUpQuestion> = emptyList()

            _uiState.value = _uiState.value.copy(
                questions = allQuestions,
                isLoading = false
            )
        }
    }
    
    fun answerQuestion(questionId: String, answer: String) {
        val currentAnswers = _uiState.value.answers.toMutableMap()
        currentAnswers[questionId] = answer
        _uiState.value = _uiState.value.copy(answers = currentAnswers)
    }
    
    fun analyzeSymptoms(): Boolean {
        var isTriage = false
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true)
            
            try {
                val profile = profileRepository.getProfileOnce()
                
                // Build symptom selections with answers
                // This is simplified - in real app, combine with selected symptoms
                
                // Check triage
                val triageResult = modelRepository.evaluateTriage(symptomSelections, profile)
                isTriage = triageResult != null
                
                if (!isTriage) {
                    // Run inference
                    val result = modelRepository.runInference(symptomSelections, profile)
                    // Store result for next screen
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.value = _uiState.value.copy(isAnalyzing = false)
            }
        }
        
        return isTriage
    }
}
