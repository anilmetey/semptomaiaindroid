package com.semptom.ai.domain.model

/**
 * Represents a medical symptom with its basic properties
 */
data class Symptom(
    val id: String,
    val name: String,
    val description: String = "",
    val category: SymptomCategory,
    val severity: Int = 3,
    val commonCauses: List<String> = emptyList(),
    val relatedSymptoms: List<String> = emptyList(),
    var isSelected: Boolean = false
) {
    fun getSeverityText(): String = when (severity) {
        1 -> "Hafif"
        2 -> "Orta"
        3 -> "Orta Şiddetli"
        4 -> "Şiddetli"
        5 -> "Çok Şiddetli"
        else -> "Belirsiz"
    }
}

fun Symptom.toggleSelection(): Symptom = copy(isSelected = !isSelected)

/**
 * Categories for organizing symptoms by body system/area
 */
enum class SymptomCategory(val displayName: String) {
    GENERAL("Genel"),
    HEAD("Baş"),
    CHEST("Göğüs"),
    ABDOMEN("Karın"),
    EXTREMITIES("Uzuvlar"),
    SKIN("Cilt"),
    RESPIRATORY("Solunum"),
    CARDIOVASCULAR("Kalp-Damar"),
    GASTROINTESTINAL("Sindirim"),
    NEUROLOGICAL("Nörolojik"),
    MUSCULOSKELETAL("Kas-İskelet"),
    GENITOURINARY("Ürogenital"),
    PSYCHIATRIC("Psikiyatrik"),
    EYE("Göz"),
    EAR_NOSE_THROAT("Kulak-Burun-Boğaz")
}

