package com.semptom.ai.domain.model

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
    NONE("Yok")
}

enum class Allergy(val displayName: String) {
    DRUG("İlaç Alerjisi"),
    FOOD("Gıda Alerjisi"),
    POLLEN("Polen Alerjisi"),
    DUST("Toz Alerjisi"),
    ANIMAL("Hayvan Alerjisi"),
    OTHER("Diğer"),
    NONE("Yok")
}
