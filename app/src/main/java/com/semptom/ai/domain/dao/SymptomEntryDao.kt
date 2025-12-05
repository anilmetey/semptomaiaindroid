package com.semptom.ai.domain.dao

import kotlinx.coroutines.flow.Flow

interface SymptomEntryDao {
    suspend fun getAllEntries(): Flow<List<SymptomEntry>>
    suspend fun insertEntry(entry: SymptomEntry)
    suspend fun updateEntry(entry: SymptomEntry)
    suspend fun deleteEntry(entryId: String)
    suspend fun getEntryById(entryId: String): SymptomEntry?
}

data class SymptomEntry(
    val id: String,
    val symptoms: List<String>,
    val date: Long,
    val notes: String? = null
)
