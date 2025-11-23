package com.semptom.ai.data.local.dao

import com.semptom.ai.data.repository.JournalEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface SymptomEntryDao {
    fun getAllEntries(): Flow<List<JournalEntry>>
}

class InMemorySymptomEntryDao : SymptomEntryDao {
    private val entries = MutableStateFlow<List<JournalEntry>>(emptyList())

    override fun getAllEntries(): Flow<List<JournalEntry>> = entries
}
