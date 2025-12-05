package com.semptom.ai.domain.model

data class ActiveIngredient(
    val id: String,
    val name: String
)

data class Medication(
    val id: String,
    val name: String,
    val ingredients: List<ActiveIngredient>,
    val indications: List<String> = emptyList()
)

data class InteractionRule(
    val id: String,
    val title: String,
    val description: String,
    val matches: (UserProfile) -> Boolean,
    val relatedIngredients: List<String> = emptyList()
)
