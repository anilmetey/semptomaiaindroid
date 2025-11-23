package com.semptom.ai.data.repository

import com.semptom.ai.domain.model.Disease
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Simple journal entry model for in-memory usage
data class JournalEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val symptoms: List<String> = emptyList(),
    val isTriageCase: Boolean = false,
    val result: JournalResult? = null
)

data class JournalResult(
    val diseases: List<Disease> = emptyList()
)

interface JournalRepository {
    fun getAllEntries(): Flow<List<JournalEntry>>
    suspend fun addEntry(entry: JournalEntry)
    suspend fun deleteEntry(entry: JournalEntry)
}

class InMemoryJournalRepository : JournalRepository {
    private val entriesState = MutableStateFlow<List<JournalEntry>>(emptyList())

    override fun getAllEntries(): Flow<List<JournalEntry>> = entriesState

    override suspend fun addEntry(entry: JournalEntry) {
        entriesState.value = entriesState.value + entry
    }

    override suspend fun deleteEntry(entry: JournalEntry) {
        entriesState.value = entriesState.value.filterNot { it.id == entry.id }
    }
}
