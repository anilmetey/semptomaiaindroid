# SemptomAI - Yapay Zeka Destekli Semptom Analizörü ve Sağlık Asistanı

## 📋 Proje Hakkında

SemptomAI, kullanıcıların yaşadıkları semptomları girerek olası sağlık durumlarını öğrenebilecekleri, yapay zeka destekli bir mobil sağlık asistanıdır.

**⚠️ ÖNEMLİ UYARI**: Bu uygulama tıbbi bir cihaz değildir ve doktor tavsiyesi yerine geçmez. Sadece bilgilendirme amaçlıdır.

## ✨ Özellikler

- 🤖 **Yapay Zeka Destekli Analiz**: TensorFlow Lite ile cihaz içi semptom analizi
- 🎯 **Akıllı Triyaj Sistemi**: Acil durumları tespit eden kırmızı bayrak sistemi
- 📊 **Olasılık Tahmini**: Hastalık olasılıklarını yüzdesel gösterim
- 💡 **Semptom Bazlı Öneriler**: İlaç önerisi yerine, eczacı/doktor danışma odaklı rehberlik
- 🗺️ **Yakın Sağlık Kuruluşları**: Google Maps entegrasyonu ile en yakın acil/eczane
- 📝 **Sağlık Günlüğü**: Geçmiş semptom kayıtlarını takip
- 👤 **Kullanıcı Profili**: Yaş, cinsiyet, kronik hastalık ve alerji bilgileri
- 🔒 **Gizlilik Odaklı**: Tüm veriler cihaz içinde, offline çalışma

## 🏗️ Teknik Mimari

### Android Uygulama
- **Dil**: Kotlin
- **UI Framework**: Jetpack Compose
- **Dependency Injection**: Hilt
- **Database**: Room
- **ML Framework**: TensorFlow Lite
- **Maps**: Google Maps SDK & Places API
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

### Yapay Zeka Modeli
- **Algoritma**: Random Forest / Naive Bayes
- **Framework**: scikit-learn → TensorFlow Lite
- **Özellikler**: Binary semptomlar + profil bilgileri + mevsimsel faktörler
- **Çıktı**: Softmax olasılık dağılımı

## 📁 Proje Yapısı

```
├── app/                          # Android uygulama
│   ├── src/main/
│   │   ├── java/com/semptom/ai/
│   │   │   ├── data/            # Repository, DAO, Entity
│   │   │   ├── domain/          # Use Cases, Models
│   │   │   ├── ui/              # Compose Screens
│   │   │   └── di/              # Hilt Modules
│   │   ├── assets/              # Model ve JSON dosyaları
│   │   │   ├── model.tflite
│   │   │   ├── feature_map.json
│   │   │   ├── classes.json
│   │   │   ├── symptoms.json
│   │   │   ├── triage_rules.json
│   │   │   └── advice_rules.json
│   │   └── res/                 # Resources
│   └── build.gradle.kts
├── ml_model/                     # Python model eğitim
│   ├── train_model.py
│   ├── convert_to_tflite.py
│   ├── dataset/
│   │   └── symptoms_dataset.csv
│   └── requirements.txt
└── README.md
```

## 🚀 Kurulum ve Çalıştırma

### Android Uygulaması

1. **Gereksinimler**:
   - Android Studio Hedgehog | 2023.1.1 veya üzeri
   - JDK 17
   - Android SDK 34

2. **Google Maps API Key**:
   ```bash
   # local.properties dosyasına ekleyin:
   MAPS_API_KEY=your_api_key_here
   ```

3. **Projeyi Açın**:
   ```bash
   # Android Studio'da File > Open > proje klasörünü seçin
   ```

4. **Çalıştırın**:
   - Build > Make Project
   - Run > Run 'app'

### Model Eğitimi

1. **Python Ortamı**:
   ```bash
   cd ml_model
   python -m venv venv
   source venv/bin/activate  # Windows: venv\Scripts\activate
   pip install -r requirements.txt
   ```

2. **Veri Seti Hazırlama**:
   - `dataset/symptoms_dataset.csv` dosyasını düzenleyin veya kendi veri setinizi ekleyin

3. **Model Eğitimi**:
   ```bash
   python train_model.py
   ```

4. **TFLite Dönüşümü**:
   ```bash
   python convert_to_tflite.py
   ```

5. **Model Kopyalama**:
   ```bash
   cp output/model.tflite ../app/src/main/assets/
   cp output/feature_map.json ../app/src/main/assets/
   cp output/classes.json ../app/src/main/assets/
   ```

## 📊 Veri Seti Formatı

```csv
fever_low,fever_mid,fever_high,cough_dry,cough_productive,headache,runny_nose,short_breath,chest_pain,age_18_35,age_36_55,age_56p,sex_f,asthma,allergy,season_spring,season_summer,season_fall,season_winter,label
0,1,0,1,0,1,0,0,0,1,0,0,0,0,0,0,0,0,1,Grip
1,0,0,0,0,0,1,0,0,1,0,0,1,0,1,1,0,0,0,Alerji
...
```

