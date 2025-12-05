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
    
    private val _selectedSymptoms = MutableStateFlow<List<Symptom>>(emptyList())
    private val allSymptoms = listOf(
        Symptom("headache", "Baş Ağrısı", "Genel baş ağrısı şikayeti", SymptomCategory.HEAD, 1),
        Symptom("fever", "Ateş", "Yüksek vücut sıcaklığı", SymptomCategory.GENERAL, 2),
        Symptom("cough", "Öksürük", "Kuru veya balgamlı öksürük", SymptomCategory.RESPIRATORY, 3),
        Symptom("fatigue", "Yorgunluk", "Halsizlik ve enerji düşüklüğü", SymptomCategory.GENERAL, 1),
        Symptom("nausea", "Mide Bulantısı", "Kusma hissi", SymptomCategory.GASTROINTESTINAL, 2),
        Symptom("chest_pain", "Göğüs Ağrısı", "Göğüs bölgesinde ağrı", SymptomCategory.CHEST, 5),
        Symptom("shortness_breath", "Nefes Darlığı", "Nefes almakta zorluk", SymptomCategory.RESPIRATORY, 5),
        Symptom("dizziness", "Baş Dönmesi", "Denge bozukluğu hissi", SymptomCategory.NEUROLOGICAL, 2),
        Symptom("sore_throat", "Boğaz Ağrısı", "Boğazda ağrı ve tahriş", SymptomCategory.EAR_NOSE_THROAT, 2),
        Symptom("runny_nose", "Burun Akıntısı", "Burundan sıvı akması", SymptomCategory.EAR_NOSE_THROAT, 1),
        Symptom("muscle_pain", "Kas Ağrısı", "Kaslarda ağrı ve hassasiyet", SymptomCategory.MUSCULOSKELETAL, 2),
        Symptom("joint_pain", "Eklem Ağrısı", "Eklem bölgelerinde ağrı", SymptomCategory.MUSCULOSKELETAL, 2),
        Symptom("stomach_pain", "Karın Ağrısı", "Karın bölgesinde ağrı", SymptomCategory.ABDOMEN, 3),
        Symptom("diarrhea", "İshal", "Sulu ve sık dışkılama", SymptomCategory.GASTROINTESTINAL, 2),
        Symptom("constipation", "Kabızlık", "Dışkılama zorluğu", SymptomCategory.GASTROINTESTINAL, 1),
        Symptom("loss_of_appetite", "İştah Kaybı", "Yemek yeme isteğinin azalması", SymptomCategory.GASTROINTESTINAL, 1),
        Symptom("weight_loss", "Kilo Kaybı", "İstenmeyen kilo düşüklüğü", SymptomCategory.GENERAL, 3),
        Symptom("weight_gain", "Kilo Artışı", "İstenmeyen kilo artışı", SymptomCategory.GENERAL, 2),
        Symptom("insomnia", "Uykusuzluk", "Uyku güçlüğü", SymptomCategory.PSYCHIATRIC, 2),
        Symptom("excessive_thirst", "Aşırı Susuzluk", "Sürekli su içme ihtiyacı", SymptomCategory.GENERAL, 3),
        Symptom("frequent_urination", "Sık İdrar", "Artmış idrar sıklığı", SymptomCategory.GENITOURINARY, 2),
        Symptom("skin_rash", "Cilt Döküntüsü", "Ciltte kızarıklık veya leke", SymptomCategory.SKIN, 2),
        Symptom("itching", "Kaşıntı", "Ciltte kaşıntı hissi", SymptomCategory.SKIN, 1),
        Symptom("swelling", "Şişlik", "Vücut bölgelerinde ödem", SymptomCategory.GENERAL, 3),
        Symptom("numbness", "Uyuşma", "His kaybı veya uyuşma", SymptomCategory.NEUROLOGICAL, 2),
        Symptom("tingling", "Karıncalanma", "Ciltte karıncalanma hissi", SymptomCategory.NEUROLOGICAL, 1),
        Symptom("blurred_vision", "Bulanık Görme", "Net görüş bozukluğu", SymptomCategory.EYE, 3),
        Symptom("hearing_loss", "İşitme Kaybı", "Duyma güçlüğü", SymptomCategory.EAR_NOSE_THROAT, 4),
        Symptom("ear_pain", "Kulak Ağrısı", "Kulakta ağrı", SymptomCategory.EAR_NOSE_THROAT, 3),
        Symptom("nosebleed", "Burun Kanaması", "Burundan kan gelmesi", SymptomCategory.EAR_NOSE_THROAT, 2),
        Symptom("bleeding_gums", "Diş Eti Kanaması", "Diş etlerinden kanama", SymptomCategory.EAR_NOSE_THROAT, 1),
        Symptom("hair_loss", "Saç Dökülmesi", "Saçların aşırı dökülmesi", SymptomCategory.SKIN, 1)
    )
    private val allConditions = listOf<Condition>()
    
    override suspend fun getSymptoms(): List<Symptom> = allSymptoms
    override suspend fun getSymptomById(id: String): Symptom? = allSymptoms.find { it.id == id }
    override suspend fun searchSymptoms(query: String): List<Symptom> = allSymptoms.filter { it.name.contains(query, ignoreCase = true) }
    override suspend fun getSymptomsByCategory(category: String): List<Symptom> = allSymptoms.filter { it.category.displayName == category }
    override suspend fun saveSelectedSymptoms(symptoms: List<Symptom>) { _selectedSymptoms.value = symptoms }
    override suspend fun getSelectedSymptoms(): List<Symptom> = _selectedSymptoms.value
    override fun observeSelectedSymptoms(): Flow<List<Symptom>> = _selectedSymptoms.asStateFlow()
    override suspend fun toggleSymptomSelection(symptomId: String, isSelected: Boolean) {
        val current = _selectedSymptoms.value.toMutableList()
        val symptom = getSymptomById(symptomId)
        if (isSelected && symptom != null) {
            if (!current.any { it.id == symptomId }) current.add(symptom)
        } else {
            current.removeAll { it.id == symptomId }
        }
        _selectedSymptoms.value = current
    }
    override suspend fun getSymptomCount(): Int = allSymptoms.size
    override suspend fun getConditions(): List<Condition> = allConditions
    override suspend fun getConditionById(id: String): Condition? = allConditions.find { it.id == id }
    override suspend fun searchConditions(query: String): List<Condition> = allConditions.filter { it.name.contains(query, ignoreCase = true) }
    override suspend fun getMatchingConditions(symptomIds: List<String>): List<Condition> = emptyList()
    override suspend fun getConditionsByCategory(category: String): List<Condition> = emptyList()
    override suspend fun generateDiagnoses(symptomIds: List<String>): List<Pair<Condition, Float>> = emptyList()
    override suspend fun clearAllData() { _selectedSymptoms.value = emptyList() }
    override suspend fun initializeData() {}
    override suspend fun isDataInitialized(): Boolean = true
}