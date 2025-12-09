package com.semptom.ai.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semptom.ai.domain.model.DiseaseRule
import com.semptom.ai.domain.model.SymptomAnalysis
import com.semptom.ai.domain.model.UrgencyLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.util.Locale
import android.util.Log
// ============================================================================================
// UI STATE DEFINITIONS
// ============================================================================================

data class TextAnalysisUiState(
    val isLoading: Boolean = false,
    val result: SymptomAnalysis? = null,
    val error: String? = null,
    val debugInfo: String? = null
)

// ============================================================================================
// VIEW MODEL
// ============================================================================================

@HiltViewModel
class TextAnalysisViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TextAnalysisUiState())
    val uiState: StateFlow<TextAnalysisUiState> = _uiState.asStateFlow()

    private val knowledgeBase by lazy { MedicalKnowledgeBase.getAllRules() }

    fun analyzeText(inputText: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null, result = null)

                delay(1500)

                val validatedInput = validateInput(inputText)
                    ?: throw IllegalArgumentException("Lütfen şikayetlerinizi daha detaylı, en az birkaç kelime ile anlatın.")

                Log.d("TextAnalysis", "Input validated: ${validatedInput.length} chars")

                val normalizedTokens = TextProcessor.normalizeAndTokenize(validatedInput)
                Log.d("TextAnalysis", "Tokens: ${normalizedTokens.size}")

                val analysisResult = ScoringEngine.findBestMatch(normalizedTokens, knowledgeBase)

                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        result = analysisResult
                    )
                    Log.d("TextAnalysis", "Analysis complete: ${analysisResult.title}")
                }

            } catch (e: Exception) {
                Log.e("TextAnalysis", "Error during analysis", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Beklenmedik bir hata oluştu."
                    )
                }
            }
        }
    }

    private fun validateInput(text: String): String? {
        val trimmed = text.trim()
        return if (trimmed.length >= 4) trimmed else null
    }
}

// ============================================================================================
// NLP ENGINE (TEXT PROCESSOR)
// ============================================================================================

object TextProcessor {

    private val TURKISH_STOP_WORDS = setOf(
        "ve", "ile", "bir", "var", "yok", "biraz", "çok",
        "ben", "sen", "o", "biz", "siz", "onlar",
        "bu", "şu", "o", "benim", "senin", "onun", "bizim", "sizin",
        "de", "da", "ama", "ancak", "çünkü", "ise", "mi", "mı", "mu", "mü",
        "için", "kadar", "değil", "daha", "çok", "az",
        "edi", "dir", "dur", "sun", "ti", "te", "yor", "iyor",
        "olacak", "olan", "olarak", "olur", "olmak"
    )

    fun normalizeAndTokenize(text: String): List<String> {
        Log.d("TextProcessor", "Input: ${text.take(50)}...")

        val lowerCase = text.lowercase(Locale("tr", "TR"))
        val normalized = normalizeCharacters(lowerCase)
        val noApostrophe = normalized.replace("'", "").replace("'", "")
        val cleanText = noApostrophe.replace(Regex("[^a-zçğıöşüñ ]"), " ")

        val tokens = cleanText.split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.length > 2 && !TURKISH_STOP_WORDS.contains(it) }

        Log.d("TextProcessor", "Final tokens: ${tokens.size}")
        return tokens
    }

    private fun normalizeCharacters(text: String): String {
        val sb = StringBuilder()
        for (char in text) {
            sb.append(when (char) {
                'ş' -> 's'
                'ç' -> 'c'
                'ğ' -> 'g'
                'ü' -> 'u'
                'ö' -> 'o'
                'ı' -> 'i'
                'î' -> 'i'
                'â' -> 'a'
                'ũ' -> 'u'
                'õ' -> 'o'
                else -> char
            })
        }
        return sb.toString()
    }
}

// ============================================================================================
// SCORING ENGINE (MATCHING ALGORITHM)
// ============================================================================================

object ScoringEngine {

    private object Constants {
        // Scoring weights
        const val KEYWORD_MATCH = 10.0
        const val MUST_HAVE_MATCH = 50.0
        const val SEVERITY_BONUS = 30.0
        
        // Thresholds
        const val THRESHOLD_STRICT = 30.0
        const val THRESHOLD_LOOSE = 15.0
        
        // Fuzzy matching
        const val MIN_WORD_LENGTH_FOR_FUZZY = 4
        const val MAX_LEVENSHTEIN_DISTANCE = 1
        
        // Limits
        const val MAX_MISSING_KEYWORDS = 3
        const val MAX_RESULTS = 5
    }

    private val severityModifiers = setOf(
        "şiddetli", "dayanılmaz", "ölümcül", "ani", "patlayıcı",
        "kıvrandıran", "bayıltan", "bıçak", "nefes"
    )

    fun findBestMatch(userTokens: List<String>, rules: List<DiseaseRule>): SymptomAnalysis {
        Log.d("ScoringEngine", "Analyzing ${rules.size} rules...")

        val severityScore = calculateSeverity(userTokens)
        val isSevereCase = severityScore > 0

        val userTokenSet = userTokens.toHashSet()
        Log.d("ScoringEngine", "User tokens: ${userTokenSet.size}")

        val scoredDiseases = rules.mapNotNull { rule ->
            scoreDisease(rule, userTokens, userTokenSet, isSevereCase)
        }
            .sortedByDescending { it.score }
            .take(5)

        Log.d("ScoringEngine", "Scored diseases: ${scoredDiseases.size}")

        if (scoredDiseases.isEmpty()) {
            Log.d("ScoringEngine", "No match found, using fallback")
            return createSmartFallbackAnalysis(userTokens)
        }

        val primaryDiagnosis = scoredDiseases.first()
        val differentialDiagnosis = if (scoredDiseases.size > 1 &&
            scoredDiseases[1].percentage > 40.0) {
            scoredDiseases[1]
        } else null

        Log.d("ScoringEngine", "Primary: ${primaryDiagnosis.rule.title} (${primaryDiagnosis.percentage}%)")

        val finalReport = generateClinicalReport(
            primary = primaryDiagnosis,
            secondary = differentialDiagnosis,
            severityMode = isSevereCase
        )

        val finalUrgency = if (isSevereCase &&
            primaryDiagnosis.rule.urgency == UrgencyLevel.LOW) {
            UrgencyLevel.MODERATE
        } else {
            primaryDiagnosis.rule.urgency
        }

        return SymptomAnalysis(
            title = primaryDiagnosis.rule.title,
            description = finalReport,
            urgencyLevel = finalUrgency,
            department = primaryDiagnosis.rule.department,
            recommendations = primaryDiagnosis.rule.recommendations,
            detectedKeywords = primaryDiagnosis.matches
        )
    }

    data class ScoredDisease(
        val rule: DiseaseRule,
        val score: Double,
        val percentage: Double,
        val matches: List<String>,
        val missing: List<String>
    )

    private fun calculateSeverity(tokens: List<String>): Int {
        return tokens.count { token ->
            severityModifiers.any { modifier ->
                token == modifier ||
                        (token.length >= 4 && modifier.length >= 4 &&
                                token.contains(modifier))
            }
        }
    }

    private fun scoreDisease(
        rule: DiseaseRule,
        userTokens: List<String>,
        userTokenSet: Set<String>,
        isSevere: Boolean
    ): ScoredDisease? {
        require(rule.relatedKeywords.isNotEmpty() || rule.mustHaveKeywords.isNotEmpty()) {
            "DiseaseRule must have at least one keyword"
        }

        var score = 0.0
        val matches = mutableSetOf<String>()
        
        // Check must-have keywords first
        val matchedMustHaves = rule.mustHaveKeywords.filter { keyword ->
            userTokenSet.any { token -> isTokenMatch(token, keyword) }?.also { if (it) matches.add(keyword) } ?: false
        }
        
        // If there are must-have keywords and none matched, return early
        if (rule.mustHaveKeywords.isNotEmpty() && matchedMustHaves.isEmpty()) {
            Log.d("ScoringEngine", "${rule.title}: Missing critical keyword")
            return null
        }
        
        // Score must-have matches
        score += matchedMustHaves.size * Constants.MUST_HAVE_MATCH
        
        // Score related keywords
        val matchedRelated = rule.relatedKeywords.filter { keyword ->
            userTokenSet.any { token -> isTokenMatch(token, keyword) }?.also { if (it) matches.add(keyword) } ?: false
        }
        score += matchedRelated.size * Constants.KEYWORD_MATCH
        
        // Apply severity bonus if applicable
        if (isSevere && rule.urgency in listOf(UrgencyLevel.HIGH, UrgencyLevel.CRITICAL)) {
            score += Constants.SEVERITY_BONUS
        }
        
        // Calculate percentage score
        val totalPossible = (rule.relatedKeywords.size * Constants.KEYWORD_MATCH +
                           rule.mustHaveKeywords.size * Constants.MUST_HAVE_MATCH).coerceAtLeast(1.0)
        val percentage = ((score / totalPossible) * 100.0).coerceIn(0.0, 100.0)
        
        // Apply threshold
        if (percentage < Constants.THRESHOLD_STRICT) return null
        
        // Find missing keywords
        val allKeywords = (rule.mustHaveKeywords + rule.relatedKeywords).toSet()
        val missingKeywords = (allKeywords - matches)
            .shuffled()
            .take(Constants.MAX_MISSING_KEYWORDS)
            .toList()

        Log.d("ScoringEngine", "${rule.title}: Score=$score, %=$percentage, Matches=${matches.size}")
        
        return ScoredDisease(rule, score, percentage, matches.toList(), missingKeywords)
    }
    
    private fun isTokenMatch(token: String, keyword: String): Boolean {
        return token == keyword || fuzzyMatch(token, keyword) == true
    }

    private fun fuzzyMatch(token: String, keyword: String): Boolean? {
        if (token == keyword) return true
        
        // Only perform fuzzy matching on words of sufficient length
        if (token.length < Constants.MIN_WORD_LENGTH_FOR_FUZZY || 
            keyword.length < Constants.MIN_WORD_LENGTH_FOR_FUZZY) {
            return false
        }
        
        return levenshteinDistance(token, keyword) <= Constants.MAX_LEVENSHTEIN_DISTANCE
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }

        return dp[s1.length][s2.length]
    }

    private fun generateClinicalReport(
        primary: ScoredDisease,
        secondary: ScoredDisease?,
        severityMode: Boolean
    ): String {
        val confidence = "Güven Skoru: %${primary.percentage.toInt()}"
        val severityWarning = if (severityMode)
            "\n⚠️ HASTA ŞİDDETLİ AGRI/SEMPTOM BİLDİRMİŞTİR." else ""

        val anamnesis = """
TIBBI DEĞERLENDİRME RAPORU
------------------------------------------------
$confidence $severityWarning

BULGULAR:
Hastanın şikayetleri arasında "${primary.matches.joinToString(", ").uppercase()}" semptomları tespit edilmiş 
ve klinik tablo ile korelasyon saptanmıştır.
        """.trimIndent()

        val diagnosis = """
        
ÖN TANI: ${primary.rule.title}
${primary.rule.description}
        """.trimIndent()

        val differential = if (secondary != null) {
            """
            
AYIRICI TANI (DİKKAT):
Klinik tablo %${secondary.percentage.toInt()} olasılıkla "${secondary.rule.title}" 
ile de karıştırılabilir.
            """.trimIndent()
        } else "\nAYIRICI TANI: Belirtiler spesifik bir tabloyu işaret ettiğinden, ikinci bir güçlü şüphe bulunamamıştır."

        val action = """
        
------------------------------------------------
YÖNLENDIRME VE AKSIYON PLANI:
${getProfessionalAdvice(primary.rule.urgency, primary.rule.department)}
        """.trimIndent()

        return anamnesis + diagnosis + differential + action
    }

    private fun getProfessionalAdvice(urgency: UrgencyLevel, dept: String): String {
        return when (urgency) {
            UrgencyLevel.CRITICAL -> "🚨 KIRMIZI ALAN: Hayati risk taşıyan bulgular mevcuttur. Vakit kaybetmeden ACİL SERVISE başvurunuz. (112)"
            UrgencyLevel.HIGH -> "⚠️ SARI ALAN: Ciddi klinik tablo. En kısa sürede $dept polikliğine başvurunuz."
            UrgencyLevel.MODERATE -> "ℹ️ YEŞİL ALAN: Şikayetler 48 saat içinde gerilemezse $dept uzmanına başvurunuz."
            UrgencyLevel.LOW -> "✅ BEYAZ ALAN: Semptomatik tedavi ve istirahat önerilir."
        }
    }

    private fun createSmartFallbackAnalysis(tokens: List<String>): SymptomAnalysis {
        val psychoKeys = listOf("mutsuz", "stres", "bunal", "ağla", "üzgün", "korku", "endişe", "panik")
        if (tokens.any { t -> psychoKeys.any { k -> t.contains(k) } }) {
            return SymptomAnalysis(
                title = "Psikosomatik Değerlendirme",
                description = """
KLINIK DEĞERLENDİRME RAPORU
------------------------------------------------
BULGULAR: Organik bir patolojiden ziyade, anksiyete ve stres kaynaklı semptomlar gözlenmektedir.

DEĞERLENDIRME:
Vücudunuzdaki fiziksel belirtiler (çarpıntı, daralma hissi vb.) yüksek stres altında tetiklenen tepkisi olabilir.

ÖNERI:
Vital bulgularınız normalse, önce sakinleşmeyi deneyin. Şikayetler fiziksel ağrıya dönüşürse doktora başvurun.
                """.trimIndent(),
                urgencyLevel = UrgencyLevel.LOW,
                department = "Psikiyatri / Psikoloji",
                recommendations = listOf("Nefes egzersizi", "Uyaranlardan uzaklaşma"),
                detectedKeywords = listOf("Psikolojik Bulgular")
            )
        }

        val bodyParts = mapOf(
            "baş" to "Nöroloji", "kafa" to "Nöroloji",
            "mide" to "Gastroenteroloji", "karın" to "Genel Cerrahi",
            "göz" to "Göz Hastalıkları", "kalp" to "Kardiyoloji",
            "göğüs" to "Göğüs Hastalıkları", "bacak" to "Ortopedi"
        )

        for ((part, dept) in bodyParts) {
            if (tokens.any { it.contains(part) }) {
                return SymptomAnalysis(
                    title = "Bölgesel Semptom Analizi",
                    description = """
📍 BÖLGESEL YÖNLENDİRME:
Sorunun "$part" bölgesinde olduğu tespit edildi. "$dept" uzmanına başvurunuz.
                    """.trimIndent(),
                    urgencyLevel = UrgencyLevel.LOW,
                    department = dept,
                    recommendations = listOf("Semptom takibi", "Uzman görüşü"),
                    detectedKeywords = listOf(part)
                )
            }
        }

        return SymptomAnalysis(
            title = "Tanımlanamayan Klinik Tablo",
            description = """
❓ ANALİZ SONUCU:
Belirtileriniz veritabanımdaki hastalık profilleriyle net eşleşme sağlamadı.
Lütfen daha detaylı (yer, süre, şiddet) anlatarak tekrar deneyin.
            """.trimIndent(),
            urgencyLevel = UrgencyLevel.LOW,
            department = "Aile Hekimliği",
            recommendations = listOf("Detaylı anamnez girişi", "İstirahat"),
            detectedKeywords = emptyList()
        )
    }
}


// ============================================================================================
// MEDICAL KNOWLEDGE BASE (DATA REPOSITORY)
// ============================================================================================
// ============================================================================================
// MEDICAL KNOWLEDGE BASE (MASTER DATA REPOSITORY)
// ============================================================================================

object MedicalKnowledgeBase {

