package com.semptom.ai.domain.model

import java.util.UUID

data class JournalResult(
    val diseases: List<Disease>
)

data class JournalEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val symptoms: List<String>,
    val isTriageCase: Boolean,
    val result: JournalResult?
)
