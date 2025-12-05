package com.semptom.ai.data.repository

import com.semptom.ai.domain.model.JournalEntry as DomainJournalEntry
import com.semptom.ai.domain.repository.JournalRepository as DomainJournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryJournalRepository : DomainJournalRepository {
    private val _entries = MutableStateFlow<List<DomainJournalEntry>>(emptyList())
    private val entries = mutableListOf<DomainJournalEntry>()

    override suspend fun getAllEntries(): Flow<List<DomainJournalEntry>> = _entries.asStateFlow()

    override suspend fun addEntry(entry: DomainJournalEntry): Result<Unit> {
        entries.add(entry)
        _entries.value = entries.toList()
        return Result.success(Unit)
    }

    override suspend fun updateEntry(entry: DomainJournalEntry): Result<Unit> {
        val index = entries.indexOfFirst { it.id == entry.id }
        if (index >= 0) {
            entries[index] = entry
            _entries.value = entries.toList()
            return Result.success(Unit)
        }
        return Result.failure(Exception("Entry not found"))
    }

    override suspend fun deleteEntry(entryId: String): Result<Unit> {
        entries.removeAll { it.id == entryId }
        _entries.value = entries.toList()
        return Result.success(Unit)
    }

    override suspend fun getEntryById(entryId: String): DomainJournalEntry? {
        return entries.find { it.id == entryId }
    }
}
