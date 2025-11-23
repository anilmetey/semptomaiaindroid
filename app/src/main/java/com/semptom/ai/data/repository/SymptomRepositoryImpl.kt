package com.semptom.ai.data.repository

import com.semptom.ai.domain.model.Condition
import com.semptom.ai.domain.model.Symptom
import com.semptom.ai.domain.model.SymptomCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SymptomRepositoryImpl @Inject constructor() : SymptomRepository {

    // In-memory storage as a temporary replacement for missing DB layer
    private val allSymptoms = mutableListOf<Symptom>()
    private val selectedSymptoms = MutableStateFlow<List<Symptom>>(emptyList())

    private val allConditions = mutableListOf<Condition>()

    override suspend fun getSymptoms(): List<Symptom> {
        return allSymptoms.toList()
    }

    override suspend fun getSymptomById(id: String): Symptom? {
        return allSymptoms.firstOrNull { it.id == id }
    }

    override suspend fun searchSymptoms(query: String): List<Symptom> {
        return allSymptoms.filter { symptom ->
            symptom.name.contains(query, ignoreCase = true)
        }
    }

    override suspend fun getSymptomsByCategory(category: String): List<Symptom> {
        return allSymptoms.filter { it.category.name == category }
    }

    override suspend fun saveSelectedSymptoms(symptoms: List<Symptom>) {
        selectedSymptoms.value = symptoms
    }

    override suspend fun getSelectedSymptoms(): List<Symptom> {
        return selectedSymptoms.value
    }

    override fun observeSelectedSymptoms(): Flow<List<Symptom>> {
        return selectedSymptoms.asStateFlow()
    }

    override suspend fun toggleSymptomSelection(symptomId: String, isSelected: Boolean) {
        val updated = allSymptoms.map { symptom ->
            if (symptom.id == symptomId) {
                symptom.copy(isSelected = isSelected)
            } else symptom
        }
        allSymptoms.clear()
        allSymptoms.addAll(updated)
        selectedSymptoms.value = updated.filter { it.isSelected }
    }

    override suspend fun getSymptomCount(): Int {
        return allSymptoms.size
    }

    override suspend fun getConditions(): List<Condition> {
        return allConditions.toList()
    }

    override suspend fun getConditionById(id: String): Condition? {
        return allConditions.firstOrNull { it.id == id }
    }

    override suspend fun searchConditions(query: String): List<Condition> {
        return allConditions.filter { condition ->
            condition.name.contains(query, ignoreCase = true)
        }
    }

    override suspend fun getMatchingConditions(symptomIds: List<String>): List<Condition> {
        // Simple placeholder: return all conditions for now
        return allConditions.toList()
    }

    override suspend fun getConditionsByCategory(category: String): List<Condition> {
        return allConditions.filter { it.category == category }
    }

    override suspend fun generateDiagnoses(symptomIds: List<String>): List<Pair<Condition, Float>> {
        // Placeholder: return empty list until a real model is wired
        return emptyList()
    }

    override suspend fun clearAllData() {
        allSymptoms.clear()
        allConditions.clear()
        selectedSymptoms.value = emptyList()
    }

    override suspend fun initializeData() {
        if (allSymptoms.isNotEmpty() || allConditions.isNotEmpty()) return

        // Expanded demo symptom set so UI is more representative
        allSymptoms.addAll(
            listOf(
                Symptom(
                    id = "fever",
                    name = "Ateş",
                    description = "Vücut ısısında artış, üşüme veya titreme",
                    category = SymptomCategory.GENERAL,
                    severity = 3
                ),
                Symptom(
                    id = "cough",
                    name = "Öksürük",
                    description = "Kuru veya balgamlı öksürük",
                    category = SymptomCategory.RESPIRATORY,
                    severity = 2
                ),
                Symptom(
                    id = "runny_nose",
                    name = "Burun Akıntısı",
                    description = "Sulu veya koyu kıvamlı burun akıntısı",
                    category = SymptomCategory.RESPIRATORY,
                    severity = 1
                ),
                Symptom(
                    id = "sore_throat",
                    name = "Boğaz Ağrısı",
                    description = "Yutkunmakla artan boğazda yanma veya batma hissi",
                    category = SymptomCategory.EAR_NOSE_THROAT,
                    severity = 2
                ),
                Symptom(
                    id = "shortness_breath",
                    name = "Nefes Darlığı",
                    description = "Nefes nefese kalma veya derin nefes alamama hissi",
                    category = SymptomCategory.RESPIRATORY,
                    severity = 4
                ),
                Symptom(
                    id = "chest_pain",
                    name = "Göğüs Ağrısı",
                    description = "Göğüste baskı, sıkışma veya batma hissi",
                    category = SymptomCategory.CHEST,
                    severity = 4
                ),
                Symptom(
                    id = "headache",
                    name = "Baş Ağrısı",
                    description = "Sürekli veya aralıklı baş ağrısı",
                    category = SymptomCategory.HEAD,
                    severity = 2
                ),
                Symptom(
                    id = "nausea",
                    name = "Mide Bulantısı",
                    description = "Mide bölgesinde bulantı hissi",
                    category = SymptomCategory.GASTROINTESTINAL,
                    severity = 2
                ),
                Symptom(
                    id = "vomiting",
                    name = "Kusma",
                    description = "Tekrarlayan veya şiddetli kusma",
                    category = SymptomCategory.GASTROINTESTINAL,
                    severity = 3
                ),
                Symptom(
                    id = "diarrhea",
                    name = "İshal",
                    description = "Sık ve sulu dışkılama",
                    category = SymptomCategory.GASTROINTESTINAL,
                    severity = 2
                ),
                Symptom(
                    id = "rash",
                    name = "Döküntü",
                    description = "Ciltte kızarıklık, kabarıklık veya lekelenme",
                    category = SymptomCategory.SKIN,
                    severity = 2
                ),
                Symptom(
                    id = "sneezing",
                    name = "Hapşırma",
                    description = "Tekrarlayan hapşırma atakları",
                    category = SymptomCategory.RESPIRATORY,
                    severity = 1
                ),
                Symptom(
                    id = "itchy_eyes",
                    name = "Göz Kaşıntısı",
                    description = "Gözlerde kaşıntı ve sulanma",
                    category = SymptomCategory.EYE,
                    severity = 1
                ),
                Symptom(
                    id = "loss_smell_taste",
                    name = "Koku/Tat Kaybı",
                    description = "Koku veya tat duyusunda azalma/kaybolma",
                    category = SymptomCategory.NEUROLOGICAL,
                    severity = 3
                ),
                Symptom(
                    id = "confusion",
                    name = "Bilinç Bulanıklığı",
                    description = "Çevreyi algılamada güçlük veya karışıklık",
                    category = SymptomCategory.NEUROLOGICAL,
                    severity = 5
                ),
                Symptom(
                    id = "stiff_neck",
                    name = "Ense Sertliği",
                    description = "Başınızı öne eğmekte zorlanma",
                    category = SymptomCategory.NEUROLOGICAL,
                    severity = 4
                ),
                Symptom(
                    id = "chills",
                    name = "Titreme",
                    description = "Üşüme ile birlikte istemsiz titreme",
                    category = SymptomCategory.GENERAL,
                    severity = 2
                ),
                Symptom(
                    id = "night_sweats",
                    name = "Gece Terlemeleri",
                    description = "Geceleri uykudan terlemiş uyanma",
                    category = SymptomCategory.GENERAL,
                    severity = 2
                ),
                Symptom(
                    id = "muscle_pain",
                    name = "Kas Ağrısı",
                    description = "Kaslarda hassasiyet ve ağrı",
                    category = SymptomCategory.MUSCULOSKELETAL,
                    severity = 2
                ),
                Symptom(
                    id = "fatigue",
                    name = "Yorgunluk / Halsizlik",
                    description = "Genel enerji düşüklüğü ve halsizlik",
                    category = SymptomCategory.GENERAL,
                    severity = 2
                )
            )
        )
    }

    override suspend fun isDataInitialized(): Boolean {
        return allSymptoms.isNotEmpty() || allConditions.isNotEmpty()
    }
}
