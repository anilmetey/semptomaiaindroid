package com.semptom.ai.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.semptom.ai.domain.repository.AuthRepository as DomainAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAuthRepository(context: Context) : DomainAuthRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    override val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()

    private val KEY_EMAIL = "user_email"
    private val KEY_PASSWORD = "user_password"
    private val KEY_NAME = "user_name"

    override suspend fun login(email: String, password: String): Result<Unit> {
        val savedEmail = prefs.getString(KEY_EMAIL, null)
        val savedPassword = prefs.getString(KEY_PASSWORD, null)

        return if (savedEmail == email && savedPassword == password) {
            prefs.edit()
                .putBoolean("is_logged_in", true)
                .apply()
            _isLoggedIn.value = true
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("E-posta veya şifre hatalı"))
        }
    }

    override suspend fun register(email: String, password: String, fullName: String): Result<Unit> {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .putString(KEY_NAME, fullName)
            .apply()
        _isLoggedIn.value = true
        return Result.success(Unit)
    }

    override suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        // Basit demo senaryosu: doğrulama kodu zaten ViewModel tarafında kontrol edildiği için
        // burada email'e bakmadan şifreyi güncelliyoruz.
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, newPassword)
            .apply()
        return Result.success(Unit)
    }

    override suspend fun logout() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .remove(KEY_EMAIL)
            .remove(KEY_NAME)
            .remove(KEY_PASSWORD)
            .apply()
        _isLoggedIn.value = false
    }
}
