package com.semptom.ai.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semptom.ai.domain.repository.ProfileRepository
import com.semptom.ai.domain.repository.AuthRepository
import com.semptom.ai.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

// UI State Genişletildi
data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val height: Float = 170f, // cm
    val weight: Float = 70f,  // kg
    val bloodType: String? = null,
    val isSaved: Boolean = false
) {
    // Canlı BMI Hesaplama
    val bmi: Float
        get() = if (height > 0) weight / (height / 100).pow(2) else 0f
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            // Gerçek uygulamada bunları da repository'den çekeceksin
            // Şimdilik simüle ediyoruz
            _uiState.value = _uiState.value.copy(
                height = 175f,
                weight = 72f,
                bloodType = "A Rh+"
            )

            profileRepository.getProfile().collect { profile ->
                if (profile != null) {
                    _uiState.value = _uiState.value.copy(profile = profile)
                }
            }
        }
    }

    fun updateHeight(newHeight: Float) {
        _uiState.value = _uiState.value.copy(height = newHeight, isSaved = false)
    }

    fun updateWeight(newWeight: Float) {
        _uiState.value = _uiState.value.copy(weight = newWeight, isSaved = false)
    }

    fun updateBloodType(type: String) {
        _uiState.value = _uiState.value.copy(bloodType = type, isSaved = false)
    }

    fun updateAgeGroup(ageGroup: AgeGroup) {
        _uiState.value = _uiState.value.copy(
            profile = _uiState.value.profile.copy(ageGroup = ageGroup),
            isSaved = false
        )
    }

    fun updateSex(sex: Sex) {
        _uiState.value = _uiState.value.copy(
            profile = _uiState.value.profile.copy(sex = sex),
            isSaved = false
        )
    }

    fun toggleChronicDisease(disease: ChronicDisease) {
        val current = _uiState.value.profile.chronicDiseases.toMutableList()
        if (disease in current) current.remove(disease) else current.add(disease)
        _uiState.value = _uiState.value.copy(
            profile = _uiState.value.profile.copy(chronicDiseases = current),
            isSaved = false
        )
    }

    fun toggleAllergy(allergy: Allergy) {
        val current = _uiState.value.profile.allergies.toMutableList()
        if (allergy in current) current.remove(allergy) else current.add(allergy)
        _uiState.value = _uiState.value.copy(
            profile = _uiState.value.profile.copy(allergies = current),
            isSaved = false
        )
    }

    fun saveProfile() {
        viewModelScope.launch {
            profileRepository.saveProfile(_uiState.value.profile)
            // Burada boy, kilo, kan grubu da kaydedilmeli
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}