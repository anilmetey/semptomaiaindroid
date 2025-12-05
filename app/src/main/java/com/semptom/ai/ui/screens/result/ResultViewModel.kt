package com.semptom.ai.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semptom.ai.domain.repository.JournalRepository
import com.semptom.ai.domain.repository.ModelRepository
import com.semptom.ai.domain.repository.SymptomRepository
import com.semptom.ai.domain.repository.MedicationRepository
import com.semptom.ai.domain.repository.ProfileRepository
import com.semptom.ai.domain.model.Advice
import com.semptom.ai.domain.model.Disease
import com.semptom.ai.domain.model.Symptom
import com.semptom.ai.domain.model.AgeGroup
import com.semptom.ai.domain.model.ChronicDisease
import com.semptom.ai.domain.model.JournalEntry
import com.semptom.ai.domain.model.JournalResult
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
    private val symptomRepository: SymptomRepository,
    private val profileRepository: ProfileRepository,
    private val medicationRepository: MedicationRepository
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

            try {
                val profile = profileRepository.getProfileOnce()

                var weightCold = 1f
                var weightFlu = 1f
                var weightAllergy = 1f

                when (profile.ageGroup) {
                    AgeGroup.CHILD -> {
                        weightFlu *= 1.1f
                        weightAllergy *= 1.05f
                    }
                    AgeGroup.SENIOR -> {
                        weightFlu *= 1.15f
                        weightCold *= 1.05f
                    }
                    else -> {}
                }

                if (profile.chronicDiseases.contains(ChronicDisease.ASTHMA)) {
                    weightAllergy *= 1.15f
                }
                if (profile.chronicDiseases.contains(ChronicDisease.COPD)) {
                    weightFlu *= 1.1f
                }

                val sum = (diseases.sumOf { it.probability.toDouble() }).toFloat().takeIf { it > 0f } ?: 1f
                var coldP = diseases.find { it.id == "cold" }?.probability ?: 0f
                var fluP = diseases.find { it.id == "flu" }?.probability ?: 0f
                var allergyP = diseases.find { it.id == "allergy" }?.probability ?: 0f

                coldP *= weightCold
                fluP *= weightFlu
                allergyP *= weightAllergy

                val renorm = (coldP + fluP + allergyP).takeIf { it > 0f } ?: sum

                val adjusted = diseases.map { d ->
                    when (d.id) {
                        "cold" -> d.copy(probability = (coldP / renorm).coerceIn(0.05f, 0.9f))
                        "flu" -> d.copy(probability = (fluP / renorm).coerceIn(0.05f, 0.9f))
                        "allergy" -> d.copy(probability = (allergyP / renorm).coerceIn(0.05f, 0.9f))
                        else -> d
                    }
                }

                val medWarnings = medicationRepository.generateWarnings(profile)
                if (medWarnings.isNotEmpty()) {
                    advices += Advice(
                        "İlaç/Uyarılar",
                        "Profilinize göre dikkat edilmesi gereken noktalar.",
                        com.semptom.ai.domain.model.AdviceType.MEDICATION_INFO,
                        medWarnings
                    )
                }

                _uiState.value = _uiState.value.copy(
                    diseases = adjusted,
                    advices = advices,
                    hasAlertSymptoms = hasAlertSymptom,
                    symptomCount = selectedSymptoms.size
                )
                return@launch
            } catch (_: Exception) {}

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
                    val entry = JournalEntry(
                        symptoms = selectedSymptomsCache.map { it.name },
                        isTriageCase = false,
                        result = JournalResult(
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
