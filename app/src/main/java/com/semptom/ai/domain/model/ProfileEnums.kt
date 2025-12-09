package com.semptom.ai.domain.model

// --- 1. ENUM SINIFLARI (Senin belirlediğin detaylı yapı) ---

enum class AgeGroup(val displayName: String) {
    CHILD("Çocuk"),
    TEEN("Genç"),
    ADULT("Yetişkin"),
    SENIOR("Yaşlı")
}

enum class Sex(val displayName: String) {
    MALE("Erkek"),
    FEMALE("Kadın"),
    OTHER("Diğer")
}

enum class ChronicDisease(val displayName: String) {
    DIABETES("Diyabet"),
    HYPERTENSION("Hipertansiyon"),
    HEART_DISEASE("Kalp Hastalığı"),
    ASTHMA("Astım"),
    COPD("KOAH"),
    KIDNEY_DISEASE("Böbrek Hastalığı"),
    LIVER_DISEASE("Karaciğer Hastalığı"),
    NONE("Yok") // UI'da özel işlem görüyor (Yeşil renk oluyor)
}

enum class Allergy(val displayName: String) {
    DRUG("İlaç Alerjisi"),
    FOOD("Gıda Alerjisi"),
    POLLEN("Polen Alerjisi"),
    DUST("Toz Alerjisi"),
    ANIMAL("Hayvan Alerjisi"),
    OTHER("Diğer"),
    NONE("Yok") // UI'da özel işlem görüyor
}

// --- 2. DATA CLASS'LAR (State Yönetimi için Gerekli Kısım) ---

// ViewModel'in UI'ya gönderdiği anlık durum
data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val height: Float = 170f,
    val weight: Float = 70f,
    val bmi: Float = 24.2f, // İstersen kaldırabilirsin ama backend için dursun
    val bloodType: String = "A Rh+",
    val profile: UserProfile = UserProfile()
)

// Kullanıcının veritabanına kaydedilecek asıl profili