    fun getAllRules(): List<DiseaseRule> {
        val rules = mutableListOf<DiseaseRule>()

        // ===========================================================================
        // BÖLÜM 1: TEMEL VE ACİL TIP (ÖNCEKİ KODLAR)
        // ===========================================================================

        // 1. KATEGORİ: ACİL VE KRİTİK DURUMLAR
        rules.add(DiseaseRule(
            id = "EMERGENCY_HEART_ATTACK",
            title = "Miyokard Enfarktüsü (Kalp Krizi) Şüphesi",
            relatedKeywords = listOf("gogus", "agri", "sol kol", "baski", "fil", "oturmus", "sikisma", "terleme", "soguk", "nefes", "cene", "sirt", "mide", "fenalik", "bayilma"),
            mustHaveKeywords = listOf("gogus", "kalp", "sol"),
            description = "Göğüs kemiği arkasında baskı hissi, sol kola, boyna veya çeneye yayılan ağrı, soğuk terleme ve ölüm korkusu kalp krizinin tipik belirtileridir.",
            urgency = UrgencyLevel.CRITICAL,
            department = "ACİL SERVİS (112)",
            recommendations = listOf("DERHAL 112'yi arayın.", "Hastayı hareket ettirmeyin, yere yarı oturur pozisyonda uzandırın.", "Kravat vb. sıkı giysileri gevşetin.", "Varsa bir adet Aspirin çiğnetin.", "Öksürtmeye çalışın.")
        ))

        rules.add(DiseaseRule(
            id = "EMERGENCY_STROKE",
            title = "Serebrovasküler Olay (İnme/Felç)",
            relatedKeywords = listOf("konusma", "peltek", "anlamsiz", "yuz", "kayma", "egrilme", "sarkma", "kol", "bacak", "gucsuzluk", "tutmamasi", "hissizlik", "tek taraf", "denge", "gorme"),
            mustHaveKeywords = listOf("konusma", "yuz", "kol", "felc"),
            description = "Ani gelişen konuşma bozukluğu, yüzde kayma, tek taraflı kol/bacak güçsüzlüğü inme belirtisidir. İlk 4.5 saat müdahale için hayati önem taşır.",
            urgency = UrgencyLevel.CRITICAL,
            department = "NÖROLOJİ / ACİL",
            recommendations = listOf("Zaman kaybetmeden 112'yi arayın (Zaman = Beyin).", "Hastaya su veya yemek VERMEYİN (yutma refleksi gitmiş olabilir).", "Hastayı yan yatırın.", "Belirtilerin saat kaçta başladığını not edin.")
        ))
        rules.add(DiseaseRule(
            id = "INF_GASTROENTERITIS",
            title = "Viral Gastroenterit (Mide Üşütmesi)",
            relatedKeywords = listOf("ishal", "kusma", "bulanti", "karin", "agrisi", "halsizlik", "ates", "su", "kaybi", "usutme", "salgin"),
            mustHaveKeywords = listOf("ishal", "kusma", "bulanti"),
            description = "Halk arasında 'Mide Üşütmesi' olarak bilinen, virüs kaynaklı mide ve bağırsak iltihabı. İshal ve kusmanın en yaygın sebebidir.",
            urgency = UrgencyLevel.LOW,
            department = "AİLE HEKİMİ / DAHİLİYE",
            recommendations = listOf(
                "En önemli şey sıvı kaybını önlemektir (Bol su, ayran, tuzlu kraker).",
                "Haşlanmış patates, muz ve pirinç lapası tüketin.",
                "Antibiyotik işe yaramaz (Virüstür), kendiliğinden geçer.",
                "Dışkıda kan görürseniz veya ateş düşmezse doktora gidin."
            )
        ))

        // 2. GIDA ZEHİRLENMESİ (Güncellendi)
        rules.add(DiseaseRule(
            id = "GEN_FOOD_POISON",
            title = "Gıda Zehirlenmesi",
            relatedKeywords = listOf("bozuk", "yemek", "tavuk", "mayonez", "kusma", "ishal", "ani", "baslayan", "karin", "krampi", "restoran", "disaridan"),
            mustHaveKeywords = listOf("yemek", "bozuk", "kusma", "ani"),
            description = "Bozuk veya bakterili gıda tüketiminden 1-6 saat sonra aniden başlayan kusma ve ishal durumu.",
            urgency = UrgencyLevel.MODERATE,
            department = "ACİL SERVİS / DAHİLİYE",
            recommendations = listOf(
                "Kusmayı durdurmaya çalışmayın, vücut toksini atıyor.",
                "Yudum yudum su için.",
                "Şikayetler 24 saati geçerse serum takılması gerekebilir."
            )
        ))

        // 3. ÇÖLYAK (ZORLAŞTIRILDI - Artık sadece 'ishal' diyince çıkmayacak)
        rules.add(DiseaseRule(
            id = "GASTRO_CELIAC",
            title = "Çölyak Hastalığı (Gluten Hassasiyeti)",
            // 'ishal' kelimesini related'da bıraktık ama mustHave'den çıkardık.
            // Tetiklenmesi için 'gluten', 'ekmek', 'hamur' veya 'kronik' denmesi gerekecek.
            relatedKeywords = listOf("ishal", "karin", "siskinlik", "kilo", "kaybi", "kansizlik", "cocukluktan", "beri", "gaz"),
            mustHaveKeywords = listOf("gluten", "ekmek", "hamur", "kronik", "bugday"),
            description = "Gluten proteinine karşı ömür boyu süren hassasiyet. Genellikle kronik (uzun süreli) şikayetlerdir.",
            urgency = UrgencyLevel.LOW,
            department = "GASTROENTEROLOJİ",
            recommendations = listOf("Glutensiz diyet şarttır.", "Kan testi ve endoskopi ile tanı konur.", "Ailenizde var mı sorgulayın.")
        ))

        // 4. İRRİTABL BAĞIRSAK (IBS)
        rules.add(DiseaseRule(
            id = "GASTRO_IBS",
            title = "Hassas Bağırsak Sendromu (IBS)",
            relatedKeywords = listOf("kabizlik", "ishal", "degismeli", "stres", "gaz", "siskinlik", "kramp", "tuvalet", "sonrasi", "rahatlama"),
            mustHaveKeywords = listOf("stres", "gaz", "degismeli"),
            description = "Stresle tetiklenen, bağırsak alışkanlıklarında değişim (bir ishal bir kabız) yapan kronik durum.",
            urgency = UrgencyLevel.LOW,
            department = "GASTROENTEROLOJİ",
            recommendations = listOf("Stresten uzak durun.", "Gaz yapan yiyecekleri (baklagil) azaltın.", "Probiyotik kullanabilirsiniz.")
        ))

        // 5. KOLON KANSERİ (ZORLAŞTIRILDI - Sadece 'kan' veya 'kilo kaybı' varsa çıkacak)
        rules.add(DiseaseRule(
            id = "ONCO_COLON",
            title = "Kolorektal (Bağırsak) Rahatsızlık Şüphesi",
            relatedKeywords = listOf("diski", "kan", "siyah", "kilo", "kaybi", "ince", "diski", "kansizlik", "ailede", "kanser"),
            mustHaveKeywords = listOf("kan", "kilo", "siyah"), // Kritik kelimeler
            description = "Dışkıda kan görülmesi veya açıklanamayan kilo kaybı ciddiye alınmalıdır.",
            urgency = UrgencyLevel.HIGH,
            department = "GENEL CERRAHİ",
            recommendations = listOf("Dışkıda gizli kan testi yaptırın.", "50 yaş üstüyseniz kolonoskopi şarttır.", "Hemoroid ile karıştırılabilir, doktor görmelidir.")
        ))

        rules.add(DiseaseRule(
            id = "EMERGENCY_APPENDICITIS",
            title = "Akut Apandisit",
            relatedKeywords = listOf("karin", "sag alt", "agri", "bulanti", "kusma", "ates", "ziplama", "yururken", "kivrandiran", "istahsizlik"),
            mustHaveKeywords = listOf("karin", "sag", "alt"),
            description = "Göbek çevresinde başlayıp sağ alt karına yerleşen şiddetli ağrı. Zıplamakla veya öksürmekle ağrı artar.",
            urgency = UrgencyLevel.HIGH,
            department = "GENEL CERRAHİ / ACİL",
            recommendations = listOf("Ağrı kesici ALMAYIN (teşhisi zorlaştırır).", "Bir şey yiyip içmeyin (ameliyat ihtimaline karşı).", "Sıcak uygulama YAPMAYIN (apandisi patlatabilir).", "Hastaneye başvurun.")
        ))

        // 2. KATEGORİ: ENFEKSİYON VE SOLUNUM (TEMEL)
        rules.add(DiseaseRule(
            id = "INF_FLU",
            title = "İnfluenza (Grip) / Ağır Soğuk Algınlığı",
            relatedKeywords = listOf("halsizlik", "yorgunluk", "kas", "eklem", "kirginlik", "ates", "titreme", "bas", "goz", "agrisi", "kuru", "oksuruk"),
            mustHaveKeywords = listOf("halsiz", "kas", "ates", "grip"),
            description = "Ani başlayan yüksek ateş, şiddetli kas ağrıları ve halsizlik ile karakterize viral enfeksiyon.",
            urgency = UrgencyLevel.LOW,
            department = "DAHİLİYE / AİLE HEKİMİ",
            recommendations = listOf("Bol sıvı tüketin.", "Yatak istirahati şarttır.", "Bulunduğunuz odayı sık sık havalandırın.", "Ateş düşürücü kullanabilirsiniz.")
        ))

        rules.add(DiseaseRule(
            id = "INF_COVID",
            title = "COVID-19 Semptomları",
            relatedKeywords = listOf("tat", "koku", "kaybi", "nefes", "darligi", "ates", "oksuruk", "ishal", "bogaz", "agrisi", "koku alamama", "tatsiz"),
            mustHaveKeywords = listOf("tat", "koku", "nefes"),
            description = "Tat ve koku kaybı, inatçı öksürük ve nefes darlığı ile seyreden viral enfeksiyon şüphesi.",
            urgency = UrgencyLevel.MODERATE,
            department = "GÖĞÜS HASTALIKLARI / ENFEKSİYON",
            recommendations = listOf("Kendinizi izole edin.", "Maske kullanın.", "Oksijen satürasyonunuzu takip edin.", "Solunum sıkıntısı artarsa hastaneye başvurun.")
        ))

        rules.add(DiseaseRule(
            id = "INF_SINUSITIS",
            title = "Akut Sinüzit",
            relatedKeywords = listOf("bas", "alin", "goz", "alti", "agri", "egilince", "burun", "tikanikligi", "sari", "yesil", "akinti", "koku"),
            mustHaveKeywords = listOf("bas", "egilince", "alin", "burun"),
            description = "Yüzde dolgunluk hissi, başı öne eğince artan ağrı ve koyu renkli burun akıntısı.",
            urgency = UrgencyLevel.LOW,
            department = "KBB (Kulak Burun Boğaz)",
            recommendations = listOf("Tuzlu su ile burun yıkama (lavaj) yapın.", "Sıcak duş buharı iyi gelebilir.", "Klimadan uzak durun.", "Saçlarınızı ıslak bırakmayın.")
        ))

        // 3. KATEGORİ: PSİKOLOJİ VE RUH SAĞLIĞI
        rules.add(DiseaseRule(
            id = "PSY_DEPRESSION",
            title = "Majör Depresif Bozukluk Belirtileri",
            relatedKeywords = listOf("mutsuz", "isteksiz", "keyif", "alamama", "uyku", "bozuklugu", "iştah", "yorgun", "enerjisiz", "degersiz", "suculuk", "olum", "intihar"),
            mustHaveKeywords = listOf("mutsuz", "isteksiz", "bunalim"),
            description = "En az 2 haftadır süren çökkkün duygu durumu, hayattan keyif alamama ve enerji kaybı.",
            urgency = UrgencyLevel.MODERATE,
            department = "PSİKİYATRİ / PSİKOLOG",
            recommendations = listOf("Bu durumun biyolojik bir süreç olduğunu kabul edin.", "Kendinizi suçlamayın.", "Küçük hedefler belirleyin.", "Profesyonel destek almaktan çekinmeyin.")
        ))

        rules.add(DiseaseRule(
            id = "PSY_PANIC",
            title = "Panik Atak",
            relatedKeywords = listOf("olum", "korkusu", "kalp", "carpintisi", "nefes", "alamama", "bogulma", "titreme", "uyusma", "kontrol", "kaybi", "delirme"),
            mustHaveKeywords = listOf("korku", "atak", "carpinti"),
            description = "Aniden gelen, 10-15 dakika süren yoğun korku ve fiziksel belirtiler. Fiziksel bir tehlike yoktur.",
            urgency = UrgencyLevel.HIGH,
            department = "PSİKİYATRİ",
            recommendations = listOf("Şu an güvendesiniz, bu sadece bir yanlış alarm.", "Nefesinize odaklanın: 4 saniye al, 4 saniye tut, 4 saniye ver.", "Bulunduğunuz ortamdaki 5 nesneyi sayın.", "Kafeinden uzak durun.")
        ))

        rules.add(DiseaseRule(
            id = "PSY_ANXIETY",
            title = "Yaygın Anksiyete (Kaygı) Bozukluğu",
            relatedKeywords = listOf("endise", "kaygi", "kuruntu", "evham", "kotu", "birsey", "olacak", "huzursuzluk", "kas", "gerginligi", "odaklanamama"),
            mustHaveKeywords = listOf("endise", "kaygi", "kuruntu"),
            description = "Ortada belirgin bir neden yokken sürekli tetikte olma ve felaket senaryoları düşünme hali.",
            urgency = UrgencyLevel.LOW,
            department = "PSİKİYATRİ / TERAPİ",
            recommendations = listOf("Endişe saati belirleyin.", "Haberleri ve sosyal medyayı kısıtlayın.", "Düzenli yürüyüş yapın.")
        ))

        // 4. KATEGORİ: GASTROENTEROLOJİ (TEMEL)
        rules.add(DiseaseRule(
            id = "GASTRO_REFLUX",
            title = "Gastroözofageal Reflü",
            relatedKeywords = listOf("mide", "yanmasi", "gogus", "arkasi", "agza", "aci", "su", "eksime", "gegirme", "bogazda", "yumru", "oksuruk"),
            mustHaveKeywords = listOf("yanma", "mide", "aci", "su"),
            description = "Mide asidinin yemek borusuna kaçması sonucu oluşan yanma ve ağza acı su gelmesi.",
            urgency = UrgencyLevel.LOW,
            department = "GASTROENTEROLOJİ",
            recommendations = listOf("Yemekten hemen sonra yatmayın (en az 3 saat).", "Yastığınızı yükseltin.", "Çikolata, nane, kahve ve yağlı yiyeceklerden kaçının.", "Dar kıyafetler giymeyin.")
        ))

        rules.add(DiseaseRule(
            id = "GASTRO_ULCER",
            title = "Gastrit / Ülser",
            relatedKeywords = listOf("mide", "agrisi", "kazinma", "aclik", "agrisi", "siskinlik", "bulanti", "siyah", "diski", "yemekten", "sonra"),
            mustHaveKeywords = listOf("mide", "kazinma", "agri"),
            description = "Mide iç yüzeyinin tahriş olması veya yara oluşumu. Açken veya yemekten sonra artan ağrı.",
            urgency = UrgencyLevel.MODERATE,
            department = "DAHİLİYE / GASTROENTEROLOJİ",
            recommendations = listOf("Sigara ve alkolü bırakın.", "Ağrı kesicileri bilinçsiz kullanmayın.", "Az ve sık beslenin.")
        ))

        // 5. KATEGORİ: NÖROLOJİ (TEMEL)
        rules.add(DiseaseRule(
            id = "NEURO_MIGRAINE",
            title = "Migren Atağı",
            relatedKeywords = listOf("bas", "agrisi", "tek", "tarafli", "zonklama", "isik", "ses", "hassasiyet", "bulanti", "kusma", "aura", "simsek"),
            mustHaveKeywords = listOf("bas", "zonklama", "isik", "bulanti"),
            description = "Genellikle tek taraflı, zonklayıcı, ışık ve sese duyarlılıkla beraber seyreden şiddetli baş ağrısı.",
            urgency = UrgencyLevel.MODERATE,
            department = "NÖROLOJİ",
            recommendations = listOf("Karanlık ve sessiz bir odada uyumayı deneyin.", "Başınıza soğuk kompres uygulayın.", "Tetikleyicileri (peynir, şarap, stres) not edin.")
        ))

        rules.add(DiseaseRule(
            id = "NEURO_TENSION",
            title = "Gerilim Tipi Baş Ağrısı",
            relatedKeywords = listOf("bas", "agrisi", "sikistirici", "banti", "gibi", "alin", "ense", "stres", "yorgunluk", "iki", "tarafli"),
            mustHaveKeywords = listOf("bas", "sikisma", "ense"),
            description = "Başın etrafında bir bant varmış gibi hissettiren, genellikle stres ve kas gerginliği kaynaklı ağrı.",
            urgency = UrgencyLevel.LOW,
            department = "NÖROLOJİ",
            recommendations = listOf("Boyun ve omuz masajı yapın.", "Sıcak duş alın.", "Ekran başındaysanız mola verin.", "Postürünüzü düzeltin.")
        ))

        rules.add(DiseaseRule(
            id = "NEURO_VERTIGO",
            title = "Vertigo (Baş Dönmesi)",
            relatedKeywords = listOf("bas", "donmesi", "yer", "ayagin", "altindan", "kaymasi", "denge", "kaybi", "kulak", "cinlamasi", "bulanti"),
            mustHaveKeywords = listOf("bas", "donme", "denge"),
            description = "Etrafın veya kendinizin döndüğü hissi. İç kulak problemlerinden kaynaklanabilir.",
            urgency = UrgencyLevel.MODERATE,
            department = "KBB / NÖROLOJİ",
            recommendations = listOf("Ani baş hareketlerinden kaçının.", "Atak sırasında sabit bir noktaya bakarak oturun.", "Tuz tüketimini azaltın.")
        ))

        // 6. KATEGORİ: ENDOKRİN (TEMEL)
        rules.add(DiseaseRule(
            id = "ENDO_DIABETES",
            title = "Diyabet (Şeker Hastalığı) Şüphesi",
            relatedKeywords = listOf("cok", "su", "icme", "sik", "idrar", "gece", "tuvalet", "agiz", "kurulugu", "kilo", "kaybi", "yaralar", "gec", "iyilesiyor", "acikma"),
            mustHaveKeywords = listOf("su", "idrar", "agiz"),
            description = "Aşırı susama, sık idrara çıkma ve açıklanamayan kilo kaybı yüksek kan şekeri belirtileridir.",
            urgency = UrgencyLevel.MODERATE,
            department = "DAHİLİYE / ENDOKRİNOLOJİ",
            recommendations = listOf("Açlık kan şekerinizi ölçtürün.", "Şekerli ve karbonhidratlı gıdaları azaltın.", "Ayak bakımınıza özen gösterin.")
        ))

        rules.add(DiseaseRule(
            id = "ENDO_THYROID_HYPO",
            title = "Hipotiroidi (Tiroid Tembelliği)",
            relatedKeywords = listOf("kilo", "alma", "yorgunluk", "ussume", "sac", "dokulmesi", "cilt", "kurulugu", "kabizlik", "unutkanlik", "sislik"),
            mustHaveKeywords = listOf("kilo", "yorgun", "ussume"),
            description = "Metabolizmanın yavaşlaması. Kilo alma, sürekli üşüme ve halsizlik görülür.",
            urgency = UrgencyLevel.LOW,
            department = "DAHİLİYE / ENDOKRİNOLOJİ",
            recommendations = listOf("TSH, T3 ve T4 hormonlarınıza baktırın.", "İyotlu tuz kullanın (doktor aksini demedikçe).", "Düzenli egzersiz metabolizmayı hızlandırır.")
        ))

        // 7. KATEGORİ: DERMATOLOJİ
        rules.add(DiseaseRule(
            id = "DERMA_ECZEMA",
            title = "Atopik Dermatit (Egzama)",
            relatedKeywords = listOf("kasinti", "kizariklik", "kuruluk", "pul", "pul", "dokulme", "catlama", "deri", "kabarcik", "stress"),
            mustHaveKeywords = listOf("kasinti", "kizarik", "kuru"),
            description = "Ciltte kuruluk, yoğun kaşıntı ve kızarıklıkla seyreden kronik cilt rahatsızlığı.",
            urgency = UrgencyLevel.LOW,
            department = "CİLDİYE (DERMATOLOJİ)",
            recommendations = listOf("Cildinizi sürekli nemli tutun.", "Sıcak suyla duş almayın.", "Yünlü giysilerden kaçının.", "Stres egzamanın en büyük tetikleyicisidir.")
        ))

        rules.add(DiseaseRule(
            id = "DERMA_HIVES",
            title = "Ürtiker (Kurdeşen)",
            relatedKeywords = listOf("vucutta", "kabarma", "kasinti", "kizarik", "plak", "sislik", "alerji", "bocek", "isirigi", "gibi"),
            mustHaveKeywords = listOf("kabarma", "kasinti", "kizarik"),
            description = "Aniden ortaya çıkan, kaşıntılı, kızarık ve kabarık plaklar.",
            urgency = UrgencyLevel.MODERATE,
            department = "CİLDİYE / ACİL",
            recommendations = listOf("Alerji yapabilecek son yediğiniz besinleri düşünün.", "Soğuk kompres kaşıntıyı azaltabilir.", "Nefes darlığı eşlik ederse ACİL servise gidin.")
        ))

        // 8. KATEGORİ: ÜROLOJİ (TEMEL)
        rules.add(DiseaseRule(
            id = "URO_STONE",
            title = "Böbrek Taşı (Renal Kolik)",
            relatedKeywords = listOf("yan", "agri", "bel", "boslugu", "bicak", "saplanmasi", "idrarda", "kan", "pembe", "bulanti", "kivrandiran"),
            mustHaveKeywords = listOf("yan", "agri", "bel"),
            description = "Sırttan kasığa doğru vuran, doğum sancısına benzetilen çok şiddetli ağrı.",
            urgency = UrgencyLevel.HIGH,
            department = "ÜROLOJİ / ACİL",
            recommendations = listOf("Hareket etmek taşın düşmesine yardımcı olabilir.", "Sıcak su torbası ağrıyı hafifletebilir.", "Bol su içmeye çalışın.")
        ))

        rules.add(DiseaseRule(
            id = "URO_CYSTITIS",
            title = "İdrar Yolu Enfeksiyonu (Sistit)",
            relatedKeywords = listOf("idrar", "yaparken", "yanma", "aci", "sik", "cikma", "kasilma", "tam", "bosalamama", "bulanık", "koku"),
            mustHaveKeywords = listOf("idrar", "yanma", "sik"),
            description = "Mesane enfeksiyonu. İdrarda yanma, sıkışma hissi ve kasık ağrısı.",
            urgency = UrgencyLevel.MODERATE,
            department = "ÜROLOJİ / AİLE HEKİMİ",
            recommendations = listOf("Günde en az 3 litre su için.", "İdrarınızı tutmayın.", "Ayaklarınızı sıcak tutun.", "Kızılcık suyu faydalı olabilir.")
        ))

        // 9. KATEGORİ: ORTOPEDİ (TEMEL)
        rules.add(DiseaseRule(
            id = "ORTHO_HERNIA",
            title = "Bel Fıtığı",
            relatedKeywords = listOf("bel", "agrisi", "bacak", "uyusma", "karincalanma", "kalca", "yururken", "topallama", "hareket", "kisitliligi"),
            mustHaveKeywords = listOf("bel", "bacak", "agri"),
            description = "Omurlar arasındaki diskin kayarak sinire baskı yapması. Bacağa vuran ağrı tipiktir.",
            urgency = UrgencyLevel.MODERATE,
            department = "BEYİN CERRAHİ / FİZİK TEDAVİ",
            recommendations = listOf("Ağır kaldırmaktan kaçının.", "Sert bir yatakta yatmayı deneyin.", "Uzun süre aynı pozisyonda oturmayın.", "Korse kullanımı doktor önerisiyle olmalıdır.")
        ))

        rules.add(DiseaseRule(
            id = "ORTHO_NECK",
            title = "Boyun Düzleşmesi / Tutulma",
            relatedKeywords = listOf("boyun", "agrisi", "cevirirken", "aci", "omuz", "sirt", "tutulma", "kitlama", "ses", "gelmesi", "telefon"),
            mustHaveKeywords = listOf("boyun", "agri", "omuz"),
            description = "Genellikle yanlış duruş, uzun süre telefon/bilgisayar kullanımına bağlı mekanik ağrı.",
            urgency = UrgencyLevel.LOW,
            department = "FİZİK TEDAVİ / ORTOPEDİ",
            recommendations = listOf("Ekranı göz hizasına yükseltin.", "Düzenli boyun germe egzersizleri yapın.", "Klimaya doğrudan maruz kalmayın.")
        ))

        // 10. KATEGORİ: GÖZ HASTALIKLARI
        rules.add(DiseaseRule(
            id = "EYE_CONJUNCTIVITIS",
            title = "Konjonktivit (Göz Nezlesi)",
            relatedKeywords = listOf("goz", "kizariklik", "capaklanma", "sulanma", "batma", "yanma", "kasinti", "kirpik", "yapisma"),
            mustHaveKeywords = listOf("goz", "kizarik", "capak"),
            description = "Gözün beyaz kısmının iltihaplanması. Bulaşıcı olabilir.",
            urgency = UrgencyLevel.LOW,
            department = "GÖZ HASTALIKLARI",
            recommendations = listOf("Gözlerinize elinizle dokunmayın.", "Havlu ve yastık kılıfınızı ayırın (Bulaşıcıdır).", "Çay pansumanı rahatlatabilir.", "Kontakt lens kullanmayın.")
        ))

        // ===========================================================================
        // BÖLÜM 2: GELİŞMİŞ VE UZMANLIK GEREKTİREN DURUMLAR (YENİ EKLENENLER)
        // ===========================================================================

        // 11. KATEGORİ: SOLUNUM SİSTEMİ HASTALIKLARI (GENIŞLETME)
        rules.add(DiseaseRule(
            id = "RESP_ASTHMA",
            title = "Astım Atağı",
            relatedKeywords = listOf("nefes", "darligi", "hisiltili", "solunum", "isligi", "gogus", "sikmasi", "oksuruk", "gece", "uyanma", "yorgunluk", "havasiz"),
            mustHaveKeywords = listOf("nefes", "darligi", "hisiltili"),
            description = "Hava yollarının daralması ile oluşan nefes darlığı, hışırtılı solunum ve göğüste sıkışma hissi.",
            urgency = UrgencyLevel.HIGH,
            department = "GÖĞÜS HASTALIKLARI / ACİL",
            recommendations = listOf("Oturarak solunum egzersizleri yapın (Yatar pozisyonda zorlaşır).", "Ventolin gibi bronkodilatör ilaç kullanın.", "Tetikleyicilerden uzak durun (Soğuk hava, duman, toz).", "Nefes darlığı artıyorsa 112'yi arayın.")
        ))

        rules.add(DiseaseRule(
            id = "RESP_BRONCHITIS",
            title = "Akut Bronşit",
            relatedKeywords = listOf("oksuruk", "balgam", "sari", "yesil", "gogus", "agrisi", "ates", "halsizlik", "nefes", "almak", "zor", "hiriltili"),
            mustHaveKeywords = listOf("oksuruk", "balgam", "gogus"),
            description = "Bronşların iltihaplanması. Balgamlı öksürük, göğüs ağrısı ve hırıltılı solunum görülür.",
            urgency = UrgencyLevel.MODERATE,
            department = "GÖĞÜS HASTALIKLARI / DAHİLİYE",
            recommendations = listOf("Bol sıvı içerek balgamı kolayca çıkarın.", "Buhar inhalasyonu yapın.", "Dumanlı ortamlardan uzak durun.", "Öksürük 3 haftadan uzun sürerse doktora başvurun.")
        ))

        rules.add(DiseaseRule(
            id = "RESP_PNEUMONIA",
            title = "Zatürre (Pnömoni)",
            relatedKeywords = listOf("yuksek", "ates", "titreme", "oksuruk", "balgam", "pasli", "gogus", "agrisi", "nefes", "almakta", "zorluk", "halsizlik", "terleme"),
            mustHaveKeywords = listOf("ates", "oksuruk", "gogus", "nefes"),
            description = "Akciğer dokusunun enfeksiyonu. Yüksek ateş, derin nefes alırken artan göğüs ağrısı ve paslı balgam tipiktir.",
            urgency = UrgencyLevel.HIGH,
            department = "GÖĞÜS HASTALIKLARI / ACİL",
            recommendations = listOf("Mutlaka antibiyotik tedavisi gerekir, doktora başvurun.", "Bol sıvı tüketin.", "Yatak istirahati yapın.", "Nefes egzersizleri önemlidir (Solunum fizyoterapisi).")
        ))

        rules.add(DiseaseRule(
            id = "RESP_COPD",
            title = "KOAH (Kronik Obstrüktif Akciğer Hastalığı) Atağı",
            relatedKeywords = listOf("nefes", "darligi", "kronik", "oksuruk", "balgam", "dudaklar", "mosmor", "sislik", "ayak", "yorgunluk", "sigara"),
            mustHaveKeywords = listOf("nefes", "kronik", "oksuruk"),
            description = "Uzun süreli sigara kullanımına bağlı akciğer hasarı. Kronik öksürük, balgam ve nefes darlığı ile seyreder.",
            urgency = UrgencyLevel.HIGH,
            department = "GÖĞÜS HASTALIKLARI",
            recommendations = listOf("Sigarayı DERHAL bırakın.", "Oksijen destek cihazınızı kullanın.", "Dudaklar morarmaya başladıysa ACİL servise gidin.", "Grip aşısı ve pnömokok aşısı yaptırın.")
        ))

        // 12. KATEGORİ: KADIN SAĞLIĞI VE JİNEKOLOJİ
        rules.add(DiseaseRule(
            id = "GYNE_MENSTRUAL_PAIN",
            title = "Dismenore (Adet Sancısı)",
            relatedKeywords = listOf("adet", "sancisi", "karin", "kramp", "agrisi", "kasik", "bel", "bulanti", "bas", "agrisi", "regl", "donem"),
            mustHaveKeywords = listOf("adet", "sanci", "karin"),
            description = "Adet döneminde alt karında kramp tarzı ağrılar. Normal bir durum olabilir ancak şiddeti kişiden kişiye değişir.",
            urgency = UrgencyLevel.LOW,
            department = "KADIN HASTALIKLARI",
            recommendations = listOf("Karın bölgesine sıcak uygulama yapın.", "Hafif egzersiz (Yürüyüş) rahatlatabilir.", "Ağrı kesici kullanabilirsiniz.", "Ağrı dayanılmaz düzeydeyse (Endometrioz ihtimali) doktora başvurun.")
        ))

        rules.add(DiseaseRule(
            id = "GYNE_OVARIAN_CYST",
            title = "Yumurtalık Kisti Rüptürü",
            relatedKeywords = listOf("ani", "kasik", "agrisi", "sag", "sol", "taraf", "sivri", "saplar", "gibi", "bulanti", "bayilma", "kanama"),
            mustHaveKeywords = listOf("kasik", "ani", "agri"),
            description = "Yumurtalıkta oluşan kistin patlaması. Ani ve şiddetli kasık ağrısı ile kendini gösterir.",
            urgency = UrgencyLevel.HIGH,
            department = "KADIN HASTALIKLARI / ACİL",
            recommendations = listOf("Hareket etmeyin, yatarak bekleyin.", "Hastaneye başvurun.", "İç kanama riski vardır, acil müdahale gerekebilir.")
        ))

        rules.add(DiseaseRule(
            id = "GYNE_UTI_PREGNANCY",
            title = "Gebelikte İdrar Yolu Enfeksiyonu",
            relatedKeywords = listOf("hamile", "gebe", "idrar", "yanma", "sik", "tuvalet", "bel", "agrisi", "ates", "bulanık"),
            mustHaveKeywords = listOf("hamile", "idrar", "yanma"),
            description = "Gebelik sırasında idrar yolu enfeksiyonu. Tedavi edilmezse erken doğum riskine yol açabilir.",
            urgency = UrgencyLevel.HIGH,
            department = "KADIN HASTALIKLARI",
            recommendations = listOf("Mutlaka doktora başvurun (Antibiyotik gerekir).", "Bol su için.", "Kızılcık suyu faydalıdır.", "İdrarınızı tutmayın.")
        ))

        rules.add(DiseaseRule(
            id = "GYNE_ECTOPIC",
            title = "Dış Gebelik (Ektopik Gebelik)",
            relatedKeywords = listOf("gebelik", "testi", "pozitif", "kasik", "agrisi", "kanama", "kahverengi", "akinti", "bayilma", "omuz", "ucu"),
            mustHaveKeywords = listOf("gebelik", "agri", "kanama"),
            description = "Döllenmiş yumurtanın rahim dışında gelişmesi (Genellikle yumurta kanalında). Hayati tehlike yaratır.",
            urgency = UrgencyLevel.CRITICAL,
            department = "KADIN HASTALIKLARI / ACİL",
            recommendations = listOf("DERHAL hastaneye gidin.", "Omuz ucunda ağrı varsa iç kanama olabilir.", "Acil ameliyat gerekebilir.")
        ))

        // 13. KATEGORİ: KULAK BURUN BOĞAZ (KBB) (GENIŞLETME)
        rules.add(DiseaseRule(
            id = "ENT_OTITIS",
            title = "Orta Kulak İltihabı (Otitis Media)",
            relatedKeywords = listOf("kulak", "agrisi", "zonklama", "ates", "isitme", "kaybi", "akinti", "sari", "tikanma", "hissi", "cocuk"),
            mustHaveKeywords = listOf("kulak", "agri", "ates"),
            description = "Orta kulak bölgesinin enfeksiyonu. Özellikle çocuklarda sıktır. Kulakta zonklayıcı ağrı ve ateş görülür.",
            urgency = UrgencyLevel.MODERATE,
            department = "KBB (Kulak Burun Boğaz)",
            recommendations = listOf("Antibiyotik tedavisi gerekir.", "Kulağa sıcak kompres uygulayın.", "Ağrı kesici kullanabilirsiniz.", "Uçak yolculuğundan kaçının.")
        ))

        rules.add(DiseaseRule(
            id = "ENT_TONSILLITIS",
            title = "Akut Tonsillit (Bademcik İltihabı)",
            relatedKeywords = listOf("bogaz", "agrisi", "yutkunma", "zor", "ates", "bademcik", "sislik", "kizariklik", "beyaz", "lekeler", "koku"),
            mustHaveKeywords = listOf("bogaz", "agri", "yutkunma"),
            description = "Bademciklerin iltihaplanması. Yutkunmayı zorlaştıran boğaz ağrısı, yüksek ateş ve şişlik.",
            urgency = UrgencyLevel.MODERATE,
            department = "KBB / AİLE HEKİMİ",
            recommendations = listOf("Bol ılık sıvı için (Ihlamur, papatya).", "Gargara yapın (Tuzlu ılık su).", "Yıl içinde 5+ kez tekrarlıyorsa ameliyat gerekebilir.", "Antibiyotik tedavisi için doktora başvurun.")
        ))

        rules.add(DiseaseRule(
            id = "ENT_LARYNGITIS",
            title = "Laringit (Gırtlak İltihabı / Ses Kısıklığı)",
            relatedKeywords = listOf("ses", "kisikmasi", "cikmiyor", "bogaz", "agrisi", "kuru", "oksuruk", "fisildama", "hava", "yolu"),
            mustHaveKeywords = listOf("ses", "kisik", "bogaz"),
            description = "Ses tellerinin iltihaplanması. Ses kısıklığı veya tamamen kaybolması, boğaz ağrısı ve kuru öksürük.",
            urgency = UrgencyLevel.LOW,
            department = "KBB",
            recommendations = listOf("Sesinizi tamamen dinlendirin (Fısıldamayın bile).", "Buhar inhalasyonu yapın.", "Bol ılık sıvı için.", "2 haftadan uzun sürerse mutlaka doktora gidin (Kanser riski).")
        ))

        rules.add(DiseaseRule(
            id = "ENT_EPISTAXIS",
            title = "Burun Kanaması (Epistaksis)",
            relatedKeywords = listOf("burun", "kanama", "damlama", "akim", "koku", "pisirme", "kuru", "hava", "travma", "yukseklik"),
            mustHaveKeywords = listOf("burun", "kanama"),
            description = "Burun içindeki damarların yırtılması. Kuru hava, tansiyon, travma veya kan pıhtılaşma bozukluğuna bağlı olabilir.",
            urgency = UrgencyLevel.MODERATE,
            department = "KBB / ACİL",
            recommendations = listOf("Başınızı ÖNE eğin (Arkaya değil!).", "Burnunuzun yumuşak kısmını 10 dakika sıkın.", "Buz uygulayın.", "20 dakikadan uzun sürerse veya sık tekrarlıyorsa hastaneye gidin.")
        ))

        // 14. KATEGORİ: ALERJİ VE İMMÜNOLOJİ
        rules.add(DiseaseRule(
            id = "ALLERGY_HAY_FEVER",
            title = "Alerjik Rinit (Saman Nezlesi)",
            relatedKeywords = listOf("hapsu", "aksirma", "burun", "akmasi", "sulanma", "goz", "kasintisi", "mevsimsel", "toz", "cicek", "tozu"),
            mustHaveKeywords = listOf("hapsu", "burun", "goz"),
            description = "Polen, toz veya hayvan tüyüne karşı alerjik reaksiyon. Hapşırma, burun akıntısı ve göz sulanması tipiktir.",
            urgency = UrgencyLevel.LOW,
            department = "ALERJİ / KBB",
            recommendations = listOf("Antihistaminik ilaç kullanın.", "Evde hava temizleyici bulundurun.", "Polen yoğun saatlerde dışarı çıkmayın.", "Alerjik faktörü belirlemek için deri testi yaptırın.")
        ))

        rules.add(DiseaseRule(
            id = "ALLERGY_ANAPHYLAXIS",
            title = "Anafilaktik Şok",
            relatedKeywords = listOf("nefes", "darligi", "bogaz", "sisme", "dil", "kalinlasma", "kan", "basinci", "dusme", "bayilma", "tansiz", "hizli", "kalp"),
            mustHaveKeywords = listOf("nefes", "sisme", "bogaz"),
            description = "Hayati tehlike yaratan şiddetli alerjik reaksiyon. Solunum yolları kapanabilir.",
            urgency = UrgencyLevel.CRITICAL,
            department = "ACİL SERVİS (112)",
            recommendations = listOf("DERHAL 112'yi arayın.", "Epipen (Adrenalin) varsa hemen uygulayın.", "Hasta yatar pozisyona alın, bacakları kaldırın.", "Ağızda bir şey varsa çıkarın.")
        ))

        rules.add(DiseaseRule(
            id = "ALLERGY_FOOD",
            title = "Gıda Alerjisi",
            relatedKeywords = listOf("yemekten", "sonra", "kasinti", "sislik", "agiz", "dudak", "bulanti", "kusma", "ishal", "karin", "kramp"),
            mustHaveKeywords = listOf("yemek", "kasinti", "sislik"),
            description = "Belirli bir gıdaya karşı bağışıklık sisteminin aşırı reaksiyonu. Kaşıntı, şişlik, mide bulantısı görülür.",
            urgency = UrgencyLevel.MODERATE,
            department = "ALERJİ / ACİL",
            recommendations = listOf("Alerjik olduğunuz gıdayı kesin olarak tespit edin.", "Etiketleri okuyun (Gizli içerikler olabilir).", "Epipen taşıyın (Şiddetli alerjide).", "Nefes darlığı başlarsa 112'yi arayın.")
        ))

        // 15. KATEGORİ: KARDİYOLOJİ (GENIŞLETME)
        rules.add(DiseaseRule(
            id = "CARDIO_HYPERTENSION",
            title = "Hipertansif Kriz (Tansiyon Krizi)",
            relatedKeywords = listOf("yuksek", "tansiyon", "bas", "agrisi", "goz", "onunde", "isik", "carpma", "bulanik", "gorme", "gogus", "sikismasi", "kulak", "cinlama"),
            mustHaveKeywords = listOf("tansiyon", "yuksek", "bas"),
            description = "Kan basıncının aniden ve tehlikeli düzeyde yükselmesi (180/120 üzeri). İnme ve kalp krizi riskini artırır.",
            urgency = UrgencyLevel.HIGH,
            department = "KARDİYOLOJİ / ACİL",
            recommendations = listOf("Oturarak sakinleşmeye çalışın.", "Derin nefes alın.", "Tuz tüketimini derhal durdurun.", "Tansiyon 180/120 üzerindeyse ACİL servise gidin.")
        ))

        rules.add(DiseaseRule(
            id = "CARDIO_ARRHYTHMIA",
            title = "Kalp Ritim Bozukluğu (Aritmi)",
            relatedKeywords = listOf("kalp", "carpintisi", "duzensiz", "atim", "atlamasi", "gogus", "carpma", "bas", "donmesi", "bayilma"),
            mustHaveKeywords = listOf("kalp", "duzensiz", "atim"),
            description = "Kalbin düzensiz atması. Hızlı, yavaş veya atlayarak vuruş hissi olabilir.",
            urgency = UrgencyLevel.MODERATE,
            department = "KARDİYOLOJİ",
            recommendations = listOf("Kafeini azaltın.", "Stres yönetimi yapın.", "Holter EKG cihazı ile takip yapılabilir.", "Göğüs ağrısı veya bayılma eşlik ederse ACİL'e gidin.")
        ))

        rules.add(DiseaseRule(
            id = "CARDIO_VARICOSE",
            title = "Varis ve Derin Ven Trombozu (DVT) Şüphesi",
            relatedKeywords = listOf("bacak", "sislik", "agri", "kizariklik", "sicaklik", "ven", "damar", "sisman", "hareketsizlik", "uzun", "yolculuk"),
            mustHaveKeywords = listOf("bacak", "sislik", "agri"),
            description = "Bacak damarlarında pıhtı oluşumu. Özellikle uzun süre hareketsiz kalma sonrası riskli. Pıhtı akciğere giderse fatal olabilir.",
            urgency = UrgencyLevel.HIGH,
            department = "KARDİYOLOJİ / KALP DAMAR CERRAHİSİ",
            recommendations = listOf("Bacağınızı yükseğe kaldırın.", "Hareket edin, masaj YAPMAYIN (Pıhtı kopar).", "Kompresyon çorabı giyin.", "Nefes darlığı başlarsa (Pulmoner emboli) 112'yi arayın.")
        ))

        // 16. KATEGORİ: GASTROENTEROLOJİ (GENIŞLETME)
        rules.add(DiseaseRule(
            id = "GASTRO_IBS",
            title = "İrritabl Bağırsak Sendromu (IBS)",
            relatedKeywords = listOf("karin", "agrisi", "siskinlik", "gaz", "kabizlik", "ishal", "degismeli", "stres", "yemekten", "sonra", "kramp"),
            mustHaveKeywords = listOf("karin", "agri", "gaz", "stres"),
            description = "Kronik bağırsak rahatsızlığı. Stresle tetiklenen karın ağrısı, şişkinlik, gaz ve değişken dışkılama alışkanlığı.",
            urgency = UrgencyLevel.LOW,
            department = "GASTROENTEROLOJİ",
            recommendations = listOf("Stres yönetimi çok önemlidir.", "FODMAP diyeti deneyin.", "Probiyotik kullanın.", "Gaz yapan besinlerden kaçının (Baklagiller, kola).")
        ))

        rules.add(DiseaseRule(
            id = "GASTRO_HEMORRHOID",
            title = "Hemoroid (Basur)",
            relatedKeywords = listOf("anüs", "agri", "kanama", "kirmizi", "kan", "tuvalet", "kagidinda", "sislik", "kasinti", "kabizlik"),
            mustHaveKeywords = listOf("anus", "kanama", "agri"),
            description = "Anüs çevresindeki damarların şişmesi. Dışkılama sırasında ağrı, kanama ve kaşıntı görülür.",
            urgency = UrgencyLevel.LOW,
            department = "GENEL CERRAHİ",
            recommendations = listOf("Lifli beslenin (Sebze, meyve).", "Bol su için.", "Tuvalette uzun süre oturmayın.", "Sitz banyosu (Ilık oturma banyosu) yapın.")
        ))

        rules.add(DiseaseRule(
            id = "GASTRO_CELIAC",
            title = "Çölyak Hastalığı (Gluten Enteropatisi)",
            relatedKeywords = listOf("ishal", "karin", "siskinlik", "kilo", "kaybi", "yorgunluk", "ekmek", "makarna", "gluten", "bulanti"),
            mustHaveKeywords = listOf("ishal", "gluten", "karin"),
            description = "Gluten intoleransı. Buğday, arpa içeren gıdaları yedikten sonra bağırsak hasarı ve sindirim problemleri oluşur.",
            urgency = UrgencyLevel.LOW,
            department = "GASTROENTEROLOJİ",
            recommendations = listOf("Glutensiz diyet uygulamalısınız (Ömür boyu).", "Etiketleri okuyun (Gizli gluten kaynakları).", "Diyetisyenden destek alın.", "Vitamin takviyesi gerekebilir (D, Demir, Kalsiyum).")
        ))

        rules.add(DiseaseRule(
            id = "GASTRO_PANCREATITIS",
            title = "Akut Pankreatit",
            relatedKeywords = listOf("ust", "karin", "agri", "sirt", "yayilan", "bulanti", "kusma", "ates", "yagili", "yemek", "sonra", "kivrandiran"),
            mustHaveKeywords = listOf("ust", "karin", "sirt", "agri"),
            description = "Pankreasın iltihaplanması. Üst karında başlayan ve sırta vuran dayanılmaz ağrı, kusma ve ateş eşlik eder.",
            urgency = UrgencyLevel.HIGH,
            department = "GENEL CERRAHİ / ACİL",
            recommendations = listOf("DERHAL hastaneye gidin.", "Hiçbir şey yiyip içmeyin.", "Alkol tüketimini durdurun.", "Hastanede yatarak tedavi gerekir.")
        ))

        // 17. KATEGORİ: NÖROLOJİK (GENIŞLETME)
        rules.add(DiseaseRule(
            id = "NEURO_BELL_PALSY",
            title = "Bell Paralizisi (Yüz Felci)",
            relatedKeywords = listOf("yuz", "felc", "kayma", "asimetri", "goz", "kapanmama", "agiz", "suyu", "akmasi", "tat", "alma", "bozuklugu"),
            mustHaveKeywords = listOf("yuz", "kayma", "goz"),
            description = "Yüz sinirinin felci. Yüzün bir tarafı kası çalışmaz, göz kapanmaz, ağız suyu akar.",
            urgency = UrgencyLevel.HIGH,
            department = "NÖROLOJİ",
            recommendations = listOf("48 saat içinde tedavi başlanmalı (Kortikosteroid).", "Gözünüzü nemli tutun (Damlalar kullanın).", "Fizik tedavi önemlidir.", "Çoğu hasta 3-6 ayda tamamen iyileşir.")
        ))

        rules.add(DiseaseRule(
            id = "NEURO_SEIZURE",
            title = "Epileptik Nöbet",
            relatedKeywords = listOf("nobet", "kaslarma", "konvulsiyon", "bilinc", "kaybi", "sarsinti", "tirma", "goz", "kaymasi", "agiz", "kopurme"),
            mustHaveKeywords = listOf("nobet", "kaslarma", "bilinc"),
            description = "Beyin elektriksel aktivitesinin anormal şekilde artması. Bilinç kaybı, kasılma ve sarsıntılar görülür.",
            urgency = UrgencyLevel.HIGH,
            department = "NÖROLOJİ / ACİL",
            recommendations = listOf("Hastayı yan yatırın (Kusma durumunda boğulmasın).", "Ağzına hiçbir şey koymayın.", "Çevresindeki kesici cisimlerden uzaklaştırın.", "Nöbet 5 dakikadan uzun sürerse 112'yi arayın.")
        ))

        rules.add(DiseaseRule(
            id = "NEURO_PARKINSONS",
            title = "Parkinson Hastalığı Belirtileri",
            relatedKeywords = listOf("titreme", "el", "parmak", "yavas", "hareket", "denge", "bozuklugu", "yurume", "zorluğu", "yuz", "donuklugu", "kas", "sertligi"),
            mustHaveKeywords = listOf("titreme", "yavas", "hareket"),
            description = "Beyin hücrelerinin zamanla kaybı. Dinlenme halinde titreme, yavaş hareket, denge problemleri ve kas sertliği.",
            urgency = UrgencyLevel.MODERATE,
            department = "NÖROLOJİ",
            recommendations = listOf("Erken teşhis önemlidir.", "İlaç tedavisi semptomları azaltır.", "Fizik tedavi ve egzersiz şarttır.", "Destek gruplarına katılın.")
        ))

        rules.add(DiseaseRule(
            id = "NEURO_MS",
            title = "Multipl Skleroz (MS) Atağı",
            relatedKeywords = listOf("gorme", "bozuklugu", "cift", "bulanik", "uyusma", "karincalanma", "gucsuzluk", "denge", "kaybi", "yorgunluk"),
            mustHaveKeywords = listOf("gorme", "uyusma", "gucsuzluk"),
            description = "Bağışıklık sisteminin sinir sistemine saldırması. Görme bozukluğu, uyuşma, güçsüzlük ve denge kaybı ataklar halinde gelir.",
            urgency = UrgencyLevel.HIGH,
            department = "NÖROLOJİ",
            recommendations = listOf("Atak döneminde kortikosteroid tedavisi verilir.", "Stres MS'i tetikler, yönetmeyi öğrenin.", "Soğuk ortamlar rahatlatır, sıcaktan kaçının.", "Hastalık modifiye edici ilaçlar (DMT) kullanın.")
        ))

        // 18. KATEGORİ: ORTOPEDİ VE TRAVMATOLOJI (GENIŞLETME)
        rules.add(DiseaseRule(
            id = "ORTHO_CARPAL_TUNNEL",
            title = "Karpal Tünel Sendromu",
            relatedKeywords = listOf("el", "uyusmasi", "parmak", "karincalanma", "gece", "uyanma", "agrisi", "kavrama", "gucu", "kaybi", "bilgisayar"),
            mustHaveKeywords = listOf("el", "uyusma", "parmak"),
            description = "El bileğindeki sinirin sıkışması. Başparmak, işaret ve orta parmakta uyuşma, özellikle gece artar.",
            urgency = UrgencyLevel.LOW,
            department = "ORTOPEDİ / EL CERRAHİSİ",
            recommendations = listOf("Gece ateli kullanın.", "Bilgisayar başında sık sık mola verin.", "El ve bilek germe egzersizleri yapın.", "İleri vakalarda ameliyat gerekir.")
        ))

        rules.add(DiseaseRule(
            id = "ORTHO_FROZEN_SHOULDER",
            title = "Donuk Omuz (Frozen Shoulder)",
            relatedKeywords = listOf("omuz", "agri", "hareket", "kisitliligi", "giydirme", "taramak", "zor", "gece", "artma", "sertlik"),
            mustHaveKeywords = listOf("omuz", "agri", "hareket"),
            description = "Omuz ekleminin sertleşmesi ve hareket kısıtlılığı. Kolunu yukarı kaldıramama, gece artan ağrı tipiktir.",
            urgency = UrgencyLevel.LOW,
            department = "FİZİK TEDAVİ / ORTOPEDİ",
            recommendations = listOf("Fizik tedavi çok önemlidir.", "Ağrı kesiciler kullanın.", "Düzenli omuz egzersizleri yapın.", "İyileşme süreci 1-3 yıl sürebilir.")
        ))

        rules.add(DiseaseRule(
            id = "ORTHO_ARTHRITIS",
            title = "Osteoartrit (Eklem Kireçlenmesi)",
            relatedKeywords = listOf("diz", "agri", "sabah", "sertlik", "merdiven", "inme", "cikma", "zor", "sislik", "krepitasyon", "ciritma"),
            mustHaveKeywords = listOf("diz", "agri", "sabah", "sertlik"),
            description = "Eklem kıkırdağının aşınması. Sabah sertliği, hareketle başlangıçta ağrı, merdiven inip çıkmada zorluk.",
            urgency = UrgencyLevel.LOW,
            department = "ORTOPEDİ / FİZİK TEDAVİ",
            recommendations = listOf("Kilo verin (Her 5 kg dizde 20 kg azalma yaratır).", "Düzenli egzersiz (Yüzme, bisiklet).", "Baston kullanmaktan çekinmeyin.", "İleri vakalarda protez cerrahisi yapılabilir.")
        ))

        rules.add(DiseaseRule(
            id = "ORTHO_FRACTURE",
            title = "Kemik Kırığı Şüphesi",
            relatedKeywords = listOf("travma", "carpma", "dusme", "sislik", "morluk", "deforme", "akilmayan", "agri", "hareket", "edememe"),
            mustHaveKeywords = listOf("travma", "sislik", "agri"),
            description = "Kemik bütünlüğünün bozulması. Şiddetli ağrı, şişlik, hareket edememe ve deformite görülür.",
            urgency = UrgencyLevel.HIGH,
            department = "ORTOPEDİ / ACİL",
            recommendations = listOf("Yaralı bölgeyi hareket ettirmeyin.", "Atel uygulayın (Gazete, karton kullanabilirsiniz).", "Buz uygulayın (Deri ile temas ettirmeyin).", "Hastaneye gidin, röntgen çekilmeli.")
        ))

        // 19. KATEGORİ: HEMATOLOJİ (KAN HASTALIKLARI)
        rules.add(DiseaseRule(
            id = "HEMATO_ANEMIA",
            title = "Demir Eksikliği Anemisi",
            relatedKeywords = listOf("yorgunluk", "solgunluk", "nefes", "darligi", "bas", "donmesi", "cakma", "dilinde", "yaralar", "buz", "yeme"),
            mustHaveKeywords = listOf("yorgunluk", "solgun", "nefes"),
            description = "Kanda hemoglobin azlığı. Aşırı yorgunluk, solgunluk, çarpıntı ve nefes darlığı görülür.",
            urgency = UrgencyLevel.LOW,
            department = "İÇ HASTALIKLARI / HEMATOLOJİ",
            recommendations = listOf("Demir hapı kullanın (Mide bulantısı yapabilir).", "Kırmızı et, ıspanak, mercimek tüketin.", "C vitamini demirin emilimini artırır.", "Çay ve kahve demir emilimini azaltır.")
        ))

        rules.add(DiseaseRule(
            id = "HEMATO_LEUKEMIA",
            title = "Lösemi (Kan Kanseri) Şüphesi",
            relatedKeywords = listOf("halsizlik", "ates", "gece", "terlemesi", "kilo", "kaybi", "kolay", "morarma", "kanamalar", "lenf", "bezi", "sismesi"),
            mustHaveKeywords = listOf("halsizlik", "ates", "morarma"),
            description = "Beyaz kan hücrelerinin anormal çoğalması. Sürekli ateş, gece terlemesi, kolay morarma ve lenf bezi büyümesi.",
            urgency = UrgencyLevel.HIGH,
            department = "HEMATOLOJİ / ONKOLOJİ",
            recommendations = listOf("Tam kan sayımı yaptırın.", "Kemik iliği biyopsisi gerekir.", "Erken tanı hayat kurtarır.", "Tedavi seçenekleri: Kemoterapi, kök hücre nakli.")
        ))

        // 20. KATEGORİ: ENDOKRİN VE METABOLİZMA (GENIŞLETME)
        rules.add(DiseaseRule(
            id = "ENDO_HYPERTHYROID",
            title = "Hipertiroidi (Tiroid Fazla Çalışması)",
            relatedKeywords = listOf("kilo", "kaybi", "titreme", "terleme", "carpinti", "sinirlilik", "goz", "firlamasi", "ishal", "uyuyamama"),
            mustHaveKeywords = listOf("kilo", "kaybi", "carpinti", "titreme"),
            description = "Tiroid bezinin fazla hormon üretmesi. Metabolizma hızlanır, kilo kaybı, titreme, çarpıntı ve sinirlilik görülür.",
            urgency = UrgencyLevel.MODERATE,
            department = "ENDOKRİNOLOJİ",
            recommendations = listOf("TSH, T3 ve T4 seviyelerinizi ölçtürün.", "İyotlu tuzdan uzak durun.", "İlaç tedavisi veya radyoaktif iyot tedavisi gerekir.", "Sigara tiroid hastalığını kötüleştirir.")
        ))

        rules.add(DiseaseRule(
            id = "ENDO_CUSHINGS",
            title = "Cushing Sendromu",
            relatedKeywords = listOf("ay", "yuz", "kilo", "alma", "gogus", "karinda", "catlaklar", "yuz", "killanma", "sivilce", "tansiyon"),
            mustHaveKeywords = listOf("ay", "yuz", "kilo", "catlak"),
            description = "Vücutta fazla kortizol hormonu. Yüz dolgunluğu (ay yüzü), karında kilo alma, ciltte mor çatlaklar.",
            urgency = UrgencyLevel.MODERATE,
            department = "ENDOKRİNOLOJİ",
            recommendations = listOf("Kortizol seviyeniz ölçülmeli.", "Nedeni belirlemek için görüntüleme yapılır (MR, BT).", "Tedavi nedene bağlıdır (İlaç, ameliyat).", "Kan şekerinizi kontrol ettirin (Diyabet riski).")
        ))

        // 21. KATEGORİ: DİŞ VE AĞIZ SAĞLIĞI
        rules.add(DiseaseRule(
            id = "DENTAL_ABSCESS",
            title = "Diş Apsesi",
            relatedKeywords = listOf("dis", "agrisi", "sislik", "yanak", "ates", "cene", "zonklama", "cerahat", "koku", "agiz"),
            mustHaveKeywords = listOf("dis", "agri", "sislik"),
            description = "Diş kökünde iltihaplı cep oluşumu. Şiddetli zonklayıcı ağrı, yüzde şişlik ve ateş görülür.",
            urgency = UrgencyLevel.HIGH,
            department = "DİŞ HEKİMLİĞİ",
            recommendations = listOf("Antibiyotik tedavisi gerekir.", "Tuzlu ılık suyla gargara yapın.", "Ağrı kesici kullanın.", "Apse drenajı veya diş çekimi gerekebilir.")
        ))

        rules.add(DiseaseRule(
            id = "DENTAL_GINGIVITIS",
            title = "Diş Eti İltihabı (Gingivit)",
            relatedKeywords = listOf("dis", "eti", "kanamasi", "kizariklik", "sislik", "agiz", "kokusu", "fircalama", "sonrasi", "kan"),
            mustHaveKeywords = listOf("dis", "eti", "kanama"),
            description = "Diş etlerinin iltihaplanması. Fırçalama sırasında kanama, kızarıklık ve şişlik görülür.",
            urgency = UrgencyLevel.LOW,
            department = "DİŞ HEKİMLİĞİ",
            recommendations = listOf("Diş fırçalama ve diş ipi kullanımına özen gösterin.", "Düzenli diş taşı temizliği yaptırın.", "Klorheksidin gargarası kullanın.", "Tedavi edilmezse diş kaybına yol açar.")
        ))

        rules.add(DiseaseRule(
            id = "DENTAL_TMJ",
            title = "Temporomandibular Eklem (Çene Eklem) Bozukluğu",
            relatedKeywords = listOf("cene", "agrisi", "citirti", "ses", "agiz", "acmakta", "zorluk", "dis", "sikmasi", "kulak", "agri", "bas"),
            mustHaveKeywords = listOf("cene", "agri", "citirti"),
            description = "Çene ekleminin fonksiyon bozukluğu. Ağız açarken ses, ağrı ve kilitlenme hissi olur.",
            urgency = UrgencyLevel.LOW,
            department = "DİŞ HEKİMLİĞİ / ÇENE CERRAHİSİ",
            recommendations = listOf("Sert yiyeceklerden kaçının.", "Sakız çiğnemeyin.", "Stres çeneyi sıkmanıza neden olur, yönetmeyi öğrenin.", "Gece plağı (bite guard) kullanın.")
        ))

        // 22. KATEGORİ: ÇOCUK HASTALIKLARI
        rules.add(DiseaseRule(
            id = "PEDIA_CROUP",
            title = "Krup (Çocuklarda Boğaz İltihabı)",
            relatedKeywords = listOf("cocuk", "havlama", "oksuruk", "ses", "kisikmasi", "nefes", "almakta", "zorluk", "hisiltili", "gece"),
            mustHaveKeywords = listOf("cocuk", "havlama", "oksuruk"),
            description = "Çocuklarda hava yolu daralması. Köpek havlaması gibi öksürük, nefes darlığı ve ses kısıklığı.",
            urgency = UrgencyLevel.HIGH,
            department = "ÇOCUK SAĞLIĞI / ACİL",
            recommendations = listOf("Çocuğu buhar altına alın.", "Soğuk hava solumak faydalıdır (Pencere açın).", "Kortikosteroid tedavisi gerekir.", "Nefes alamıyorsa 112'yi arayın.")
        ))

        rules.add(DiseaseRule(
            id = "PEDIA_ROSEOLA",
            title = "Roseola (Altıncı Hastalık)",
            relatedKeywords = listOf("bebek", "yuksek", "ates", "3", "gun", "dokultu", "kizariklik", "govde", "huysuzluk"),
            mustHaveKeywords = listOf("bebek", "ates", "dokultu"),
            description = "6-24 ay arası bebeklerde görülür. 3 gün yüksek ateş sonrası vücutta kırmızı döküntüler çıkar.",
            urgency = UrgencyLevel.LOW,
            department = "ÇOCUK SAĞLIĞI",
            recommendations = listOf("Ateş düşürücü verin.", "Bol sıvı tüketsin.", "Hastalık kendiliğinden geçer.", "Döküntüler kaşıntı yapmaz, tedavi gerektirmez.")
        ))

        rules.add(DiseaseRule(
            id = "PEDIA_HAND_FOOT_MOUTH",
            title = "El-Ayak-Ağız Hastalığı",
            relatedKeywords = listOf("cocuk", "agiz", "yarasi", "el", "ayak", "dokultu", "kabarcik", "ates", "yemek", "yiyememe"),
            mustHaveKeywords = listOf("cocuk", "agiz", "el", "ayak"),
            description = "Viral enfeksiyon. Ağızda aft benzeri yaralar, el ve ayaklarda kırmızı kabarcıklar çıkar.",
            urgency = UrgencyLevel.LOW,
            department = "ÇOCUK SAĞLIĞI",
            recommendations = listOf("Soğuk gıdalar (Dondurma) rahatlatır.", "Asitli içeceklerden kaçının.", "Hastalık 7-10 günde kendiliğinden geçer.", "Bulaşıcıdır, çocuğu izole edin.")
        ))

        // 23. KATEGORİ: BÖBREK VE İDRAR YOLU (GENIŞLETME)
        rules.add(DiseaseRule(
            id = "URO_KIDNEY_FAILURE",
            title = "Akut Böbrek Yetmezliği Şüphesi",
            relatedKeywords = listOf("idrar", "azalmasi", "cikmiyor", "sislik", "yuz", "bacak", "nefes", "darligi", "bulanti", "kusma"),
            mustHaveKeywords = listOf("idrar", "azalma", "sislik"),
            description = "Böbreklerin aniden çalışmayı durdurması. İdrar azalması veya kesilmesi, vücutta ödem ve nefes darlığı.",
            urgency = UrgencyLevel.CRITICAL,
            department = "NEFROLOJİ / ACİL",
            recommendations = listOf("DERHAL hastaneye gidin.", "Diyaliz gerekebilir.", "Sıvı kısıtlaması yapılmalıdır.", "Potasyum yüksekliği kalp ritmini bozabilir.")
        ))

        rules.add(DiseaseRule(
            id = "URO_PROSTATE",
            title = "Prostat Büyümesi (BPH)",
            relatedKeywords = listOf("idrar", "yapmakta", "zorluk", "zayif", "akim", "damla", "damla", "gece", "tuvalete", "kalkma", "tam", "bosaltamama"),
            mustHaveKeywords = listOf("idrar", "zayif", "gece", "tuvalet"),
            description = "Yaşlı erkeklerde prostatın büyümesi. İdrar akımında zayıflama, sık ve gece tuvalete kalkma.",
            urgency = UrgencyLevel.LOW,
            department = "ÜROLOJİ",
            recommendations = listOf("Akşam sıvı tüketimini azaltın.", "Kafein ve alkol prostatı irrite eder.", "İlaç tedavisi veya ameliyat gerekebilir.", "PSA testinizi yaptırın (Kanser taraması).")
        ))

        // 24. KATEGORİ: ENFEKSIYONLAR (GENIŞLETME)
        rules.add(DiseaseRule(
            id = "INF_MENINGITIS",
            title = "Menenjit (Beyin Zarı İltihabı) Şüphesi",
            relatedKeywords = listOf("siddetli", "bas", "agrisi", "boyun", "sertligi", "ates", "kusma", "isiga", "hassasiyet", "bilinc", "bozuklugu"),
            mustHaveKeywords = listOf("bas", "agri", "boyun", "sertlik"),
            description = "Beyin ve omurilik zarlarının enfeksiyonu. Şiddetli baş ağrısı, boyun sertliği ve ateş hayati tehlikedir.",
            urgency = UrgencyLevel.CRITICAL,
            department = "ENFEKSİYON / ACİL",
            recommendations = listOf("DERHAL 112'yi arayın.", "Bu bir tıbbi acildir, zaman kaybetmeyin.", "Lomber ponksiyon (bel iğnesi) gerekir.", "Antibiyotik tedavisi hayat kurtarır.")
        ))

        rules.add(DiseaseRule(
            id = "INF_SEPSIS",
            title = "Sepsis (Kan Zehirlenmesi)",
            relatedKeywords = listOf("yuksek", "ates", "titreme", "hizli", "kalp", "nefes", "darligi", "bilinc", "bozuklugu", "soguk", "terli", "deri"),
            mustHaveKeywords = listOf("ates", "titreme", "bilinc"),
            description = "Enfeksiyonun kana yayılması. Organ yetmezliğine yol açabilir, hayati tehlikedir.",
            urgency = UrgencyLevel.CRITICAL,
            department = "YOĞ UN BAKIM / ACİL",
            recommendations = listOf("DERHAL ACİL servise gidin.", "Her dakika önemlidir.", "Geniş spektrumlu antibiyotik gerekir.", "Yoğun bakım takibi şarttır.")
        ))

        rules.add(DiseaseRule(
            id = "INF_TUBERCULOSIS",
            title = "Tüberküloz (Verem) Şüphesi",
            relatedKeywords = listOf("kronik", "oksuruk", "3", "hafta", "gece", "terlemesi", "kilo", "kaybi", "kanli", "balgam", "halsizlik"),
            mustHaveKeywords = listOf("oksuruk", "gece", "terlemesi", "kilo"),
            description = "Akciğer enfeksiyonu. 3 haftadan uzun süren öksürük, gece terlemesi, kilo kaybı ve bazen kanlı balgam.",
            urgency = UrgencyLevel.MODERATE,
            department = "GÖĞÜS HASTALIKLARI",
            recommendations = listOf("PPD testi (tüberkülin) ve akciğer filmi çektirin.", "En az 6 ay antibiyotik tedavisi gerekir.", "Bulaşıcıdır, yakınlarınızı koruyun.", "İlaçları düzenli kullanın (Direnç gelişmemesi için).")
        ))

        rules.add(DiseaseRule(
            id = "INF_SHINGLES",
            title = "Zona (Herpes Zoster)",
            relatedKeywords = listOf("yanma", "aci", "kabarcik", "dokultu", "tek", "tarafli", "bel", "gogus", "yuz", "cizgi", "halinde"),
            mustHaveKeywords = listOf("yanma", "kabarcik", "tek", "taraf"),
            description = "Suçiçeği virüsünün reaktivasyonu. Vücudun bir tarafında çizgi şeklinde ağrılı kızarıklık ve su dolu kabarcıklar.",
            urgency = UrgencyLevel.MODERATE,
            department = "CİLDİYE / ENFEKSİYON",
            recommendations = listOf("Antiviral ilaç 72 saat içinde başlanmalı.", "Ağrı çok şiddetli olabilir (Nöropatik).", "Kabarcıklar patladığında bulaşıcıdır.", "50 yaş üstü zona aşısı yaptırabilir.")
        ))

        // 25. KATEGORİ: ONKOLOJİ (KANSER BELİRTİLERİ)
        rules.add(DiseaseRule(
            id = "ONCO_BREAST",
            title = "Meme Kanseri Şüphesi",
            relatedKeywords = listOf("meme", "kitle", "sertlik", "cukurlesme", "meme", "basi", "akintisi", "kanli", "agrisiz", "yumru"),
            mustHaveKeywords = listOf("meme", "kitle", "sertlik"),
            description = "Memede ele gelen ağrısız kitle, meme ucunda çekinti, meme başından kanlı akıntı.",
            urgency = UrgencyLevel.HIGH,
            department = "GENEL CERRAHİ / ONKOLOJİ",
            recommendations = listOf("Erken teşhis hayat kurtarır.", "Mamografi ve ultrason yaptırın.", "Kitle olsa da %80'i iyi huyludur.", "40 yaş üstü yılda bir mamografi şarttır.")
        ))

        rules.add(DiseaseRule(
            id = "ONCO_COLON",
            title = "Kolorektal Kanser Şüphesi",
            relatedKeywords = listOf("diski", "aliskanligi", "degisimi", "kan", "karanlik", "kilo", "kaybi", "karin", "agrisi", "kabizlik", "ishal"),
            mustHaveKeywords = listOf("diski", "kan", "kilo", "kaybi"),
            description = "Dışkılama alışkanlığında değişiklik, dışkıda kan, açıklanamayan kilo kaybı ve karın ağrısı.",
            urgency = UrgencyLevel.HIGH,
            department = "GENEL CERRAHİ / ONKOLOJİ",
            recommendations = listOf("50 yaş üstü kolonoskopi yaptırın.", "Ailede kanser öyküsü varsa daha erken tarama yapın.", "Kırmızı et tüketimini azaltın.", "Lifli beslenin ve egzersiz yapın.")
        ))

        rules.add(DiseaseRule(
            id = "ONCO_LUNG",
            title = "Akciğer Kanseri Şüphesi",
            relatedKeywords = listOf("kronik", "oksuruk", "kanli", "balgam", "gogus", "agrisi", "nefes", "darligi", "kilo", "kaybi", "sigara"),
            mustHaveKeywords = listOf("oksuruk", "kanli", "sigara"),
            description = "Uzun süreli sigara kullanımı sonrası kronik öksürük, kanlı balgam, nefes darlığı ve kilo kaybı.",
            urgency = UrgencyLevel.HIGH,
            department = "GÖĞÜS HASTALIKLARI / ONKOLOJİ",
            recommendations = listOf("Sigarayı derhal bırakın.", "Akciğer grafisi ve BT çektirin.", "Erken evrede ameliyat şansı yüksektir.", "Ağrı kesiciler yetersiz kalıyorsa ciddi bir durumun işaretidir.")
        ))

        // 26. KATEGORİ: GENETİK VE SİSTEMİK HASTALIKLAR
        rules.add(DiseaseRule(
            id = "GENETIC_LUPUS",
            title = "Sistemik Lupus Eritematozus (SLE)",
            relatedKeywords = listOf("kelebek", "doku", "yanak", "eklem", "agrisi", "ates", "yorgunluk", "gunes", "hassasiyeti", "sac", "dokulmesi"),
            mustHaveKeywords = listOf("kelebek", "eklem", "yorgunluk"),
            description = "Otoimmün hastalık. Yanaklarda kelebek şeklinde kızarıklık, eklem ağrıları ve kronik yorgunluk.",
            urgency = UrgencyLevel.MODERATE,
            department = "ROMANTOLOJİ / İMMÜNOLOJİ",
            recommendations = listOf("Güneşten korunun (SPF 50+ krem).", "ANA ve anti-dsDNA testleri yaptırın.", "İmmünsüpresif tedavi gerekir.", "Hamilelik planı doktor gözetiminde olmalı.")
        ))

        rules.add(DiseaseRule(
            id = "GENETIC_FIBROMYALGIA",
            title = "Fibromiyalji",
            relatedKeywords = listOf("yaygin", "vucut", "agrisi", "hassas", "nokta", "yorgunluk", "uyku", "bozuklugu", "hafiza", "sorunlari", "sis"),
            mustHaveKeywords = listOf("yaygin", "agri", "yorgunluk"),
            description = "Kronik yaygın vücut ağrısı sendromu. Yorgunluk, uyku bozukluğu ve hafıza problemleri eşlik eder.",
            urgency = UrgencyLevel.LOW,
            department = "ROMANTOLOJİ / FİZİK TEDAVİ",
            recommendations = listOf("Düzenli egzersiz (Yüzme, yürüyüş).", "Stres yönetimi çok önemlidir.", "Uyku hijyeni sağlayın.", "Antidepresanlar ağrıyı azaltabilir.")
        ))

        // 27. KATEGORİ: ACİL DURUMLAR (EK)
        rules.add(DiseaseRule(
            id = "EMERGENCY_CHOKING",
            title = "Boğulma / Hava Yolu Tıkanması",
            relatedKeywords = listOf("yemek", "kacti", "nefes", "alamama", "elleri", "bogaz", "tutma", "konusamama", "morarmma"),
            mustHaveKeywords = listOf("nefes", "alamama", "bogaz"),
            description = "Yemek veya cisim hava yoluna kaçması. Kişi konuşamaz, öksüremez ve boğazını tutar (evrensel boğulma işareti).",
            urgency = UrgencyLevel.CRITICAL,
            department = "ACİL MÜDAHALE",
            recommendations = listOf("Heimlich manevrası yapın (Karından sıkıştırma).", "5 sırt vuruşu, 5 karın basısı (Tekrarla).", "Bilinç kaybederse 112'yi arayın ve CPR başlatın.", "Küçük çocuklarda farklı teknik kullanılır.")
        ))

        rules.add(DiseaseRule(
            id = "EMERGENCY_BURN",
            title = "Yanık (2. veya 3. Derece)",
            relatedKeywords = listOf("yanik", "kabarcik", "beyazlasma", "aci", "deri", "kalkti", "kizariklik", "sislik"),
            mustHaveKeywords = listOf("yanik", "kabarcik", "deri"),
            description = "Sıcak, kimyasal veya elektrik kaynaklı deri hasarı. Kabarcıklar, deri kaybı ve şiddetli ağrı.",
            urgency = UrgencyLevel.HIGH,
            department = "ACİL SERVİS / YANIK ÜNİTESİ",
            recommendations = listOf("Derhal soğuk (ılık) suyla yıkayın (15-20 dakika).", "Buz KULLANMAYIN (Doku hasarını artırır).", "Kabarcıkları patlatmayın.", "Diş macunu, yoğurt gibi şeyler SÜRMEYIN, sadece su kullanın.")
        ))

        rules.add(DiseaseRule(
            id = "EMERGENCY_ELECTRIC_SHOCK",
            title = "Elektrik Çarpması",
            relatedKeywords = listOf("elektrik", "carpma", "bilinc", "kaybi", "yanik", "iz", "kalp", "ritim", "bozuklugu"),
            mustHaveKeywords = listOf("elektrik", "carpma"),
            description = "Elektrik akımının vücuttan geçmesi. Kalp ritmi bozulabilir, iç organ hasarı olabilir.",
            urgency = UrgencyLevel.CRITICAL,
            department = "ACİL SERVİS",
            recommendations = listOf("Önce elektrik kaynağını kapatın.", "Kişiye DOKUNMAYIN (Siz de çarpılabilirsiniz).", "112'yi arayın.", "Bilinçsizse CPR başlatın.", "Dış yaralanma az olsa bile iç hasar olabilir, mutlaka hastaneye gidin.")
        ))
// ===========================================================================
        // EKSTRA PAKET: EKSİK KALAN KLASİKLER
        // ===========================================================================

        // ---------------------------------------------------------------------------
        // 28. KATEGORİ: VİTAMİN VE MİNERAL EKSİKLİKLERİ (ÇOK YAYGIN)
        // ---------------------------------------------------------------------------

        rules.add(DiseaseRule(
            id = "VIT_B12",
            title = "B12 Vitamini Eksikliği",
            relatedKeywords = listOf("unutkanlik", "hafiza", "el", "ayak", "uyusmasi", "yorgunluk", "dil", "yarasi", "sinirlilik", "konsantrasyon"),
            mustHaveKeywords = listOf("unutkanlik", "uyusma", "yorgunluk"),
            description = "Sinir sistemi için kritik olan B12 vitamininin eksikliği. Unutkanlık, uyuşma ve halsizlik yapar.",
            urgency = UrgencyLevel.LOW,
            department = "DAHİLİYE",
            recommendations = listOf("Kan tahlili yaptırın.", "Et, yumurta ve süt ürünleri tüketin.", "Doktor kontrolünde B12 iğnesi veya hapı gerekebilir.")
        ))

        rules.add(DiseaseRule(
            id = "VIT_D",
            title = "D Vitamini Eksikliği",
            relatedKeywords = listOf("kemik", "agrisi", "halsizlik", "terleme", "bas", "depresyon", "sac", "dokulmesi", "eklem"),
            mustHaveKeywords = listOf("kemik", "agri", "halsizlik"),
            description = "Güneş ışığı vitamini eksikliği. Yaygın kemik ağrısı, bağışıklık düşüklüğü ve depresif ruh hali yapar.",
            urgency = UrgencyLevel.LOW,
            department = "DAHİLİYE",
            recommendations = listOf("Güneşli saatlerde 15-20 dk kollarınızı güneşe tutun.", "D vitamini takviyesi alın (Doktor önerisiyle).", "Balık tüketin.")
        ))

        rules.add(DiseaseRule(
            id = "NUTRI_MAGNESIUM",
            title = "Magnezyum Eksikliği",
            relatedKeywords = listOf("kas", "krampi", "goz", "segirmesi", "yorgunluk", "uykusuzluk", "bacak", "agrisi", "kabizlik"),
            mustHaveKeywords = listOf("kramp", "segirme", "kas"),
            description = "Kas ve sinir fonksiyonları için gerekli mineralin eksikliği. Gece krampları ve göz seğirmesi klasiktir.",
            urgency = UrgencyLevel.LOW,
            department = "DAHİLİYE",
            recommendations = listOf("Maden suyu, muz, kuruyemiş ve yeşil sebzeler tüketin.", "Magnezyum takviyesi kasları rahatlatır.")
        ))

        // ---------------------------------------------------------------------------
        // 29. KATEGORİ: UYKU BOZUKLUKLARI
        // ---------------------------------------------------------------------------

        rules.add(DiseaseRule(
            id = "SLEEP_INSOMNIA",
            title = "İnsomnia (Uykusuzluk)",
            relatedKeywords = listOf("uykuya", "dalamama", "gece", "uyanma", "sabah", "yorgun", "kalkma", "gun", "ici", "uyuklama", "sinir"),
            mustHaveKeywords = listOf("uyku", "dalamama", "uyanma"),
            description = "Uykuya dalmakta veya uykuyu sürdürmekte zorluk çekme. Günlük yaşam kalitesini düşürür.",
            urgency = UrgencyLevel.LOW,
            department = "PSİKİYATRİ / NÖROLOJİ",
            recommendations = listOf("Yatmadan 1 saat önce ekran ışığını kesin.", "Kafeini öğleden sonra bırakın.", "Yatak odanızı karanlık ve serin tutun.")
        ))

        rules.add(DiseaseRule(
            id = "SLEEP_APNEA",
            title = "Uyku Apnesi",
            relatedKeywords = listOf("horlama", "nefes", "durmasi", "bogulur", "gibi", "uyanma", "sabah", "bas", "agrisi", "gun", "ici", "uyuklama"),
            mustHaveKeywords = listOf("horlama", "nefes", "durmasi"),
            description = "Uykuda solunumun geçici olarak durması. Şiddetli horlama ve sabah yorgunluğu en belirgin işaretidir.",
            urgency = UrgencyLevel.MODERATE,
            department = "GÖĞÜS HASTALIKLARI / KBB",
            recommendations = listOf("Uyku testi (Polisomnografi) yaptırın.", "Kilo vermek şikayetleri azaltır.", "CPAP cihazı kullanılması gerekebilir.")
        ))

        // ---------------------------------------------------------------------------
        // 30. KATEGORİ: GÖZ SORUNLARI (EKSTRA)
        // ---------------------------------------------------------------------------

        rules.add(DiseaseRule(
            id = "EYE_STYE",
            title = "Arpacık (Hordeolum)",
            relatedKeywords = listOf("goz", "kapagi", "sislik", "kizariklik", "aci", "batma", "sivilce", "gibi", "kirpik", "dibi"),
            mustHaveKeywords = listOf("goz", "sislik", "aci"),
            description = "Göz kapağındaki yağ bezlerinin enfeksiyonu. Ağrılı, kırmızı bir şişlik oluşur.",
            urgency = UrgencyLevel.LOW,
            department = "GÖZ HASTALIKLARI",
            recommendations = listOf("Sıcak pansuman yapın (Günde 3-4 kez).", "Asla sıkmayın veya patlatmayın.", "Bebek şampuanı ile kirpik diplerini temizleyin.")
        ))

        rules.add(DiseaseRule(
            id = "EYE_DRY",
            title = "Göz Kuruluğu",
            relatedKeywords = listOf("gozde", "yanma", "batma", "kum", "varmis", "hissi", "bilgisayar", "kullanimi", "yorgunluk", "kizarma"),
            mustHaveKeywords = listOf("goz", "yanma", "batma", "kum"),
            description = "Gözyaşı yetersizliği veya kalitesizliği. Bilgisayar kullanımı ve klimalı ortamlar tetikler.",
            urgency = UrgencyLevel.LOW,
            department = "GÖZ HASTALIKLARI",
            recommendations = listOf("Suni gözyaşı damlası kullanın.", "20-20-20 kuralını uygulayın (Her 20 dk'da bir 20 saniye uzağa bak).", "Bol su için.")
        ))

        // ---------------------------------------------------------------------------
        // 31. KATEGORİ: CİLT SORUNLARI (EKSTRA KLASİKLER)
        // ---------------------------------------------------------------------------

        rules.add(DiseaseRule(
            id = "DERMA_ACNE",
            title = "Akne Vulgaris (Sivilce)",
            relatedKeywords = listOf("sivilce", "yuzde", "yaglanma", "siyah", "nokta", "iltihapli", "kizarik", "iz", "ergenlik"),
            mustHaveKeywords = listOf("sivilce", "yuz", "yaglanma"),
            description = "Kıl köklerinin ve yağ bezlerinin tıkanması. Hormonal değişimler ve stres tetikler.",
            urgency = UrgencyLevel.LOW,
            department = "CİLDİYE",
            recommendations = listOf("Yüzünüzü günde iki kez yıkayın.", "Sivilceleri sıkmayın (İz kalır).", "Yağsız nemlendirici kullanın.", "Dermatologdan krem/ilaç tedavisi alın.")
        ))

        rules.add(DiseaseRule(
            id = "DERMA_FUNGUS",
            title = "Ayak Mantarı (Tinea Pedis)",
            relatedKeywords = listOf("ayak", "parmak", "arasi", "kasinti", "beyazlama", "soyulma", "koku", "yanma", "nemli"),
            mustHaveKeywords = listOf("ayak", "kasinti", "soyulma"),
            description = "Ayak parmak aralarında kaşıntı, soyulma ve koku yapan mantar enfeksiyonu.",
            urgency = UrgencyLevel.LOW,
            department = "CİLDİYE",
            recommendations = listOf("Ayaklarınızı kuru tutun.", "Ortak terlik kullanmayın.", "Antifungal krem kullanın.", "Pamuklu çorap giyin.")
        ))

        rules.add(DiseaseRule(
            id = "DERMA_SUNBURN",
            title = "Güneş Yanığı",
            relatedKeywords = listOf("gunes", "sonrasi", "kizariklik", "aci", "yanma", "deri", "soyulmasi", "sicak", "banyo"),
            mustHaveKeywords = listOf("gunes", "yanma", "kizarik"),
            description = "Güneş ışınlarına (UV) aşırı maruz kalma sonucu deri hasarı.",
            urgency = UrgencyLevel.LOW,
            department = "CİLDİYE / ECZANE",
            recommendations = listOf("Soğuk duş alın.", "Aloe vera veya yanık kremi sürün.", "Bol su için.", "Yoğurt sürmeyin (Enfeksiyon riski).")
        ))

        // ---------------------------------------------------------------------------
        // 32. KATEGORİ: ERKEK SAĞLIĞI (ANDROLOJİ)
        // ---------------------------------------------------------------------------

        rules.add(DiseaseRule(
            id = "MALE_TORSION",
            title = "Testis Torsiyonu (Dönmesi)",
            relatedKeywords = listOf("testis", "yumurta", "siddetli", "agri", "sislik", "kizariklik", "bulanti", "karin", "agrisi", "ani"),
            mustHaveKeywords = listOf("testis", "ani", "agri"),
            description = "Testisin kendi etrafında dönerek kan akışını kesmesi. Acil müdahale edilmezse organ kaybı olur.",
            urgency = UrgencyLevel.CRITICAL,
            department = "ÜROLOJİ / ACİL",
            recommendations = listOf("DERHAL ACİL servise gidin.", "Zaman çok kritiktir (İlk 6 saat).", "Asla beklemeyin.")
        ))

        rules.add(DiseaseRule(
            id = "MALE_VARICOCELE",
            title = "Varikosel",
            relatedKeywords = listOf("testis", "damar", "genislemesi", "agri", "kısırlık", "torba", "solucan", "gibi", "sislik"),
            mustHaveKeywords = listOf("testis", "damar", "agri"),
            description = "Testis damarlarının varisleşmesi (genişlemesi). Ağrı ve kısırlığa neden olabilir.",
            urgency = UrgencyLevel.LOW,
            department = "ÜROLOJİ",
            recommendations = listOf("Dar iç çamaşırı giymeyin.", "Ayakta uzun süre kalmaktan kaçının.", "Üroloji uzmanına muayene olun (Sperm kalitesini etkileyebilir).")
        ))

        // ---------------------------------------------------------------------------
        // 33. KATEGORİ: GENEL ÇEVRESEL VE SİSTEMİK DURUMLAR
        // ---------------------------------------------------------------------------

        rules.add(DiseaseRule(
            id = "GEN_FOOD_POISON",
            title = "Gıda Zehirlenmesi",
            relatedKeywords = listOf("bozuk", "yemek", "kusma", "ishal", "karin", "agrisi", "ates", "halsizlik", "tavuk", "balik"),
            mustHaveKeywords = listOf("kusma", "ishal", "yemek"),
            description = "Bozuk veya bakterili gıda tüketimi sonrası mide-bağırsak enfeksiyonu.",
            urgency = UrgencyLevel.MODERATE,
            department = "ACİL SERVİS / DAHİLİYE",
            recommendations = listOf("Su kaybını önlemek için bol sıvı alın.", "İshal kesici ilaç hemen almayın (Vücut toksini atmalı).", "Kanlı ishal veya yüksek ateş varsa doktora gidin.")
        ))

        rules.add(DiseaseRule(
            id = "GEN_HEAT_STROKE",
            title = "Sıcak Çarpması",
            relatedKeywords = listOf("gunes", "altinda", "bas", "donmesi", "bayilma", "ates", "deri", "kurulugu", "hizli", "nabiz", "susuzluk"),
            mustHaveKeywords = listOf("gunes", "sicak", "bayilma"),
            description = "Aşırı sıcağa maruz kalma sonucu vücut ısısının tehlikeli düzeyde artması.",
            urgency = UrgencyLevel.HIGH,
            department = "ACİL SERVİS",
            recommendations = listOf("Hemen serin bir yere geçin.", "Soğuk kompres uygulayın (Koltuk altı, boyun).", "Bilinci açıksa su içirin.", "Bilinci kapalıysa 112'yi arayın.")
        ))

        rules.add(DiseaseRule(
            id = "GEN_DEHYDRATION",
            title = "Dehidrasyon (Susuzluk)",
            relatedKeywords = listOf("agiz", "kurulugu", "koyu", "idrar", "bas", "agrisi", "halsizlik", "deri", "elastikiyeti", "susama"),
            mustHaveKeywords = listOf("susuzluk", "idrar", "agiz", "kuru"),
            description = "Vücudun ihtiyacı olandan fazla sıvı kaybetmesi. Böbrekleri ve tansiyonu etkiler.",
            urgency = UrgencyLevel.MODERATE,
            department = "DAHİLİYE",
            recommendations = listOf("Yudum yudum bol su için.", "Elektrolitli içecekler (Ayran, maden suyu) tüketin.", "İdrar renginiz açılana kadar sıvı alımına devam edin.")
        ))

        // ---------------------------------------------------------------------------
        // 34. KATEGORİ: TRAVMA VE YARALANMALAR (KÜÇÜK ÇAPLI)
        // ---------------------------------------------------------------------------

        rules.add(DiseaseRule(
            id = "TRAUMA_SPRAIN",
            title = "Burkulma ve İncinme",
            relatedKeywords = listOf("ayak", "bilegi", "burkulma", "sislik", "morarma", "agri", "uzerine", "basamama", "ters", "hareket"),
            mustHaveKeywords = listOf("burkulma", "sislik", "agri"),
            description = "Eklemin ters hareketi sonucu bağların zedelenmesi. Şişlik ve ağrı yapar.",
            urgency = UrgencyLevel.LOW,
            department = "ORTOPEDİ",
            recommendations = listOf("RICE yöntemi uygulayın: Rest (Dinlenme), Ice (Buz), Compression (Bandaj), Elevation (Yukarı kaldırma).", "İlk 24 saat sıcak uygulama yapmayın.")
        ))

        rules.add(DiseaseRule(
            id = "TRAUMA_CUT",
            title = "Kesik ve Açık Yara",
            relatedKeywords = listOf("bicak", "kesigi", "cam", "kanama", "yara", "dikis", "enfeksiyon", "sizlama"),
            mustHaveKeywords = listOf("kesik", "kanama", "yara"),
            description = "Deri bütünlüğünün bozulması. Kanama kontrolü ve enfeksiyon riski önemlidir.",
            urgency = UrgencyLevel.MODERATE,
            department = "ACİL SERVİS / AİLE HEKİMİ",
            recommendations = listOf("Yarayı temiz su ve sabunla yıkayın.", "Temiz bir bezle baskı uygulayarak kanamayı durdurun.", "Yara derin veya kirliyse tetanoz aşısı gerekebilir.")
        ))

        // ===========================================================================
        // EKSTRA PAKET: SPESİFİK AĞRI YÖNETİMİ (AYAK, SIRT VE KAS)
        // ===========================================================================

        // ---------------------------------------------------------------------------
        // 35. KATEGORİ: AYAK VE TOPUK AĞRILARI
        // ---------------------------------------------------------------------------

        rules.add(DiseaseRule(
            id = "ORTHO_PLANTAR",
            title = "Topuk Dikeni / Plantar Fasiit",
            relatedKeywords = listOf("topuk", "agrisi", "sabah", "ilk", "basma", "ayak", "tabani", "yurume", "zorlugu", "bicak", "batmasi"),
            mustHaveKeywords = listOf("topuk", "sabah", "basma"),
            description = "Ayak tabanındaki zarın iltihaplanması. Özellikle sabah yataktan kalkınca ilk basışta şiddetli topuk ağrısı olur.",
            urgency = UrgencyLevel.LOW,
            department = "ORTOPEDİ",
            recommendations = listOf("Silikon topukluk kullanın.", "Ayağınızın altına soğuk su şişesi koyup yuvarlayın (Masaj).", "Evde terliksiz gezmeyin.", "Kilo vermek yükü azaltır.")
        ))

        rules.add(DiseaseRule(
            id = "URO_GOUT",
            title = "Gut Hastalığı (Pagra)",
            relatedKeywords = listOf("ayak", "basparmak", "sislik", "kizariklik", "gece", "agrisi", "et", "yeme", "alkol", "dokunamama"),
            mustHaveKeywords = listOf("basparmak", "sislik", "agri"),
            description = "Kanda ürik asit yüksekliği. Genellikle ayak başparmağında ani, çok şiddetli ağrı, şişlik ve kızarıklık yapar.",
            urgency = UrgencyLevel.MODERATE,
            department = "DAHİLİYE / ROMATOLOJİ",
            recommendations = listOf("Kırmızı et ve sakatat tüketimini kesin.", "Bol su için (Ürik asidi atmak için).", "Alkolden uzak durun.", "Ağrılı bölgeye buz uygulayın.")
        ))

        rules.add(DiseaseRule(
            id = "ORTHO_FLATFOOT",
            title = "Düz Tabanlık (Pes Planus)",
            relatedKeywords = listOf("ayak", "tabani", "agrisi", "cabuk", "yorulma", "kavis", "cokmesi", "iceri", "basma", "baldir", "agrisi"),
            mustHaveKeywords = listOf("ayak", "tabani", "yorulma"),
            description = "Ayak taban kavisinin çökmesi. Uzun süre ayakta kalınca ayak ve bacak ağrısı yapar.",
            urgency = UrgencyLevel.LOW,
            department = "ORTOPEDİ",
            recommendations = listOf("Ortopedik tabanlık kullanın.", "Uygun ayakkabı seçimi yapın.", "Çıplak ayakla kumda yürümek faydalıdır.")
        ))

        // ---------------------------------------------------------------------------
        // 36. KATEGORİ: SIRT VE KAS AĞRILARI (MEKANİK)
        // ---------------------------------------------------------------------------

        rules.add(DiseaseRule(
            id = "ORTHO_UPPER_BACK",
            title = "Mekanik Sırt Ağrısı (Kulunç/Miyofasiyal Ağrı)",
            relatedKeywords = listOf("sirt", "agrisi", "kurek", "kemigi", "kulunc", "kas", "dugumu", "bilgisayar", "klima", "cereyan", "yel"),
            mustHaveKeywords = listOf("sirt", "agri", "kas"),
            description = "Duruş bozukluğu, stres veya soğukta kalma sonucu sırt kaslarında oluşan ağrılı düğümler.",
            urgency = UrgencyLevel.LOW,
            department = "FİZİK TEDAVİ",
            recommendations = listOf("Sıcak duş ve sıcak su torbası iyi gelir.", "Dik durmaya çalışın.", "Magnezyum takviyesi kasları gevşetir.", "Germe egzersizleri yapın.")
        ))

        rules.add(DiseaseRule(
            id = "ORTHO_SCIATICA",
            title = "Siyatik Ağrısı",
            relatedKeywords = listOf("belden", "bacaga", "vuran", "agri", "kalca", "elektrik", "carpmasi", "cekme", "uyusma", "topallama"),
            mustHaveKeywords = listOf("bel", "bacak", "agri"),
            description = "Siyatik sinirinin sıkışması. Belden başlayıp kalçadan topuğa kadar inen elektrik çarpması tarzında ağrı.",
            urgency = UrgencyLevel.MODERATE,
            department = "BEYİN CERRAHİ / FİZİK TEDAVİ",
            recommendations = listOf("Sert zeminde yatın.", "Ani hareketlerden kaçının.", "Kalçadan iğne (Enjeksiyon) gerekebilir.", "Sinir germe egzersizleri yapın.")
        ))
        rules.add(DiseaseRule(
            id = "RESP_PULMONARY_EMBOLISM",
            title = "Pulmoner Emboli (Akciğer Pıhtısı)",
            relatedKeywords = listOf("nefes", "darligi", "gogus", "batma", "ani", "kanli", "oksuruk", "bacak", "pıhtı", "ucus", "ameliyat", "hareketsizlik"),
            mustHaveKeywords = listOf("nefes", "ani", "gogus"),
            description = "Bacak damarındaki pıhtının akciğere gitmesi. Ani başlayan nefes darlığı, bıçak saplanır tarzda göğüs ağrısı ve kanlı balgam görülebilir.",
            urgency = UrgencyLevel.CRITICAL,
            department = "ACİL / GÖĞÜS HASTALIKLARI",
            recommendations = listOf("DERHAL 112'yi arayın.", "Hareket etmeyin.", "Oksijen desteği gerekir.", "Pıhtı eritici tedavi (trombolitik) gerekebilir.")
        ))
        rules.add(DiseaseRule(
            id = "CARDIO_PERICARDITIS",
            title = "Perikardit (Kalp Zarının İltihabı)",
            relatedKeywords = listOf("gogus", "batma", "derin", "nefes", "alinca", "artan", "oturunca", "azalan", "sirt", "sol", "noktasi"),
            mustHaveKeywords = listOf("gogus", "nefes", "batma"),
            description = "Derin nefes almakla artan, öne eğilince azalan göğüs ağrısı tipiktir. Kalp zarının iltihaplanması.",
            urgency = UrgencyLevel.HIGH,
            department = "KARDİYOLOJİ",
            recommendations = listOf("Ağrı pozisyonla değişiyorsa perikardit şüphesi yüksektir.", "EKG ve EKO yapılmalıdır.", "İltihap giderici tedavi uygulanır.")
        ))
        rules.add(DiseaseRule(
            id = "RESP_PNEUMOTHORAX",
            title = "Pnömotoraks (Akciğer Çökmesi)",
            relatedKeywords = listOf("ani", "gogus", "batma", "nefes", "almak", "zor", "sigara", "uzun", "boylu", "zayif", "klik", "ses"),
            mustHaveKeywords = listOf("ani", "gogus", "nefes"),
            description = "Akciğer zarına hava dolmasıyla akciğerin sönmesi. Ani başlayan batıcı ağrı ve nefes darlığı görülür.",
            urgency = UrgencyLevel.CRITICAL,
            department = "ACİL SERVİS",
            recommendations = listOf("Hemen acile gidin.", "Oksijen desteği gerekir.", "Gerekli durumlarda göğüs tüpü takılır.")
        ))
        rules.add(DiseaseRule(
            id = "GASTRO_CHOLECYSTITIS",
            title = "Akut Kolesistit (Safra Kesesi İltihabı)",
            relatedKeywords = listOf("sag", "ust", "karin", "agri", "yemekten", "sonra", "yagli", "bulanti", "kusma", "sirt", "sag", "omuz"),
            mustHaveKeywords = listOf("sag", "ust", "karin"),
            description = "Yağlı yemek sonrası sağ üst karın ağrısı, sırta vuran ağrı ve bulantı.",
            urgency = UrgencyLevel.MODERATE,
            department = "GENEL CERRAHİ / GASTROENTEROLOJİ",
            recommendations = listOf("Yağlı gıdalardan kaçının.", "Ultrason çekilmesi gerekir.", "Ağrı artarsa acile başvurun.")
        ))
        rules.add(DiseaseRule(
            id = "GYNE_PID",
            title = "Pelvik Enflamatuvar Hastalık (PID)",
            relatedKeywords = listOf("kasik", "agrisi", "ates", "akinti", "kotu", "koku", "cinsel", "ilişki", "sonrasi", "agri"),
            mustHaveKeywords = listOf("kasik", "akinti", "ates"),
            description = "Fallop tüplerinde enfeksiyon. Cinsel ilişki sonrası artan kasık ağrısı ve kötü kokulu akıntı görülür.",
            urgency = UrgencyLevel.HIGH,
            department = "KADIN DOĞUM",
            recommendations = listOf("Antibiyotik tedavisi gerekir.", "Tedavi edilmezse kısırlığa yol açabilir.", "Acil muayene şarttır.")
        ))
        rules.add(DiseaseRule(
            id = "GASTRO_IBD",
            title = "İnflamatuvar Bağırsak Hastalığı (Crohn / Ülseratif Kolit)",
            relatedKeywords = listOf("ishal", "kanli", "diski", "karin", "kramp", "kilo", "kaybi", "ates", "yorgunluk", "tekrarlayan"),
            mustHaveKeywords = listOf("ishal", "kan", "karin"),
            description = "Kronik bağırsak iltihabı. Kanlı ishal, karın krampları ve kilo kaybı tipiktir.",
            urgency = UrgencyLevel.MODERATE,
            department = "GASTROENTEROLOJİ",
            recommendations = listOf("Kolonoskopi yapılmalıdır.", "Bağırsak iltihap düzeyi kontrol edilir.", "Diyet ve ilaç tedavisi gerekebilir.")
        ))
        rules.add(DiseaseRule(
            id = "ENDO_HYPOGLYCEMIA",
            title = "Hipoglisemi (Kan Şekeri Düşmesi)",
            relatedKeywords = listOf("titreme", "terleme", "aclik", "bas", "donmesi", "bayilma", "bulanık", "gorme", "sinirlilik"),
            mustHaveKeywords = listOf("aclik", "titreme", "bayilma"),
            description = "Kan şekerinin düşmesi sonucu terleme, titreme ve bayılma hissi olur.",
            urgency = UrgencyLevel.MODERATE,
            department = "DAHİLİYE / ENDOKRİN",
            recommendations = listOf("Hızlı şeker alın (Meyve suyu, şeker).", "Diyabet hastasıysanız insülin dozunuzu kontrol edin.", "Bayılma olursa 112'yi arayın.")
        ))
        rules.add(DiseaseRule(
            id = "NEURO_CLUSTER_HEADACHE",
            title = "Küme Baş Ağrısı",
            relatedKeywords = listOf("tek", "goz", "arkasi", "yanma", "patlama", "burun", "akmasi", "goz", "yasi", "gece", "uyanma"),
            mustHaveKeywords = listOf("goz", "tek", "agri"),
            description = "Göz çevresinde tek taraflı, dayanılmaz, patlayıcı tarzda baş ağrısı. Geceleri uykudan uyandırır.",
            urgency = UrgencyLevel.MODERATE,
            department = "NÖROLOJİ",
            recommendations = listOf("Oksijen tedavisi atakları hızlı keser.", "Tripan ilaçlar kullanılabilir.", "Tetikleyici: Alkol, sigara.")
        ))
        rules.add(DiseaseRule(
            id = "GEN_FISH_HISTAMINE",
            title = "Histamin Balık Zehirlenmesi",
            relatedKeywords = listOf("balik", "yemekten", "sonra", "kizarma", "kasinti", "bulanti", "ishal", "bas", "donmesi"),
            mustHaveKeywords = listOf("balik", "kizarma", "kasinti"),
            description = "Taze olmayan balık tüketimi sonrası histamin artışına bağlı ani kızarma, kaşıntı ve mide şikayetleri.",
            urgency = UrgencyLevel.MODERATE,
            department = "ACİL / ENFEKSİYON",
            recommendations = listOf("Antihistaminik ilaç kullanılır.", "Sıvı tüketimini artırın.", "Şikayet ağırsa acile gidin.")
        ))
        // ===========================================================================
        // EKSTRA PAKET 2: EKSİK KALAN ÖZEL DURUMLAR (GÖZ, ROMATOLOJİ, DOLAŞIM)
        // ===========================================================================

        rules.add(DiseaseRule(
            id = "EYE_GLAUCOMA",
            title = "Glokom (Göz Tansiyonu)",
            relatedKeywords = listOf("goz", "agrisi", "bas", "agrisi", "bulanık", "gorme", "isik", "halesi", "kusma", "sertlik"),
            mustHaveKeywords = listOf("goz", "agri", "bulanık"),
            description = "Göz içi basıncının artması. Sinsi ilerler ama kriz anında şiddetli göz ve baş ağrısı, ışık etrafında hareler görme yapar.",
            urgency = UrgencyLevel.HIGH,
            department = "GÖZ HASTALIKLARI",
            recommendations = listOf("Göz tansiyonu ölçümü şarttır.", "Tedavi edilmezse körlüğe neden olabilir.", "Ani görme kaybında ACİL'e gidin.")
        ))

        rules.add(DiseaseRule(
            id = "EYE_CATARACT",
            title = "Katarakt",
            relatedKeywords = listOf("bulanık", "gorme", "sisli", "perde", "inmesi", "renkler", "soluk", "gece", "körlüğü", "cift", "gorme"),
            mustHaveKeywords = listOf("bulanık", "sisli", "perde"),
            description = "Göz merceğinin şeffaflığını yitirmesi. Görmede yavaş yavaş azalma, sisli görme ve renklerin soluklaşması.",
            urgency = UrgencyLevel.LOW,
            department = "GÖZ HASTALIKLARI",
            recommendations = listOf("Ameliyat tek kesin çözümdür.", "Güneş gözlüğü kullanımı ilerlemeyi yavaşlatabilir.", "Göz muayenesi olun.")
        ))

        rules.add(DiseaseRule(
            id = "RHEUM_RA",
            title = "Romatoid Artrit (İltihaplı Romatizma)",
            relatedKeywords = listOf("sabah", "tutuklugu", "eklem", "agrisi", "sislik", "el", "bilegi", "parmak", "yorgunluk", "simetrik"),
            mustHaveKeywords = listOf("sabah", "tutuklugu", "eklem", "sislik"),
            description = "Bağışıklık sisteminin eklemlere saldırması. Özellikle sabahları 1 saatten uzun süren eklem tutukluğu ve şişlik tipiktir.",
            urgency = UrgencyLevel.MODERATE,
            department = "ROMATOLOJİ",
            recommendations = listOf("Erken tedavi eklem hasarını önler.", "Sigara hastalığı şiddetlendirir.", "Düzenli egzersiz ve anti-inflamatuar beslenme önemlidir.")
        ))

        rules.add(DiseaseRule(
            id = "GYNE_PCOS",
            title = "Polikistik Over Sendromu (PCOS)",
            relatedKeywords = listOf("adet", "duzensizligi", "tuy", "lanma", "sivilce", "kilo", "alma", "sac", "dokulmesi", "gec", "adet"),
            mustHaveKeywords = listOf("adet", "duzensiz", "tuy"),
            description = "Hormonal dengesizlik. Adet düzensizliği, aşırı tüylenme ve yumurtalıklarda çok sayıda kist görünümü.",
            urgency = UrgencyLevel.LOW,
            department = "KADIN DOĞUM / ENDOKRİN",
            recommendations = listOf("Kilo vermek belirtileri %50 azaltır.", "Şeker ve karbonhidratı azaltın.", "Hormon testleri yapılmalıdır.")
        ))

        rules.add(DiseaseRule(
            id = "CARDIO_RAYNAUD",
            title = "Raynaud Fenomeni (Beyaz Parmak)",
            relatedKeywords = listOf("parmak", "beyazlama", "morarma", "kizarma", "soguk", "hissizlik", "uyusma", "karincalanma", "stres"),
            mustHaveKeywords = listOf("parmak", "beyaz", "soguk"),
            description = "Soğuk veya stresle el/ayak parmaklarının kan damarlarının büzüşmesi. Renk değişimi (Beyaz-Mor-Kızıl) görülür.",
            urgency = UrgencyLevel.LOW,
            department = "KALP DAMAR / ROMATOLOJİ",
            recommendations = listOf("Ellerinizi sıcak tutun (Eldiven).", "Sigara kesinlikle içmeyin (Damarları daraltır).", "Stres yönetimi yapın.")
        ))

        rules.add(DiseaseRule(
            id = "INF_RABIES",
            title = "Kuduz Şüphesi (Hayvan Isırığı)",
            relatedKeywords = listOf("kopek", "kedi", "isirigi", "tirmalama", "yara", "agiz", "kopurmesi", "sudam", "korkma", "yarasalar"),
            mustHaveKeywords = listOf("isirik", "hayvan"),
            description = "Hayvan ısırığı veya tırmalaması sonucu bulaşan ölümcül virüs. Hemen müdahale edilmezse %100 öldürücüdür.",
            urgency = UrgencyLevel.CRITICAL,
            department = "ACİL SERVİS / ENFEKSİYON",
            recommendations = listOf("DERHAL yarayı sabunlu suyla 15 dk yıkayın.", "Hemen acile gidip aşı olun.", "Hayvanı gözlem altına aldırtın.", "Zaman kaybetmeyin.")
        ))

        rules.add(DiseaseRule(
            id = "GEN_TETANUS",
            title = "Tetanoz Şüphesi",
            relatedKeywords = listOf("pasli", "civi", "demir", "kesik", "toprak", "yara", "cene", "kitlenmesi", "kasilma"),
            mustHaveKeywords = listOf("pasli", "yara", "kasilma"),
            description = "Kirli veya paslı yaralanmalarla bulaşan bakteri. Çene kilitlenmesi ve şiddetli kasılmalar yapar.",
            urgency = UrgencyLevel.HIGH,
            department = "ACİL SERVİS",
            recommendations = listOf("Son aşınızın üzerinden 5-10 yıl geçtiyse hemen aşı olun.", "Yarayı oksijenli suyla temizleyin.", "Kasılma başlarsa hemen 112'yi arayın.")
        ))

        return rules
    }
}
