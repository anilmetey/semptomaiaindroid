package com.semptom.ai.data.repository

import android.content.Context
import com.semptom.ai.domain.model.Symptom
import com.semptom.ai.domain.model.SymptomSelection
import com.semptom.ai.domain.model.UserProfile
import com.semptom.ai.domain.model.AgeGroup
import com.semptom.ai.domain.model.SymptomCategory
import com.semptom.ai.domain.repository.ModelRepository
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TFLiteModelRepository @Inject constructor(
    private val context: Context
) : ModelRepository {
    
    private var interpreter: Interpreter? = null
    private var initialized = false
    
    override suspend fun initialize() {
        try {
            val model = loadModelFile()
            interpreter = Interpreter(model)
            initialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override suspend fun getSymptoms(): List<Symptom> {
        return listOf(
            Symptom("headache", "Baş Ağrısı", "Genel baş ağrısı şikayeti", SymptomCategory.HEAD, 1),
            Symptom("fever", "Ateş", "Yüksek vücut sıcaklığı", SymptomCategory.GENERAL, 2),
            Symptom("cough", "Öksürük", "Kuru veya balgamlı öksürük", SymptomCategory.RESPIRATORY, 3),
            Symptom("fatigue", "Yorgunluk", "Halsizlik ve enerji düşüklüğü", SymptomCategory.GENERAL, 1),
            Symptom("nausea", "Mide Bulantısı", "Kusma hissi", SymptomCategory.GASTROINTESTINAL, 2),
            Symptom("chest_pain", "Göğüs Ağrısı", "Göğüs bölgesinde ağrı", SymptomCategory.CHEST, 5),
            Symptom("shortness_breath", "Nefes Darlığı", "Nefes almakta zorluk", SymptomCategory.RESPIRATORY, 5),
            Symptom("dizziness", "Baş Dönmesi", "Denge bozukluğu hissi", SymptomCategory.NEUROLOGICAL, 2),
            Symptom("sore_throat", "Boğaz Ağrısı", "Boğazda ağrı ve tahriş", SymptomCategory.EAR_NOSE_THROAT, 2),
            Symptom("runny_nose", "Burun Akıntısı", "Burundan sıvı akması", SymptomCategory.EAR_NOSE_THROAT, 1),
            Symptom("muscle_pain", "Kas Ağrısı", "Kaslarda ağrı ve hassasiyet", SymptomCategory.MUSCULOSKELETAL, 2),
            Symptom("joint_pain", "Eklem Ağrısı", "Eklem bölgelerinde ağrı", SymptomCategory.MUSCULOSKELETAL, 2),
            Symptom("stomach_pain", "Karın Ağrısı", "Karın bölgesinde ağrı", SymptomCategory.ABDOMEN, 3),
            Symptom("diarrhea", "İshal", "Sulu ve sık dışkılama", SymptomCategory.GASTROINTESTINAL, 2),
            Symptom("constipation", "Kabızlık", "Dışkılama zorluğu", SymptomCategory.GASTROINTESTINAL, 1),
            Symptom("loss_of_appetite", "İştah Kaybı", "Yemek yeme isteğinin azalması", SymptomCategory.GASTROINTESTINAL, 1),
            Symptom("weight_loss", "Kilo Kaybı", "İstenmeyen kilo düşüklüğü", SymptomCategory.GENERAL, 3),
            Symptom("weight_gain", "Kilo Artışı", "İstenmeyen kilo artışı", SymptomCategory.GENERAL, 2),
            Symptom("insomnia", "Uykusuzluk", "Uyku güçlüğü", SymptomCategory.PSYCHIATRIC, 2),
            Symptom("excessive_thirst", "Aşırı Susuzluk", "Sürekli su içme ihtiyacı", SymptomCategory.GENERAL, 3),
            Symptom("frequent_urination", "Sık İdrar", "Artmış idrar sıklığı", SymptomCategory.GENITOURINARY, 2),
            Symptom("skin_rash", "Cilt Döküntüsü", "Ciltte kızarıklık veya leke", SymptomCategory.SKIN, 2),
            Symptom("itching", "Kaşıntı", "Ciltte kaşıntı hissi", SymptomCategory.SKIN, 1),
            Symptom("swelling", "Şişlik", "Vücut bölgelerinde ödem", SymptomCategory.GENERAL, 3),
            Symptom("numbness", "Uyuşma", "His kaybı veya uyuşma", SymptomCategory.NEUROLOGICAL, 2),
            Symptom("tingling", "Karıncalanma", "Ciltte karıncalanma hissi", SymptomCategory.NEUROLOGICAL, 1),
            Symptom("blurred_vision", "Bulanık Görme", "Net görüş bozukluğu", SymptomCategory.EYE, 3),
            Symptom("hearing_loss", "İşitme Kaybı", "Duyma güçlüğü", SymptomCategory.EAR_NOSE_THROAT, 4),
            Symptom("ear_pain", "Kulak Ağrısı", "Kulakta ağrı", SymptomCategory.EAR_NOSE_THROAT, 3),
            Symptom("nosebleed", "Burun Kanaması", "Burundan kan gelmesi", SymptomCategory.EAR_NOSE_THROAT, 2),
            Symptom("bleeding_gums", "Diş Eti Kanaması", "Diş etlerinden kanama", SymptomCategory.EAR_NOSE_THROAT, 1),
            Symptom("hair_loss", "Saç Dökülmesi", "Saçların aşırı dökülmesi", SymptomCategory.SKIN, 1)
        )
    }
    
    override suspend fun evaluateTriage(
        selections: List<SymptomSelection>,
        profile: UserProfile
    ): Any? {
        val features = encodeFeatures(selections, profile)
        val chestPain = selections.any { it.symptom.id == "chest_pain" }
        val shortBreath = selections.any { it.symptom.id == "shortness_breath" }
        val severe = selections.any { it.symptom.severity >= 5 }
        val ageBoost = if (profile.ageGroup == AgeGroup.SENIOR) 1 else 0
        val triageScore = (if (chestPain) 2 else 0) + (if (shortBreath) 2 else 0) + (if (severe) 1 else 0) + ageBoost
        
        return if (triageScore >= 3) {
            mapOf("triage" to true, "score" to triageScore, "features" to features.size)
        } else null
    }
    
    override suspend fun runInference(
        selections: List<SymptomSelection>,
        profile: UserProfile
    ): Any? {
        if (!initialized) initialize()
        
        try {
            val features = encodeFeatures(selections, profile)
            val inputBuffer = ByteBuffer.allocateDirect(4 * features.size)
            inputBuffer.order(ByteOrder.nativeOrder())
            inputBuffer.rewind()
            
            features.forEach { inputBuffer.putFloat(it) }
            
            val outputBuffer = Array(1) { FloatArray(3) }
            
            interpreter?.run(inputBuffer, outputBuffer)
            
            val probabilities = outputBuffer[0]
            val sum = probabilities.sum().takeIf { it > 0f } ?: 1f
            
            return listOf(
                mapOf("id" to "cold", "probability" to (probabilities[0] / sum)),
                mapOf("id" to "flu", "probability" to (probabilities[1] / sum)),
                mapOf("id" to "allergy", "probability" to (probabilities[2] / sum))
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return fallbackInference(selections, profile)
        }
    }
    
    private fun fallbackInference(
        selections: List<SymptomSelection>,
        profile: UserProfile
    ): Any? {
        val cough = selections.any { it.symptom.id == "cough" }
        val fever = selections.any { it.symptom.id == "fever" }
        val runny = selections.any { it.symptom.id == "runny_nose" }

        var cold = 0.3f
        var flu = 0.3f
        var allergy = 0.4f

        if (cough) { cold += 0.1f; flu += 0.15f }
        if (fever) { flu += 0.25f; cold += 0.05f }
        if (runny) { cold += 0.2f; allergy += 0.1f }

        when (profile.ageGroup) {
            AgeGroup.CHILD -> { flu *= 1.1f; allergy *= 1.05f }
            AgeGroup.SENIOR -> { flu *= 1.15f; cold *= 1.05f }
            else -> {}
        }

        val sum = (cold + flu + allergy).takeIf { it > 0f } ?: 1f
        return listOf(
            mapOf("id" to "cold", "probability" to (cold / sum)),
            mapOf("id" to "flu", "probability" to (flu / sum)),
            mapOf("id" to "allergy", "probability" to (allergy / sum))
        )
    }
    
    private fun encodeFeatures(selections: List<SymptomSelection>, profile: UserProfile): FloatArray {
        val symptomVec = FloatArray(32) { 0f }
        selections.take(16).forEachIndexed { idx, sel ->
            symptomVec[idx] = sel.symptom.severity.coerceIn(1,5).toFloat() / 5f
        }
        val ageOneHot = when (profile.ageGroup) {
            AgeGroup.CHILD -> floatArrayOf(1f,0f,0f,0f)
            AgeGroup.TEEN -> floatArrayOf(0f,1f,0f,0f)
            AgeGroup.ADULT -> floatArrayOf(0f,0f,1f,0f)
            AgeGroup.SENIOR -> floatArrayOf(0f,0f,0f,1f)
        }
        val chronicCount = profile.chronicDiseases.size.coerceAtMost(5) / 5f
        val allergyCount = profile.allergies.size.coerceAtMost(5) / 5f
        val profileVec = ageOneHot + floatArrayOf(chronicCount, allergyCount)
        return symptomVec + profileVec
    }
    
    private fun loadModelFile(): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd("model.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}