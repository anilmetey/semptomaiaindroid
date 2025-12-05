package com.semptom.ai.ui.screens.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semptom.ai.data.email.EmailService
import com.semptom.ai.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val isCodeVerified: Boolean = false,
    val isPasswordReset: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val emailService: EmailService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    // Doğrulama kodları: her e-posta için birden çok aktif kod tutulabilir
    private val verificationCodes = mutableMapOf<String, MutableList<String>>()
    
    init {
        viewModelScope.launch {
            authRepository.isLoggedIn.collect { isLoggedIn ->
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = authRepository.login(email, password)
            
            result.fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Giriş başarısız"
                        )
                    }
                }
            )
        }
    }
    
    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = authRepository.register(email, password, fullName)
            
            result.fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Kayıt başarısız"
                        )
                    }
                }
            )
        }
    }
    
    fun sendVerificationCode(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isCodeVerified = false, isPasswordReset = false) }
            try {
                val code = (100000..999999).random().toString()
                val codes = verificationCodes.getOrPut(email) { mutableListOf() }
                codes.add(code)
                Log.d("AuthViewModel", "Generated code for $email: $code")
                
                val emailSent = emailService.sendVerificationCode(email, code)
                if (emailSent) {
                    _uiState.update { it.copy(isLoading = false, error = null) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "E-posta gönderilemedi. Lütfen daha sonra tekrar deneyin.") }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email send error", e)
                _uiState.update { it.copy(isLoading = false, error = "E-posta gönderme hatası: ${e.message}") }
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "E-posta gönderme hatası: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun verifyCode(email: String, code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            delay(500) // Simulate API call
            
            val storedCodes = verificationCodes[email] ?: emptyList()
            
            if (storedCodes.contains(code)) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isCodeVerified = true,
                        error = null
                    )
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Geçersiz doğrulama kodu"
                    )
                }
            }
        }
    }
    
    fun resetPassword(email: String, code: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            delay(1000) // Simulate API call
            
            val storedCodes = verificationCodes[email] ?: emptyList()
            
            if (storedCodes.contains(code)) {
                // Simulate password reset
                val result = authRepository.resetPassword(email, newPassword)
                
                result.fold(
                    onSuccess = {
                        // Başarılı şifre sıfırlamadan sonra bu e-posta için tüm kodları temizle
                        verificationCodes.remove(email)
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isPasswordReset = true,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Şifre sıfırlama başarısız"
                            )
                        }
                    }
                )
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Geçersiz doğrulama kodu"
                    )
                }
            }
        }
    }
    
    fun resendCode(email: String) {
        sendVerificationCode(email)
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedIn = false) }
        }
    }
}
