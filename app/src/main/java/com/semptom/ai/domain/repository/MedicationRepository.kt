package com.semptom.ai.domain.repository

import com.semptom.ai.domain.model.Medication
import com.semptom.ai.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    suspend fun getAllMedications(): Flow<List<Medication>>
    suspend fun addMedication(medication: Medication): Result<Unit>
    suspend fun updateMedication(medication: Medication): Result<Unit>
    suspend fun deleteMedication(medicationId: String): Result<Unit>
    suspend fun getMedicationById(medicationId: String): Medication?

    fun generateWarnings(profile: UserProfile): List<String>
}
