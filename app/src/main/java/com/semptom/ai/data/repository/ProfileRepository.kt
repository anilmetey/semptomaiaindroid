package com.semptom.ai.data.repository

import com.semptom.ai.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface ProfileRepository {
    fun getProfile(): Flow<UserProfile?>
    suspend fun getProfileOnce(): UserProfile
    suspend fun saveProfile(profile: UserProfile)
}

class InMemoryProfileRepository : ProfileRepository {
    private val state = MutableStateFlow<UserProfile?>(UserProfile())

    override fun getProfile(): Flow<UserProfile?> = state

    override suspend fun getProfileOnce(): UserProfile = state.value ?: UserProfile()

    override suspend fun saveProfile(profile: UserProfile) {
        state.value = profile
    }
}
