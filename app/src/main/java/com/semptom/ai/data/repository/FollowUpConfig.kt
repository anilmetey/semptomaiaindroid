package com.semptom.ai.data.repository

data class FollowUpConfig(
    val meta: Meta,
    val symptoms: List<FollowUpSymptomConfig>
)

data class Meta(
    val version: String,
    val description: String,
    val language: String
)

data class FollowUpSymptomConfig(
    val id: String,
    val name: String,
    val category: String,
    val followUpQuestions: List<FollowUpQuestionConfig>
)

data class FollowUpQuestionConfig(
    val id: String,
    val question: String,
    val options: List<FollowUpOptionConfig>
)

data class FollowUpOptionConfig(
    val text: String,
    val value: String,
    val severityIndex: Int,
    val redFlag: Boolean = false
)
