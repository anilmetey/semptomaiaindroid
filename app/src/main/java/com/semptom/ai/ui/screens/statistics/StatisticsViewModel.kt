package com.semptom.ai.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semptom.ai.data.local.dao.SymptomEntryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class SymptomStat(
    val name: String,
    val count: Int,
    val percentage: Float
)

data class DiseaseStat(
    val name: String,
    val count: Int,
    val percentage: Float
)

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val totalAnalyses: Int = 0,
    val monthlyAnalyses: Int = 0,
    val journalEntries: Int = 0,
    val daysSinceLastAnalysis: Int = 0,
    val topSymptoms: List<SymptomStat> = emptyList(),
    val topDiseases: List<DiseaseStat> = emptyList()
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val symptomEntryDao: SymptomEntryDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val allEntries = symptomEntryDao.getAllEntries().first()
                
                // Calculate total analyses
                val totalAnalyses = allEntries.size
                
                // Calculate monthly analyses
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)
                
                val monthlyAnalyses = allEntries.count { entry ->
                    calendar.timeInMillis = entry.timestamp
                    calendar.get(Calendar.MONTH) == currentMonth && 
                    calendar.get(Calendar.YEAR) == currentYear
                }
                
                // Calculate days since last analysis
                val lastEntry = allEntries.maxByOrNull { it.timestamp }
                val daysSinceLastAnalysis = if (lastEntry != null) {
                    val diff = System.currentTimeMillis() - lastEntry.timestamp
                    (diff / (1000 * 60 * 60 * 24)).toInt()
                } else {
                    0
                }
                
                // Calculate top symptoms (mock data for now)
                val topSymptoms = listOf(
                    SymptomStat("Ateş", 15, 75f),
                    SymptomStat("Baş Ağrısı", 12, 60f),
                    SymptomStat("Öksürük", 10, 50f),
                    SymptomStat("Yorgunluk", 8, 40f),
                    SymptomStat("Boğaz Ağrısı", 6, 30f)
                )
                
                // Calculate top diseases (mock data for now)
                val topDiseases = listOf(
                    DiseaseStat("Soğuk Algınlığı", 8, 40f),
                    DiseaseStat("Grip", 6, 30f),
                    DiseaseStat("Sinüzit", 4, 20f),
                    DiseaseStat("Alerjik Rinit", 2, 10f)
                )
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalAnalyses = totalAnalyses,
                        monthlyAnalyses = monthlyAnalyses,
                        journalEntries = totalAnalyses,
                        daysSinceLastAnalysis = daysSinceLastAnalysis,
                        topSymptoms = topSymptoms,
                        topDiseases = topDiseases
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
