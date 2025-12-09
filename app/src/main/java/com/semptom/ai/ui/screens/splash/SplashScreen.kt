package com.semptom.ai.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- HOME SCREEN UYUMLU PREMIUM PALET ---
private val MedicalBluePrimary = Color(0xFF2196F3) // Ana Marka Rengi
private val MedicalBlueLight = Color(0xFFE3F2FD)   // En açık ton
private val MedicalBlueMedium = Color(0xFF90CAF9)  // Orta ton
private val PureWhite = Color(0xFFFFFFFF)
private val DarkText = Color(0xFF1C1C1C)

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit
) {
    // --- ANIMASYON CONTROLLER ---
    val logoScale = remember { Animatable(0f) }
    val textSlide = remember { Animatable(100f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Logo "Elastik" bir şekilde ekrana düşer
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.6f, // Tatlı bir yaylanma
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // 2. Yazı süzülerek gelir
        delay(300)
        launch {
            textSlide.animateTo(0f, tween(800, easing = EaseOutQuart))
        }
        launch {
            textAlpha.animateTo(1f, tween(800))
        }

        // 3. Geçiş
        delay(3000)
        onNavigateToMain()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite), // Taban rengi
        contentAlignment = Alignment.Center
    ) {
        // --- KATMAN 1: SIVI GRADIENT ARKA PLAN (Liquid Mesh) ---
        // Home renklerinin canlı, hareketli hali
        LiquidMeshBackground()

        // --- KATMAN 2: MİKRO PARÇACIKLAR (Floating Data) ---
        FloatingParticles()

        // --- KATMAN 3: ANA İÇERİK ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // MERKEZ: KRİSTAL CAM LOGO
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(180.dp)
                    .scale(logoScale.value)
            ) {
                // Arkadaki "Nefes Alan" Hale
                BreathingHalo()

                // Cam Efektli Kutu (Glassmorphism)
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(
                            elevation = 30.dp,
                            shape = CircleShape,
                            ambientColor = MedicalBluePrimary.copy(alpha = 0.2f),
                            spotColor = MedicalBluePrimary.copy(alpha = 0.4f)
                        )
                        .clip(CircleShape)
                        .background(PureWhite.copy(alpha = 0.7f)) // Yarı saydam beyaz
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    PureWhite,
                                    PureWhite.copy(alpha = 0f),
                                    PureWhite
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // İkonun kendisi (Kalp Atışı Animasyonu ile)
                    HeartbeatIcon()
                }

                // Dekoratif "AI" Rozeti
                AiBadge(modifier = Modifier.align(Alignment.TopEnd).offset(x = (-10).dp, y = 10.dp))
            }

            Spacer(modifier = Modifier.height(40.dp))

            // TİPOGRAFİ (Home ile uyumlu ama daha havalı)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer {
                        translationY = textSlide.value
                        alpha = textAlpha.value
                    }
            ) {
                Text(
                    text = "SymptomAI",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    ),
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Modern "Pill" Şeklinde Etiket
                Box(
                    modifier = Modifier
                        .background(
                            color = MedicalBluePrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(50)
                        )
                        .border(1.dp, MedicalBluePrimary.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "AKILLI SAĞLIK ASİSTANI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = MedicalBluePrimary
                        )
                    )
                }
            }
        }

        // --- KATMAN 4: LOADING ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .alpha(textAlpha.value)
        ) {
            AnalyzingIndicator()
        }
    }
}

// --- GÖRSEL EFEKTLER VE YARDIMCI BİLEŞENLER ---

