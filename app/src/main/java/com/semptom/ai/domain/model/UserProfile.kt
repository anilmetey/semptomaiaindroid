package com.semptom.ai.domain.model

data class UserProfile(
    val ageGroup: AgeGroup = AgeGroup.ADULT,
    val sex: Sex = Sex.MALE,
    val chronicDiseases: List<ChronicDisease> = emptyList(),
    val allergies: List<Allergy> = emptyList()
)
