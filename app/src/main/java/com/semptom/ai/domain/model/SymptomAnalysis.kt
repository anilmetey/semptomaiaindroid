package com.semptom.ai.domain.model

// Analiz Sonucunu Taşıyan Sınıf (UI için)
data class SymptomAnalysis(
    val title: String,
    val description: String,
    val urgencyLevel: UrgencyLevel,
    val department: String,
    val recommendations: List<String>,
    val detectedKeywords: List<String> = emptyList()
)

// Hastalık Kuralı Tanımı (Bilgi Bankası için)
data class DiseaseRule(
    val id: String,
    val title: String,            // Hastalık Adı
    val relatedKeywords: List<String>, // Bu kelimeler geçerse puan al
    val mustHaveKeywords: List<String> = emptyList(), // Bu kelimelerden en az biri MUTLAKA olmalı
    val description: String,
    val urgency: UrgencyLevel,
    val department: String,
    val recommendations: List<String>
)

enum class UrgencyLevel(val label: String, val colorCode: Long) {
    LOW("Düşük Risk / Evde Takip", 0xFF4CAF50),       // Yeşil
    MODERATE("Orta Risk / Muayene Gerekli", 0xFFFF9800), // Turuncu
    HIGH("Yüksek Risk / Acil Durum", 0xFFF44336),     // Kırmızı
    CRITICAL("🚨 KRİTİK / 112 ACİL", 0xFFD32F2F)      // Koyu Kırmızı
}

