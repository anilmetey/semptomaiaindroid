package com.semptom.ai.domain.model

enum class AdviceType {
    GENERAL,
    MEDICATION_INFO,
    LIFESTYLE,
    WARNING
}

/**
 * Represents an advice item shown on the result screen.
 */
data class Advice(
    val title: String,
    val description: String,
    val type: AdviceType = AdviceType.GENERAL,
    val warnings: List<String> = emptyList()
)
