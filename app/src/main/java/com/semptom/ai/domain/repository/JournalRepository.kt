package com.semptom.ai.domain.repository

import com.semptom.ai.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    suspend fun getAllEntries(): Flow<List<JournalEntry>>
    suspend fun addEntry(entry: JournalEntry): Result<Unit>
    suspend fun updateEntry(entry: JournalEntry): Result<Unit>
    suspend fun deleteEntry(entryId: String): Result<Unit>
    suspend fun getEntryById(entryId: String): JournalEntry?
}
