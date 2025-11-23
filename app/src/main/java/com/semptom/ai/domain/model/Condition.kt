package com.semptom.ai.domain.model

/**
 * Represents a medical condition or disease that can be associated with symptoms
 * @property id Unique identifier for the condition
 * @property name Name of the condition (e.g., "Common Cold", "Influenza")
 * @property description Detailed description of the condition
 * @property category Category of the condition
 * @param severity Severity level from 1 (mild) to 5 (severe)
 * @property commonSymptoms List of common symptom IDs associated with this condition
 * @property riskFactors List of risk factors for this condition
 * @property whenToSeeDoctor Guidance on when to seek medical attention
 * @property selfCareTips Self-care recommendations for managing the condition
 */
data class Condition(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val severity: Int = 3,
    val commonSymptoms: List<String> = emptyList(),
    val riskFactors: List<String> = emptyList(),
    val whenToSeeDoctor: String = "Belirtiler şiddetliyse veya 2 haftadan uzun sürerse doktora başvurun.",
    val selfCareTips: List<String> = emptyList()
) {
    /**
     * Gets the severity level as a human-readable string
     */
    fun getSeverityText(): String = when (severity) {
        1 -> "Hafif"
        2 -> "Orta"
        3 -> "Orta Şiddetli"
        4 -> "Şiddetli"
        5 -> "Çok Şiddetli"
        else -> "Belirsiz"
    }

    companion object {
        // Common condition categories
        const val CATEGORY_RESPIRATORY = "Solunum Yolu"
        const val CATEGORY_GASTROINTESTINAL = "Sindirim Sistemi"
        const val CATEGORY_CARDIOVASCULAR = "Kalp ve Damar"
        const val CATEGORY_NEUROLOGICAL = "Nörolojik"
        const val CATEGORY_MUSCULOSKELETAL = "Kas-İskelet Sistemi"
        const val CATEGORY_INFECTIOUS = "Enfeksiyon"
        const val CATEGORY_METABOLIC = "Metabolik"
        const val CATEGORY_PSYCHIATRIC = "Psikiyatrik"
    }
}

/**
 * Represents a possible diagnosis based on reported symptoms
 * @property condition The suspected medical condition
 * @property confidence Confidence score from 0.0 to 1.0 (1.0 being most confident)
 * @property matchingSymptoms List of symptoms that match this condition
 * @property recommendedActions Recommended actions to take
 */
data class Diagnosis(
    val condition: Condition,
    val confidence: Float,
    val matchingSymptoms: List<Symptom>,
    val recommendedActions: List<String> = emptyList()
) {
    /**
     * Gets the confidence as a percentage string
     */
    fun getConfidencePercentage(): String = "${(confidence * 100).toInt()}%"
    
    /**
     * Gets a list of symptom names that match this diagnosis
     */
    fun getMatchingSymptomNames(): List<String> = matchingSymptoms.map { it.name }
}
