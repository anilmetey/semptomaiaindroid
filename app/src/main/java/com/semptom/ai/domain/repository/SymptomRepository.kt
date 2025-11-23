package com.semptom.ai.domain.repository

import com.semptom.ai.domain.model.Symptom
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing symptom data
 */
interface SymptomRepository {
    /**
     * Get all available symptoms
     */
    fun getAllSymptoms(): Flow<List<Symptom>>
    
    /**
     * Get symptoms by category
     */
    fun getSymptomsByCategory(category: String): Flow<List<Symptom>>
    
    /**
     * Search symptoms by name or description
     */
    fun searchSymptoms(query: String): Flow<List<Symptom>>
    
    /**
     * Toggle selection state of a symptom
     */
    suspend fun toggleSymptomSelection(symptomId: String, isSelected: Boolean)
    
    /**
     * Get all selected symptoms
     */
    fun getSelectedSymptoms(): Flow<List<Symptom>>
    
    /**
     * Get symptom by ID
     */
    suspend fun getSymptomById(id: String): Symptom?
    
    /**
     * Add sample symptoms (for testing/demo)
     */
    suspend fun addSampleSymptoms()
}
