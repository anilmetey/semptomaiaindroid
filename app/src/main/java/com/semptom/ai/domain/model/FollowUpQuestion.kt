package com.semptom.ai.domain.model

/**
 * Represents a follow-up question related to a symptom.
 */
data class FollowUpQuestion(
    val id: String,
    val question: String,
    val options: List<String>
)
