package com.semptom.ai.data.repository

import com.semptom.ai.domain.model.Medication as DomainMedication
import com.semptom.ai.domain.model.UserProfile
import com.semptom.ai.domain.repository.MedicationRepository as DomainMedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryMedicationRepository : DomainMedicationRepository {
    private val _medications = MutableStateFlow<List<DomainMedication>>(emptyList())
    private val medications = mutableListOf<DomainMedication>()

    override suspend fun getAllMedications(): Flow<List<DomainMedication>> = _medications.asStateFlow()

    override suspend fun addMedication(medication: DomainMedication): Result<Unit> {
        medications.add(medication)
        _medications.value = medications.toList()
        return Result.success(Unit)
    }

    override suspend fun updateMedication(medication: DomainMedication): Result<Unit> {
        val index = medications.indexOfFirst { it.id == medication.id }
        if (index >= 0) {
            medications[index] = medication
            _medications.value = medications.toList()
            return Result.success(Unit)
        }
        return Result.failure(Exception("Medication not found"))
    }

    override suspend fun deleteMedication(medicationId: String): Result<Unit> {
        medications.removeAll { it.id == medicationId }
        _medications.value = medications.toList()
        return Result.success(Unit)
    }

    override suspend fun getMedicationById(medicationId: String): DomainMedication? {
        return medications.find { it.id == medicationId }
    }

    override fun generateWarnings(profile: UserProfile): List<String> {
        // Demo: henüz gerçek kural seti yok, boş liste döndür
        return emptyList()
    }
}
