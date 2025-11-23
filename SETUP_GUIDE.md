# 🚀 SemptomAI - Kurulum Rehberi

## 📋 İçindekiler

1. [Gereksinimler](#gereksinimler)
2. [Proje Kurulumu](#proje-kurulumu)
3. [Model Eğitimi](#model-eğitimi)
4. [Android Uygulama](#android-uygulama)
5. [Google Maps Entegrasyonu](#google-maps-entegrasyonu)
6. [Çalıştırma](#çalıştırma)
7. [Sorun Giderme](#sorun-giderme)

## 🔧 Gereksinimler

### Android Geliştirme

- **Android Studio**: Hedgehog | 2023.1.1 veya üzeri
- **JDK**: 17
- **Android SDK**: 34 (Android 14)
- **Minimum SDK**: 24 (Android 7.0)

### Python (Model Eğitimi)

- **Python**: 3.8 - 3.11
- **pip**: En son versiyon

### Diğer

- **Git**: Versiyon kontrolü için
- **Google Cloud Account**: Maps API için (opsiyonel)

## 📦 Proje Kurulumu

### 1. Projeyi Klonlayın veya İndirin

```bash
cd "/Users/anilmete/StudioProjects/Yapay Zeka Destekli Semptom Analizoru ve Saglik Asistani"
```

### 2. Gradle Wrapper'ı Oluşturun

```bash
# Gradle wrapper dosyalarını oluştur
gradle wrapper --gradle-version 8.2
```

### 3. Local Properties Dosyasını Oluşturun

```bash
# Template'i kopyalayın
cp local.properties.template local.properties

# Dosyayı düzenleyin
nano local.properties
```

`local.properties` içeriği:

```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
```

## 🤖 Model Eğitimi

### 1. Python Ortamını Kurun

```bash
cd ml_model

# Virtual environment oluştur
python3 -m venv venv

# Aktive et
source venv/bin/activate  # macOS/Linux
# veya
venv\Scripts\activate  # Windows

# Gereksinimleri yükle
pip install -r requirements.txt
```

### 2. Veri Setini Hazırlayın

Veri seti zaten `dataset/symptoms_dataset.csv` dosyasında mevcut. Kendi veri setinizi kullanmak isterseniz:

```bash
# Veri setinizi kopyalayın
cp /path/to/your/dataset.csv dataset/symptoms_dataset.csv
```

**Veri Seti Formatı:**

```csv
fever_low,fever_mid,fever_high,cough_dry,...,label
0,1,0,1,0,...,Soğuk Algınlığı
```

### 3. Modeli Eğitin

```bash
python train_model.py
```

**Beklenen Çıktı:**

```
🏥 SemptomAI - Model Eğitim Scripti
📂 Veri seti yükleniyor...
✅ 40 satır, 33 sütun yüklendi
🔧 Veri ön işleme başlıyor...
✅ Özellik sayısı: 32
✅ Sınıf sayısı: 8
...
✅ Test Accuracy: 0.8750
✅ Eğitim tamamlandı!
```

### 4. TFLite'a Dönüştürün

```bash
python convert_to_tflite.py
```

**Beklenen Çıktı:**

```
📦 SemptomAI - TFLite Dönüştürme Scripti
📂 Model yükleniyor...
🔄 Scikit-learn modeli Keras'a dönüştürülüyor...
📦 TensorFlow Lite'a dönüştürülüyor...
✅ TFLite model kaydedildi: output/model.tflite
📊 Model boyutu: 45.23 KB
```

### 5. Model Dosyalarını Android'e Kopyalayın

```bash
# Model dosyasını kopyala
cp output/model.tflite ../app/src/main/assets/

# Feature map'i kopyala (zaten mevcut ama güncellenebilir)
cp output/feature_map.json ../app/src/main/assets/

# Classes metadata'yı kontrol et
cat output/classes_metadata.json
```

## 📱 Android Uygulama

### 1. Android Studio'da Projeyi Açın

```bash
# Android Studio'yu başlat
open -a "Android Studio" .
```

veya

- Android Studio'yu açın
- File > Open
- Proje klasörünü seçin

### 2. Gradle Sync

Android Studio otomatik olarak Gradle sync yapacaktır. Eğer yapmazsa:

- File > Sync Project with Gradle Files

### 3. Bağımlılıkları İndirin

İlk açılışta tüm bağımlılıklar otomatik indirilecektir. Bu işlem birkaç dakika sürebilir.

## 🗺️ Google Maps Entegrasyonu

### 1. Google Cloud Console'da API Key Alın

1. [Google Cloud Console](https://console.cloud.google.com/) açın
2. Yeni proje oluşturun veya mevcut projeyi seçin
3. **APIs & Services > Library** gidin
4. Aşağıdaki API'leri etkinleştirin:
   - Maps SDK for Android
   - Places API
5. **APIs & Services > Credentials** gidin
6. **Create Credentials > API Key** tıklayın
7. API Key'i kopyalayın

### 2. API Key'i Ekleyin

`local.properties` dosyasını açın ve API Key'i ekleyin:

```properties
MAPS_API_KEY=AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
```

### 3. API Key'i Kısıtlayın (Önerilen)

Google Cloud Console'da:

1. API Key'e tıklayın
2. **Application restrictions**:
   - Android apps seçin
   - Package name: `com.semptom.ai`
   - SHA-1 fingerprint ekleyin
3. **API restrictions**:
   - Restrict key seçin
   - Maps SDK for Android ve Places API seçin
4. Save

## ▶️ Çalıştırma

### Emülatörde Çalıştırma

1. Android Studio'da **Device Manager** açın
2. Yeni emülatör oluşturun (önerilen: Pixel 5, API 34)
3. Emülatörü başlatın
4. **Run > Run 'app'** veya **Shift + F10**

### Fiziksel Cihazda Çalıştırma

1. Cihazda **Developer Options** etkinleştirin
2. **USB Debugging** açın
3. Cihazı bilgisayara bağlayın
4. Android Studio'da cihazı seçin
5. **Run > Run 'app'**

## 🧪 Test Senaryoları

### Senaryo 1: Normal Kullanım

1. Uygulamayı açın
2. Feragatnameyi kabul edin
3. "Analiz Başlat" tıklayın
4. Semptomları seçin (örn: Ateş, Öksürük, Baş Ağrısı)
5. Detay sorularını cevaplayın
6. Sonuçları görüntüleyin

### Senaryo 2: Acil Durum (Triage)

1. "Analiz Başlat" tıklayın
2. "Göğüs Ağrısı" seçin
3. Şiddet: "Şiddetli / Sıkışma hissi"
4. Acil uyarı ekranını görün
5. "112'yi Ara" veya "En Yakın Acil Servis" test edin

### Senaryo 3: Profil ve Günlük

1. Ana ekranda "Profilim" tıklayın
2. Yaş, cinsiyet, kronik hastalıklar girin
3. Kaydet
4. Analiz yapın
5. "Sağlık Günlüğü" açın
6. Geçmiş kayıtları görün

## 🐛 Sorun Giderme

### Gradle Sync Hatası

```bash
# Gradle cache temizle
./gradlew clean

# Gradle wrapper yeniden oluştur
gradle wrapper --gradle-version 8.2
```

### Model Yüklenemedi Hatası

```bash
# Model dosyasının varlığını kontrol edin
ls -lh app/src/main/assets/model.tflite

# Eğer yoksa, ml_model klasöründen kopyalayın
cp ml_model/output/model.tflite app/src/main/assets/
```

### Build Hatası: "SDK location not found"

`local.properties` dosyasını kontrol edin:

```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
```

SDK yolunu bulun:

```bash
# macOS
echo $ANDROID_HOME

# veya
ls ~/Library/Android/sdk
```

### Maps Çalışmıyor

1. API Key'in doğru olduğundan emin olun
2. API'lerin etkinleştirildiğini kontrol edin
3. Billing aktif mi kontrol edin (Google Cloud)
4. Logcat'te hata mesajlarını kontrol edin

### Python Modül Hatası

```bash
# Virtual environment'ın aktif olduğundan emin olun
source ml_model/venv/bin/activate

# Gereksinimleri yeniden yükleyin
pip install --upgrade -r ml_model/requirements.txt
```

## 📊 Performans Optimizasyonu

### Model Boyutunu Küçültme

```python
# convert_to_tflite.py içinde
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.target_spec.supported_types = [tf.float16]  # float32 yerine
```

### APK Boyutunu Küçültme

```kotlin
// app/build.gradle.kts
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
}
```

## 📚 Ek Kaynaklar

- [Android Developer Guide](https://developer.android.com/)
- [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)
- [TensorFlow Lite Guide](https://www.tensorflow.org/lite)
- [Google Maps Platform](https://developers.google.com/maps)

## 🆘 Destek

Sorun yaşarsanız:

1. **Logcat**: Android Studio > Logcat'te hata mesajlarını kontrol edin
2. **README.md**: Ana dokümantasyonu okuyun
3. **ml_model/README.md**: Model eğitim rehberini inceleyin
4. **GitHub Issues**: Sorun bildirin (eğer public repo ise)

## ✅ Checklist

Başlamadan önce kontrol edin:

- [ ] Android Studio kurulu
- [ ] JDK 17 kurulu
- [ ] Python 3.8+ kurulu
- [ ] local.properties oluşturuldu
- [ ] Model eğitildi ve kopyalandı
- [ ] Google Maps API Key alındı (opsiyonel)
- [ ] Emülatör veya fiziksel cihaz hazır

---

**Başarılar! 🎉**

Herhangi bir sorunla karşılaşırsanız, yukarıdaki sorun giderme bölümünü kontrol edin.
