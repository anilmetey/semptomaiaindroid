package com.semptom.ai.domain.model

/**
 * Represents a possible disease/diagnosis with a probability score.
 */
data class Disease(
    val id: String,
    val name: String,
    val description: String = "",
    val probability: Float = 0f
)