@Composable
fun LiquidMeshBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid")

    // Blob'ların yumuşak hareketi
    val t1 by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse), label = "t1")
    val t2 by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse), label = "t2")

    Canvas(modifier = Modifier.fillMaxSize().blur(100.dp)) { // Yüksek blur = Mesh Gradient
        val w = size.width
        val h = size.height

        // Üst Mavi Blob
        drawCircle(
            color = MedicalBlueLight,
            radius = w * 0.8f,
            center = Offset(w * 0.2f + (w * 0.2f * t1), h * 0.2f)
        )

        // Alt Orta Mavi Blob
        drawCircle(
            color = MedicalBlueMedium.copy(alpha = 0.6f),
            radius = w * 0.6f,
            center = Offset(w * 0.8f - (w * 0.4f * t2), h * 0.8f)
        )

        // Merkez Beyaz Parlama
        drawCircle(
            color = PureWhite,
            radius = w * 0.5f,
            center = Offset(w / 2, h / 2)
        )
    }
}

@Composable
fun FloatingParticles() {
    // Rastgele hareket eden 5 tane parçacık
    Box(modifier = Modifier.fillMaxSize()) {
        repeat(5) {
            val startX = remember { Random.nextFloat() }
            val startDelay = remember { Random.nextInt(0, 2000) }
            val duration = remember { Random.nextInt(3000, 6000) }

            val infiniteTransition = rememberInfiniteTransition(label = "particle")
            val yOffset by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 0f,
                animationSpec = infiniteRepeatable(tween(duration, delayMillis = startDelay, easing = LinearEasing)),
                label = "y"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(duration, delayMillis = startDelay), RepeatMode.Restart),
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (startX * 400).dp, y = (-800 * (1 - yOffset)).dp) // Yukarı doğru çıkış
                    .size(Random.nextInt(4, 12).dp)
                    .alpha(if(yOffset < 0.2f) yOffset * 5 else 0.4f) // Yukarı çıktıkça kaybol
                    .background(MedicalBluePrimary.copy(alpha = 0.3f), CircleShape)
            )
        }
    }
}

@Composable
fun BreathingHalo() {
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "s"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "a"
    )

    Box(
        modifier = Modifier
            .size(130.dp)
            .scale(scale)
            .background(MedicalBluePrimary.copy(alpha = alpha), CircleShape)
    )
}

@Composable
fun HeartbeatIcon() {
    // Gerçekçi Kalp Ritmi: KÜT-KÜT ---- KÜT-KÜT
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                1.0f at 0
                1.15f at 150 // İlk vuruş
                1.0f at 300  // Gevşeme
                1.15f at 450 // İkinci vuruş
                1.0f at 600  // Gevşeme
                1.0f at 1200 // Bekleme
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    Icon(
        imageVector = Icons.Rounded.LocalHospital,
        contentDescription = null,
        tint = MedicalBluePrimary,
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
    )
}

@Composable
fun AiBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(32.dp)
            .shadow(4.dp, CircleShape)
            .background(PureWhite, CircleShape)
            .border(1.dp, MedicalBlueLight, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = Color(0xFFFFA000), // Altın sarısı bir AI dokunuşu
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun AnalyzingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp) // Yazı ile noktalar arası boşluk
    ) {
        // YAZI KISMI
        Text(
            text = "SİSTEM YÜKLENİLİYOR",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MedicalBluePrimary.copy(alpha = 0.9f)
        )

        // 3 NOKTA ANİMASYONU
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp), // Noktaların kendi arasındaki boşluğu
            verticalAlignment = Alignment.CenterVertically
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "dots_loading")

            // 3 Kere tekrar et (3 Nokta için)
            repeat(3) { index ->
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f, // Sönük hali (tam kaybolmasın, silik dursun)
                    targetValue = 1f,    // Parlak hali
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                        // EN ÖNEMLİ KISIM: Her noktaya index * 200ms gecikme veriyoruz ki dalga gibi görünsün
                        initialStartOffset = StartOffset(index * 200)
                    ),
                    label = "dot_$index"
                )

                Box(
                    modifier = Modifier
                        .size(6.dp) // Nokta boyutu
                        .alpha(alpha)
                        .background(MedicalBluePrimary, CircleShape)
                )
            }
        }
    }
}