## 🎯 MVP Hastalık Sınıfları

1. **Soğuk Algınlığı** (Common Cold)
2. **Grip** (Influenza)
3. **Alerjik Rinit** (Allergic Rhinitis)
4. **COVID-19 Benzeri Üst Solunum Yolu Enfeksiyonu**
5. **Bronşit**
6. **Sinüzit**
7. **Migren**
8. **Gastroenterit** (Mide Gribi)

## 🔴 Kırmızı Bayrak Semptomlar (Acil Triyaj)

- Şiddetli göğüs ağrısı
- Nefes darlığı / Nefes alamama
- Bilinç bulanıklığı / Bayılma
- Şiddetli baş ağrısı + ense sertliği
- Yüksek ateş (>39.5°C) + döküntü
- Kanlı balgam / Kanlı kusma

## 📱 Ekran Akışı

```
Splash → Disclaimer (İlk Açılış) → Ana Ekran
                                      ↓
                    ┌─────────────────┼─────────────────┐
                    ↓                 ↓                 ↓
              Semptom Seçimi    Profil Düzenle    Sağlık Günlüğü
                    ↓
            Detay Soruları
                    ↓
            ┌───────┴───────┐
            ↓               ↓
      Triyaj Uyarı    Model Analizi
      (Acil Durum)          ↓
            ↓         Sonuç Ekranı
            ↓         (Olasılıklar + Öneriler)
            ↓               ↓
        Harita Ekranı ←─────┘
     (En Yakın Acil/Eczane)
```

## 🛡️ Etik ve Yasal Uyarılar

### Feragatname Metni
```
Bu uygulama tıbbi bir cihaz değildir ve profesyonel tıbbi tavsiye, 
teşhis veya tedavi yerine geçmez. Gösterilen sonuçlar sadece 
istatistiksel olasılıklara dayalı bilgilendirme amaçlıdır.

Sağlık sorunlarınız için mutlaka bir doktora danışın. Acil durumlarda 
112'yi arayın veya en yakın acil servise başvurun.

Bu uygulamayı kullanarak, sonuçların kesin olmadığını ve kendi 
sorumluluğunuzda hareket ettiğinizi kabul etmiş olursunuz.
```

### KVKK/GDPR Uyumu
- ✅ Veriler cihaz içinde saklanır (offline-first)
- ✅ Sunucuya veri gönderimi yok
- ✅ Minimum izin talebi (sadece konum - opsiyonel)
- ✅ Kullanıcı verilerini silme özelliği
- ✅ Şeffaf veri kullanımı

## 🧪 Test Senaryoları

### Senaryo 1: Normal Soğuk Algınlığı
- **Semptomlar**: Hafif ateş, burun akıntısı, hafif öksürük
- **Beklenen**: Soğuk Algınlığı %60-70
- **Öneri**: Dinlenme, bol sıvı, eczacıya danışma

### Senaryo 2: Acil Durum
- **Semptomlar**: Şiddetli göğüs ağrısı
- **Beklenen**: Triyaj ekranı, 112 butonu
- **Öneri**: Acil servise yönlendirme

### Senaryo 3: Mevsimsel Alerji
- **Semptomlar**: Burun akıntısı, hapşırma (İlkbahar)
- **Profil**: Alerji geçmişi var
- **Beklenen**: Alerjik Rinit %70-80

## 📈 Performans Metrikleri

- **Model Doğruluğu**: >85% (hedef)
- **Inference Süresi**: <100ms
- **Uygulama Boyutu**: <15MB
- **Minimum RAM**: 2GB

## 🔮 Gelecek Özellikler (Stretch Goals)

- [ ] Serbest metin NLP ile semptom çıkarımı
- [ ] İlaç etkileşim veritabanı entegrasyonu
- [ ] Çoklu dil desteği (İngilizce, Almanca)
- [ ] Sesli semptom girişi
- [ ] Sağlık günlüğü analitikleri ve trendler
- [ ] Doktor randevu entegrasyonu
- [ ] Wearable cihaz entegrasyonu (ateş, nabız)

## 👥 Katkıda Bulunma

Bu bir bitirme projesidir. Öneriler için issue açabilirsiniz.

## 📄 Lisans

Bu proje eğitim amaçlıdır. Ticari kullanım için uygun değildir.

## 📞 İletişim

Proje Sahibi: [Adınız]
Email: [Email Adresiniz]

---

**⚠️ TEKRAR HATIRLATMA**: Bu uygulama eğitim amaçlı bir prototiptir. Gerçek sağlık kararları için mutlaka sağlık profesyoneline danışın.
