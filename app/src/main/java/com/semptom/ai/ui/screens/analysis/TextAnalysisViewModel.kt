package com.semptom.ai.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semptom.ai.domain.model.SymptomAnalysis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TextAnalysisViewModel @Inject constructor(
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TextAnalysisUiState())
    val uiState: StateFlow<TextAnalysisUiState> = _uiState.asStateFlow()
    
    fun analyzeText(text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Simüle edilmiş yapay zeka analizi (kısa gecikme ile)
                delay(1500)
                val result = simulateAIAnalysis(text)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    result = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Analiz sırasında bir hata oluştu. Lütfen tekrar deneyin."
                )
            }
        }
    }
    
    private fun simulateAIAnalysis(text: String): SymptomAnalysis {
        // Hem Türkçe karakterli metni hem de sadeleştirilmiş halini kullan
        val lower = text.lowercase()
        val normalized = lower
            .replace('ş', 's')
            .replace('ç', 'c')
            .replace('ğ', 'g')
            .replace('ü', 'u')
            .replace('ö', 'o')
            .replace('ı', 'i')

        // Baş ağrısı / migren tablosu
        if (lower.contains("baş ağrısı") ||
            lower.contains("başım ağrıyor") ||
            normalized.contains("bas agrisi") ||
            normalized.contains("basim agri") ||
            normalized.contains("migren") ||
            (normalized.contains("bas") && normalized.contains("agri"))) {
            return SymptomAnalysis(
                title = "Tension Type Headache (Gerilim Tipi Baş Ağrısı)",
                description = "Belirtileriniz gerilim tipi baş ağrısı ile uyumlu görünüyor. Bu en yaygın baş ağrısı türüdür ve genellikle stres, yorgunluk veya uzun süre ekrana bakma gibi nedenlerle ortaya çıkar. Ağrı genellikle başın her iki tarafında baskı veya sıkışma hissi şeklinde tarif edilir.",
                recommendations = listOf(
                    "Gün içinde kısa molalar verip gözlerinizi ve boynunuzu dinlendirin",
                    "Stres yönetimi teknikleri (nefes egzersizi, kısa yürüyüşler) uygulayın",
                    "Yeterli su tüketin ve düzenli uyku düzeni oluşturun",
                    "Basit ağrı kesiciler (parasetamol, ibuprofen) geçici rahatlama sağlayabilir (doktor önerisiyle)",
                    "Ağrılar sıklaşıyor, şiddetleniyor veya görme/konuşma bozukluğu eşlik ediyorsa acil doktora başvurun"
                )
            )
        }

        // Ateş + solunum yolu tablosu
        if (lower.contains("ateş") ||
            lower.contains("öksür") ||
            lower.contains("boğaz ağrısı") ||
            normalized.contains("ates") ||
            normalized.contains("oksur") ||
            normalized.contains("bogaz agri") ||
            normalized.contains("bogazim agri") ||
            normalized.contains("balgam") ||
            normalized.contains("nezle") ||
            normalized.contains("grip")) {
            return SymptomAnalysis(
                title = "Upper Respiratory Tract Infection (Üst Solunum Yolu Enfeksiyonu)",
                description = "Ateş, öksürük, boğaz ağrısı ve/veya balgam gibi belirtileriniz üst solunum yolu enfeksiyonu (grip, soğuk algınlığı vb.) ile uyumlu görünüyor. Bu enfeksiyonlar genellikle viral olup, çoğu zaman istirahat ve destekleyici tedavi ile kendiliğinden düzelir.",
                recommendations = listOf(
                    "Bol sıvı tüketin (su, bitki çayı, çorba)",
                    "Sigara dumanı ve çok kuru ortamdan kaçının",
                    "İstirahat edin, vücudunuza toparlanması için zaman tanıyın",
                    "Gerekirse ateş düşürücü ve ağrı kesicileri doktor önerisiyle kullanın",
                    "Nefes darlığı, göğüs ağrısı, 39°C üzeri ateş veya 3 günden uzun süren yüksek ateş durumunda mutlaka sağlık kuruluşuna başvurun"
                )
            )
        }

        // Mide-bağırsak tablosu
        if (lower.contains("mide") ||
            lower.contains("bulantı") ||
            lower.contains("karın ağrısı") ||
            lower.contains("ishal") ||
            lower.contains("kabız") ||
            normalized.contains("mide") ||
            normalized.contains("bulanti") ||
            normalized.contains("karin agri") ||
            normalized.contains("ishal") ||
            normalized.contains("kabizlik")) {
            return SymptomAnalysis(
                title = "Gastrointestinal Discomfort (Mide-Bağırsak Rahatsızlığı)",
                description = "Mide bulantısı, karın ağrısı, ishal veya kabızlık gibi şikayetleriniz sindirim sistemi ile ilişkili bir rahatsızlığa işaret ediyor olabilir. Çoğu durumda bu tablo diyet hataları, enfeksiyonlar veya hassas bağırsak yapısı ile ilişkilidir.",
                recommendations = listOf(
                    "Yağlı, çok baharatlı ve hazır gıdalardan bir süre uzak durun",
                    "Bol su için, kafein ve gazlı içecekleri sınırlayın",
                    "Sık ama az porsiyonlu, hafif öğünler tercih edin",
                    "Şiddetli karın ağrısı, kanlı dışkılama veya inatçı kusma varsa acil tıbbi değerlendirme gereklidir",
                    "Şikayetler 2–3 günden uzun sürerse aile hekiminize başvurun"
                )
            )
        }

        // Halsizlik / yorgunluk tablosu
        if (lower.contains("halsiz") ||
            lower.contains("yorgun") ||
            lower.contains("enerjim yok") ||
            normalized.contains("halsiz") ||
            normalized.contains("yorgun") ||
            normalized.contains("enerji yok") ||
            normalized.contains("bitkin")) {
            return SymptomAnalysis(
                title = "Fatigue Syndrome (Yorgunluk ve Enerji Düşüklüğü)",
                description = "Süregelen halsizlik ve enerji düşüklüğü; yoğun tempo, yetersiz uyku, beslenme düzensizliği veya stres ile ilişkili olabilir. Bazı durumlarda altta yatan kansızlık, tiroid problemleri gibi tıbbi nedenler de rol oynar.",
                recommendations = listOf(
                    "Her gece 7–9 saat uyumaya ve uyku saatlerinizi düzenli tutmaya çalışın",
                    "Günlük kısa yürüyüşler gibi hafif egzersizleri rutine ekleyin",
                    "Basit ve dengeli beslenmeye, özellikle sebze-meyve ve protein alımına dikkat edin",
                    "Kafein ve enerji içeceklerini azaltın, sigara/alkolden kaçının",
                    "Yorgunluk 2 haftadan uzun sürerse veya kilo kaybı, çarpıntı, nefes darlığı gibi ek şikayetler varsa kan tahlili için doktora başvurun"
                )
            )
        }

        // Duygu durumu / anksiyete / depresif hissetme (psikolojik tablo)
        if (lower.contains("üzgün") ||
            lower.contains("mutsuz") ||
            lower.contains("depresif") ||
            lower.contains("depresyon") ||
            lower.contains("kaygı") ||
            lower.contains("kaygi") ||
            lower.contains("anksiyete") ||
            lower.contains("endişe") ||
            lower.contains("stresli") ||
            normalized.contains("uzgun") ||
            normalized.contains("mutsuz") ||
            normalized.contains("depresif") ||
            normalized.contains("depresyon") ||
            normalized.contains("kaygi") ||
            normalized.contains("anksiyete") ||
            normalized.contains("stresli") ||
            normalized.contains("sicak basmasi") ||
            normalized.contains("panik atak") ||
            normalized.contains("yalniz hissed") ||
            normalized.contains("degersiz hissed") ||
            normalized.contains("hicbir sey yapmak istemiyorum")) {
            return SymptomAnalysis(
                title = "Anxiety / Mood Related Symptoms (Kaygı ve Duygu Durumu İle İlgili Belirtiler)",
                description = "Kendinizi uzun süredir mutsuz, kaygılı veya gergin hissettiğinizi tarif ediyorsunuz. Böyle hissetmek zaman zaman herkesin yaşayabileceği bir durumdur; ancak duygularınızın yoğunluğu ve süresi arttığında bu, yaşam kalitenizi belirgin şekilde etkileyebilir. Hissettiklerinizin " +
                        "gerçek ve önemli olduğunu, yalnız olmadığınızı ve bunun için yardım isteyebileceğinizi bilmeniz çok değerli.",
                recommendations = listOf(
                    "Gün içinde duygularınızı bastırmak yerine güven duyduğunuz biriyle (arkadaş, aile üyesi) paylaşmaya çalışın",
                    "Kendinizi zorlamadan, kısa yürüyüş, nefes egzersizleri veya sakinleştirici rutinler (ılık duş, müzik, günce tutma) eklemeyi deneyin",
                    "Uyku ve beslenme düzeninizi olabildiğince sabit tutmaya çalışın; çok uzun süre aç kalmamaya ve aşırı kafeinden kaçınmaya özen gösterin",
                    "Duygusal yükünüzü tek başınıza taşımak zorunda değilsiniz; bir psikolog veya psikiyatristten profesyonel destek almak iyileşme sürecini hızlandırabilir",
                    "Kendinize zarar verme düşünceleri, yoğun çaresizlik veya umutsuzluk hissi varsa, beklemeden en yakın acil servise başvurun veya bir kriz hattından destek isteyin"
                )
            )
        }

        // Nefes darlığı / göğüs ağrısı tablosu (uyarı içeren)
        if (lower.contains("nefes darlığı") ||
            lower.contains("nefes alamıyorum") ||
            lower.contains("göğüs ağrısı") ||
            lower.contains("göğsümde sıkışma") ||
            lower.contains("göğsüm sıkışıyor") ||
            lower.contains("çarpıntı") ||
            normalized.contains("nefes darligi") ||
            normalized.contains("nefes alam") ||
            normalized.contains("gogus agrisi")) {
            return SymptomAnalysis(
                title = "Respiratory / Cardiac Alert (Solunum veya Kalp Kaynaklı Şikayetler)",
                description = "Nefes darlığı ve göğüs ağrısı tarifiniz, solunum sistemi veya kalp-damar sistemi ile ilişkili daha ciddi bir tabloya işaret ediyor olabilir. Bu tür şikayetler bazı durumlarda acil değerlendirme gerektirir.",
                recommendations = listOf(
                    "Dinlenme halindeyken dahi nefes darlığı veya göğüs ağrısı varsa vakit kaybetmeden acil servise başvurun",
                    "Ağrı kola, çeneye, sırta yayılıyorsa, soğuk terleme veya bulantı eşlik ediyorsa kalp krizi açısından acil değerlendirme gerekir",
                    "Sigara içiyorsanız bırakmayı düşünün ve tetikleyen durumları (merdiven, ağır efor vb.) not edin",
                    "Daha önce tanı almış kalp-akciğer hastalığınız varsa, mevcut ilaçlarınızı düzenli kullanın ve hekiminizle iletişimde olun"
                )
            )
        }

        // Buraya kadar hiçbir tabloya girmediyse, metni daha genel değerlendirelim
        val healthKeywords = listOf(
            "baş", "ağrı", "ates", "ateş", "öksür", "mide", "karın", "ishal", "kabız",
            "halsiz", "yorgun", "nefes", "göğüs", "gogus", "kaygı", "kaygi", "depresyon",
            "üzgün", "mutsuz", "anksiyete", "stres"
        )
        val hasHealthWords = healthKeywords.any { kw -> lower.contains(kw) || normalized.contains(kw) }

        // Hiç sağlıkla ilgili ipucu yoksa: analiz edilemedi
        if (!hasHealthWords) {
            return SymptomAnalysis(
                title = "Analiz Edilemedi",
                description = "Girdiğiniz metin net bir sağlık şikayeti veya tıbbi durumu tarif etmiyor gibi görünüyor. Bu nedenle, elimizdeki bilgilerle anlamlı bir tıbbi analiz yapmak mümkün olmadı.",
                recommendations = listOf(
                    "Şikayetinizi mümkün olduğunca net ve ayrıntılı şekilde tarif etmeye çalışın (örnek: '3 gündür baş ağrım var ve ateşim 38 derece civarında').",
                    "Vücutta hissettiğiniz ağrı, ateş, nefes darlığı, sindirim sorunları veya ruh hali değişimleri gibi belirtileri ayrı ayrı yazmanız analizi kolaylaştırır.",
                    "Eğer ciddi bir şikayetiniz olduğunu düşünüyorsanız, sadece bu uygulamaya güvenmeyin ve bir sağlık profesyoneline başvurun."
                )
            )
        }

        // Genel durum – sağlıkla ilgili ama spesifik tabloya tam oturmuyorsa
        return SymptomAnalysis(
            title = "General Health Assessment (Genel Sağlık Değerlendirmesi)",
            description = "Paylaştığınız şikayetler birden fazla sistemi ilgilendiren, net bir gruba tam oturmayan genel bir tabloya işaret ediyor olabilir. Bu uygulama tanı koymaz; ancak belirtilerinizi düzenli takip etmeniz ve gerekirse bir sağlık profesyoneline başvurmanız önemlidir.",
            recommendations = listOf(
                "Şikayetlerinizi tarih ve şiddet olarak not alın; hangi durumlarda arttığını gözlemleyin",
                "Uyku, beslenme, sıvı tüketimi ve stres düzeyinizi iyileştirmeye yönelik küçük adımlar atın",
                "Şikayetleriniz 5–7 günden uzun sürerse veya günlük yaşamınızı belirgin etkiliyorsa aile hekiminize başvurun",
                "Ani başlayan, çok şiddetli ağrı, nefes darlığı, bilinç değişikliği gibi alarm bulgularında acil servise gidin"
            )
        )
    }
}

data class TextAnalysisUiState(
    val isLoading: Boolean = false,
    val result: SymptomAnalysis? = null,
    val error: String? = null
)
