package com.semptom.ai.data.repository

import com.semptom.ai.domain.model.Symptom
import com.semptom.ai.domain.model.SymptomSelection
import com.semptom.ai.domain.model.UserProfile

interface ModelRepository {
    suspend fun initialize()
    suspend fun getSymptoms(): List<Symptom>

    suspend fun evaluateTriage(
        selections: List<SymptomSelection>,
        profile: UserProfile
    ): Any?

    suspend fun runInference(
        selections: List<SymptomSelection>,
        profile: UserProfile
    ): Any?
}

class InMemoryModelRepository : ModelRepository {
    private var initialized = false

    override suspend fun initialize() {
        initialized = true
    }

    override suspend fun getSymptoms(): List<Symptom> = emptyList()

    override suspend fun evaluateTriage(selections: List<SymptomSelection>, profile: UserProfile): Any? {
        val chestPain = selections.any { it.symptom.id == "chest_pain" }
        val shortBreath = selections.any { it.symptom.id == "shortness_breath" }
        val severe = selections.any { it.symptom.severity >= 5 }
        val triageScore = (if (chestPain) 2 else 0) + (if (shortBreath) 2 else 0) + (if (severe) 1 else 0)
        return if (triageScore >= 3) mapOf("triage" to true, "score" to triageScore) else null
    }

    override suspend fun runInference(selections: List<SymptomSelection>, profile: UserProfile): Any? {
        val cough = selections.any { it.symptom.id == "cough" }
        val fever = selections.any { it.symptom.id == "fever" }
        val runny = selections.any { it.symptom.id == "runny_nose" }

        var cold = 0.3f
        var flu = 0.3f
        var allergy = 0.4f

        if (cough) { cold += 0.1f; flu += 0.15f }
        if (fever) { flu += 0.25f; cold += 0.05f }
        if (runny) { cold += 0.2f; allergy += 0.1f }

        val sum = (cold + flu + allergy).takeIf { it > 0f } ?: 1f
        return listOf(
            mapOf("id" to "cold", "probability" to (cold / sum)),
            mapOf("id" to "flu", "probability" to (flu / sum)),
            mapOf("id" to "allergy", "probability" to (allergy / sum))
        )
    }
}
