package com.semptom.ai.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.random.Random

// --- DATA CLASSES FOR UI ---

data class AnalysisDashboardState(
    val isLoading: Boolean = true,
    val userName: String = "Kullanıcı",
    val greetingMessage: String = "",
    val dailyHealthTip: HealthTip? = null,
    val weatherHealthImpact: String = "Yüksek Basınç: Baş ağrısı riski",
    val recentActivities: List<AnalysisHistoryItem> = emptyList(),
    val healthScore: Int = 85, // 0-100 arası genel sağlık skoru simülasyonu
    val activeAlerts: Int = 0 // Okunmamış uyarı sayısı
)

data class HealthTip(
    val title: String,
    val content: String,
    val category: String, // Beslenme, Psikoloji, Fiziksel
    val iconId: Int = 0
)

data class AnalysisHistoryItem(
    val id: String,
    val title: String,
    val date: String,
    val riskLevel: String, // Düşük, Orta, Yüksek
    val type: AnalysisType
)

enum class AnalysisType {
    TEXT, MANUEL, VOICE, CAMERA
}

@HiltViewModel
class AnalysisViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisDashboardState())
    val uiState: StateFlow<AnalysisDashboardState> = _uiState.asStateFlow()

    init {
        initializeDashboard()
    }

    private fun initializeDashboard() {
        viewModelScope.launch {
            // 1. Yükleniyor durumu
            _uiState.update { it.copy(isLoading = true) }

            // Simüle edilmiş ağ gecikmesi
            delay(1500)

            // 2. Zaman bazlı karşılama mesajı
            val hour = LocalDateTime.now().hour
            val greeting = when (hour) {
                in 5..11 -> "Günaydın,"
                in 12..17 -> "İyi Günler,"
                in 18..22 -> "İyi Akşamlar,"
                else -> "İyi Geceler,"
            }

            // 3. Rastgele Sağlık İpucu Seçimi
            val randomTip = healthTipsPool.random()

            // 4. Mock Geçmiş Verisi (Normalde DB'den gelir)
            val history = listOf(
                AnalysisHistoryItem("1", "Baş Ağrısı ve Mide", "Bugün, 10:30", "Orta", AnalysisType.TEXT),
                AnalysisHistoryItem("2", "Genel Kontrol", "Dün, 14:20", "Düşük", AnalysisType.MANUEL),
                AnalysisHistoryItem("3", "Cilt Döküntüsü", "01 Ara, 09:15", "Yüksek", AnalysisType.TEXT),
                AnalysisHistoryItem("4", "Anksiyete Belirtileri", "28 Kas, 23:00", "Düşük", AnalysisType.VOICE)
            )

            // 5. State Güncelleme
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userName = "Mete", // Kullanıcı adını buradan çekebilirsin
                    greetingMessage = greeting,
                    dailyHealthTip = randomTip,
                    recentActivities = history,
                    healthScore = Random.nextInt(75, 98),
                    activeAlerts = 2
                )
            }
        }
    }

    fun refreshDashboard() {
        initializeDashboard()
    }

    fun dismissTip() {
        _uiState.update { it.copy(dailyHealthTip = null) }
    }

    // --- MOCK DATA POOL ---
    private val healthTipsPool = listOf(
        HealthTip("Su Tüketimi", "Baş ağrılarınızın %60'ı susuzluktan kaynaklanabilir. Günde en az 2.5 litre su içmeyi unutmayın.", "Fiziksel"),
        HealthTip("Mavi Işık", "Uyumadan 1 saat önce ekranlara bakmayı bırakmak, melatonin salgısını %40 artırır.", "Uyku"),
        HealthTip("Duruş Bozukluğu", "Telefonunuza bakarken boynunuzu 60 derece eğmek, omurgaya 27 kg yük bindirir.", "Fiziksel"),
        HealthTip("Zihinsel Mola", "Pomodoro tekniği ile çalışmak, tükenmişlik sendromu riskini azaltır.", "Psikoloji"),
        HealthTip("Genetik Farkındalık", "Ailenizde kalp rahatsızlığı varsa, 30 yaşından sonra yıllık EKG çektirmelisiniz.", "Kader/Genetik")
    )
}