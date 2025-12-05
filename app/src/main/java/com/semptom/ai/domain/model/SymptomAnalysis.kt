package com.semptom.ai.domain.model

data class SymptomAnalysis(
    val title: String,
    val description: String,
    val recommendations: List<String>
)
