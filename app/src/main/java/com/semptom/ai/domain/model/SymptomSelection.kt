package com.semptom.ai.domain.model

/**
 * Represents a selected symptom together with any follow-up answers.
 */
data class SymptomSelection(
    val symptom: Symptom,
    val answers: Map<String, String> = emptyMap()
)
