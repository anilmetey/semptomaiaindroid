package com.semptom.ai.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semptom.ai.data.repository.JournalRepository
import com.semptom.ai.data.repository.ModelRepository
import com.semptom.ai.data.repository.SymptomRepository
import com.semptom.ai.domain.model.Advice
import com.semptom.ai.domain.model.Disease
import com.semptom.ai.domain.model.Symptom
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultUiState(
    val diseases: List<Disease> = emptyList(),
    val advices: List<Advice> = emptyList(),
    val isSaved: Boolean = false,
    val hasAlertSymptoms: Boolean = false,
    val symptomCount: Int = 0
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val modelRepository: ModelRepository,
    private val journalRepository: JournalRepository,
    private val symptomRepository: SymptomRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()
    private var selectedSymptomsCache: List<Symptom> = emptyList()
    
    init {
        loadResults()
    }

    private fun loadResults() {
        // In a real app, get results from shared state or navigation args
        // For now, create demo data that depends on selected symptoms
        viewModelScope.launch {
            val selectedSymptoms = try {
                symptomRepository.getSelectedSymptoms()
            } catch (_: Exception) {
                emptyList()
            }

            selectedSymptomsCache = selectedSymptoms

            if (selectedSymptoms.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    diseases = listOf(
                        Disease("cold", "Soğuk Algınlığı", "Üst solunum yolu viral enfeksiyonu", 0.65f),
                        Disease("flu", "Grip", "İnfluenza virüsü enfeksiyonu", 0.25f),
                        Disease("allergy", "Alerjik Rinit", "Alerjen kaynaklı reaksiyon", 0.10f)
                    ),
                    advices = listOf(
                        Advice(
                            "Genel Öneriler",
                            "Bol sıvı tüketin, dinlenin ve vücudunuzun iyileşmesine izin verin.",
                            com.semptom.ai.domain.model.AdviceType.GENERAL
                        ),
                        Advice(
                            "Eczacı Danışması",
                            "Eczacınızdan ateş düşürücü ve ağrı kesici ilaçlar hakkında bilgi alabilirsiniz.",
                            com.semptom.ai.domain.model.AdviceType.MEDICATION_INFO,
                            listOf("Astım hastalarının İbuprofen kullanmadan önce doktoruna danışması gerekir")
                        )
                    ),
                    hasAlertSymptoms = false,
                    symptomCount = 0
                )
                return@launch
            }

            var coldScore = 0f
            var fluScore = 0f
            var allergyScore = 0f

            selectedSymptoms.forEach { symptom ->
                val factor = symptom.severity.coerceIn(1, 5).toFloat() / 3f
                when (symptom.id) {
                    "fever" -> {
                        fluScore += 0.4f * factor
                        coldScore += 0.2f * factor
                    }
                    "cough" -> {
                        fluScore += 0.3f * factor
                        coldScore += 0.3f * factor
                    }
                    "runny_nose", "sore_throat", "sneezing" -> {
                        coldScore += 0.4f * factor
                        allergyScore += 0.2f * factor
                    }
                    "itchy_eyes", "rash" -> {
                        allergyScore += 0.5f * factor
                    }
                    "shortness_breath", "chest_pain" -> {
                        fluScore += 0.3f * factor
                        coldScore += 0.2f * factor
                    }
                    "chills", "night_sweats", "muscle_pain", "fatigue" -> {
                        fluScore += 0.2f * factor
                        coldScore += 0.2f * factor
                    }
                }
            }

            val totalScore = (coldScore + fluScore + allergyScore).takeIf { it > 0f } ?: 1f

            val diseases = mutableListOf<Disease>()
            if (coldScore > 0f) {
                diseases += Disease(
                    "cold",
                    "Soğuk Algınlığı",
                    "Üst solunum yolu viral enfeksiyonu ile uyumlu bir tablo olabilir.",
                    (coldScore / totalScore).coerceIn(0.05f, 0.9f)
                )
            }
            if (fluScore > 0f) {
                diseases += Disease(
                    "flu",
                    "Grip",
                    "Bazı bulgular grip benzeri bir tablo ile uyumlu olabilir.",
                    (fluScore / totalScore).coerceIn(0.05f, 0.9f)
                )
            }
            if (allergyScore > 0f) {
                diseases += Disease(
                    "allergy",
                    "Alerjik Rinit",
                    "Belirtiler alerji ile ilişkili olabilir.",
                    (allergyScore / totalScore).coerceIn(0.05f, 0.9f)
                )
            }

            if (diseases.isEmpty()) {
                diseases += Disease(
                    "unspecified",
                    "Belirsiz Tablo",
                    "Belirtiler tek bir duruma özgü görünmüyor.",
                    1.0f
                )
            }

            val advices = mutableListOf(
                Advice(
                    "Genel Öneriler",
                    "Dinlenme, yeterli sıvı alımı ve belirtilerin takibi önemlidir.",
                    com.semptom.ai.domain.model.AdviceType.GENERAL
                )
            )

            val hasAlertSymptom = selectedSymptoms.any {
                it.id == "chest_pain" ||
                    it.id == "shortness_breath" ||
                    it.id == "confusion" ||
                    it.id == "stiff_neck"
            }

            if (hasAlertSymptom) {
                advices += Advice(
                    "Dikkat Gerektiren Bulgular",
                    "Bazı belirtiler daha yakından değerlendirme gerektirebilir.",
                    com.semptom.ai.domain.model.AdviceType.WARNING
                )
            }

            _uiState.value = _uiState.value.copy(
                diseases = diseases,
                advices = advices,
                hasAlertSymptoms = hasAlertSymptom,
                symptomCount = selectedSymptoms.size
            )
        }
    }
    
    fun saveToJournal() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                if (state.diseases.isNotEmpty()) {
                    val entry = com.semptom.ai.data.repository.JournalEntry(
                        symptoms = selectedSymptomsCache.map { it.name },
                        isTriageCase = false,
                        result = com.semptom.ai.data.repository.JournalResult(
                            diseases = state.diseases
                        )
                    )
                    journalRepository.addEntry(entry)
                }
                _uiState.value = state.copy(isSaved = true)
            } catch (_: Exception) {
                // ignore for demo
            }
        }
    }
}
