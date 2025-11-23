package com.semptom.ai.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String, fullName: String): Result<Unit>
    suspend fun logout()
}

class InMemoryAuthRepository : AuthRepository {
    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: Flow<Boolean> = _isLoggedIn

    override suspend fun login(email: String, password: String): Result<Unit> {
        _isLoggedIn.value = true
        return Result.success(Unit)
    }

    override suspend fun register(email: String, password: String, fullName: String): Result<Unit> {
        _isLoggedIn.value = true
        return Result.success(Unit)
    }

    override suspend fun logout() {
        _isLoggedIn.value = false
    }
}
