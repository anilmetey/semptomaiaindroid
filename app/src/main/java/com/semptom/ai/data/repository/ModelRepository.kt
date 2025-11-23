package com.semptom.ai.data.repository

import com.semptom.ai.domain.model.Symptom
import com.semptom.ai.domain.model.SymptomSelection
import com.semptom.ai.domain.model.UserProfile

interface ModelRepository {
    suspend fun initialize()
    suspend fun getSymptoms(): List<Symptom>
    suspend fun evaluateTriage(selections: List<SymptomSelection>, profile: UserProfile): Any?
    suspend fun runInference(selections: List<SymptomSelection>, profile: UserProfile): Any?
}

class InMemoryModelRepository : ModelRepository {
    private var initialized = false

    override suspend fun initialize() {
        initialized = true
    }

    override suspend fun getSymptoms(): List<Symptom> = emptyList()

    override suspend fun evaluateTriage(selections: List<SymptomSelection>, profile: UserProfile): Any? = null

    override suspend fun runInference(selections: List<SymptomSelection>, profile: UserProfile): Any? = null
}
