package com.semptom.ai.data.repository

import com.semptom.ai.domain.model.UserProfile
import com.semptom.ai.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryProfileRepository @Inject constructor() : ProfileRepository {
    private val profileState = MutableStateFlow<UserProfile?>(null)
    
    override suspend fun getProfile(): Flow<UserProfile?> = profileState.asStateFlow()
    
    override suspend fun getProfileOnce(): UserProfile = profileState.value ?: UserProfile()
    
    override suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        profileState.value = profile
        return Result.success(Unit)
    }
    
    override suspend fun deleteAccount(): Result<Unit> {
        profileState.value = null
        return Result.success(Unit)
    }
}