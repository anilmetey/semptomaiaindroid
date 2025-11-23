package com.semptom.ai.data.repository

import com.semptom.ai.domain.model.Condition
import com.semptom.ai.domain.model.Symptom
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing symptom and condition data
 */
interface SymptomRepository {
    // Region: Symptom Operations
    
    /**
     * Get all available symptoms
     * @return List of all symptoms
     */
    suspend fun getSymptoms(): List<Symptom>
    
    /**
     * Get a single symptom by its unique ID
     * @param id The symptom ID to look up
     * @return The symptom if found, null otherwise
     */
    suspend fun getSymptomById(id: String): Symptom?
    
    /**
     * Search for symptoms by name, description, or category
     * @param query Search query string
     * @return List of matching symptoms
     */
    suspend fun searchSymptoms(query: String): List<Symptom>
    
    /**
     * Get all symptoms in a specific category
     * @param category The category to filter by
     * @return List of symptoms in the specified category
     */
    suspend fun getSymptomsByCategory(category: String): List<Symptom>
    
    /**
     * Save the user's selected symptoms
     * @param symptoms List of selected symptoms
     */
    suspend fun saveSelectedSymptoms(symptoms: List<Symptom>)
    
    /**
     * Get the user's currently selected symptoms
     * @return List of selected symptoms
     */
    suspend fun getSelectedSymptoms(): List<Symptom>
    
    /**
     * Observe changes to the user's selected symptoms
     * @return Flow emitting the current list of selected symptoms
     */
    fun observeSelectedSymptoms(): Flow<List<Symptom>>
    
    /**
     * Toggle the selection state of a symptom
     * @param symptomId ID of the symptom to toggle
     * @param isSelected New selection state
     */
    suspend fun toggleSymptomSelection(symptomId: String, isSelected: Boolean)
    
    /**
     * Get the total number of available symptoms
     * @return Count of all symptoms
     */
    suspend fun getSymptomCount(): Int
    
    // Region: Condition Operations
    
    /**
     * Get all available medical conditions
     * @return List of all conditions
     */
    suspend fun getConditions(): List<Condition>
    
    /**
     * Get a condition by its ID
     * @param id The condition ID to look up
     * @return The condition if found, null otherwise
     */
    suspend fun getConditionById(id: String): Condition?
    
    /**
     * Search for conditions by name or description
     * @param query Search query string
     * @return List of matching conditions
     */
    suspend fun searchConditions(query: String): List<Condition>
    
    /**
     * Get conditions that match the provided symptoms
     * @param symptomIds List of symptom IDs to match against
     * @return List of matching conditions with confidence scores
     */
    suspend fun getMatchingConditions(symptomIds: List<String>): List<Condition>
    
    /**
     * Get conditions by category
     * @param category The category to filter by
     * @return List of conditions in the specified category
     */
    suspend fun getConditionsByCategory(category: String): List<Condition>
    
    // Region: Diagnosis Operations
    
    /**
     * Generate possible diagnoses based on selected symptoms
     * @param symptomIds List of selected symptom IDs
     * @return List of possible diagnoses with confidence scores
     */
    suspend fun generateDiagnoses(symptomIds: List<String>): List<Pair<Condition, Float>>
    
    // Region: Data Management
    
    /**
     * Clear all symptom and condition data (for testing or reset)
     */
    suspend fun clearAllData()
    
    /**
     * Initialize the database with default symptom and condition data
     */
    suspend fun initializeData()
    
    /**
     * Check if the database has been initialized with data
     * @return true if data is initialized, false otherwise
     */
    suspend fun isDataInitialized(): Boolean
}
