package com.semptom.ai.domain.repository

import com.semptom.ai.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
	// Flow ile profil akışını döndür
	suspend fun getProfile(): Flow<UserProfile?>

	// Tek seferlik okuma
	suspend fun getProfileOnce(): UserProfile

	// Profili kaydet
	suspend fun saveProfile(profile: UserProfile): Result<Unit>

	// Hesabı sil
	suspend fun deleteAccount(): Result<Unit>
}

data class UserProfile(
    val id: String,
    val email: String,
    val fullName: String,
    val createdAt: Long
)
