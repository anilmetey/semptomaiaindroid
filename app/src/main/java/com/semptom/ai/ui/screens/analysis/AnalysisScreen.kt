package com.semptom.ai.ui.screens.analysis

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// --- RENK PALETİ ---
private val MedicalBlue = Color(0xFF1976D2)
private val MedicalLight = Color(0xFFE3F2FD)
private val DarkText = Color(0xFF1A1C1E)
private val SoftGray = Color(0xFFF5F6F8)
private val WarningOrange = Color(0xFFFF9800)
private val SafeGreen = Color(0xFF4CAF50)

@Composable
fun AnalysisScreen(
    onBack: () -> Unit,
    onNavigateToManualSelection: () -> Unit,
    onNavigateToTextAnalysis: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Arkaplan için animasyonlu gradient
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "offset"
    )

    Scaffold(
        containerColor = SoftGray,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToTextAnalysis,
                containerColor = MedicalBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Hızlı Analiz")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background Decoration (Hafif medikal desenler)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                drawCircle(
                    color = MedicalBlue.copy(alpha = 0.05f),
                    center = Offset(x = canvasWidth * 0.9f, y = offsetY * 0.2f),
                    radius = 150.dp.toPx()
                )
                drawCircle(
                    color = WarningOrange.copy(alpha = 0.03f),
                    center = Offset(x = 0f, y = canvasHeight * 0.8f - offsetY * 0.1f),
                    radius = 200.dp.toPx()
                )
            }

            if (uiState.isLoading) {
                LoadingScreen()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // 1. HEADER (Profil ve Hoşgeldin)
                    HeaderSection(
                        userName = uiState.userName,
                        greeting = uiState.greetingMessage,
                        alertCount = uiState.activeAlerts,
                        onBack = onBack,
                        onNotificationsClick = onNavigateToNotifications
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. DAILY SUMMARY CARD (Sağlık Skoru ve Hava Durumu)
                    DailySummaryCard(
                        score = uiState.healthScore,
                        weatherImpact = uiState.weatherHealthImpact
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. ACTION GRID (Ana Menü Butonları)
                    Text(
                        text = "Analiz Yöntemleri",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ActionGrid(
                        onTextAnalysis = onNavigateToTextAnalysis,
                        onManualAnalysis = onNavigateToManualSelection
                    )

                    // 4. HEALTH TIP (Günün İpucu)
                    uiState.dailyHealthTip?.let { tip ->
                        Spacer(modifier = Modifier.height(24.dp))
                        HealthTipCard(tip = tip, onDismiss = { viewModel.dismissTip() })
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 5. RECENT ACTIVITY (Geçmiş)
                    RecentActivitySection(history = uiState.recentActivities)

                    Spacer(modifier = Modifier.height(80.dp)) // Fab boşluğu
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// ALT BİLEŞENLER (KOMPONENTLER)
// -----------------------------------------------------------------------------

@Composable
fun HeaderSection(
    userName: String, 
    greeting: String, 
    alertCount: Int, 
    onBack: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = DarkText)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkText
                )
            }
        }

        // Bildirim İkonu
        Box {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .shadow(2.dp, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Bildirimler", tint = MedicalBlue)
            }
            if (alertCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(14.dp)
                        .background(Color.Red, CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        }
    }
}

@Composable
fun DailySummaryCard(score: Int, weatherImpact: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MedicalBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Dekoratif Çizgiler
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, size.height)
                    cubicTo(
                        size.width * 0.3f, size.height * 0.6f,
                        size.width * 0.7f, size.height * 0.8f,
                        size.width, size.height * 0.4f
                    )
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.1f),
                    style = Stroke(width = 40f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sol Taraf: Sağlık Skoru (Circular Progress)
                Box(contentAlignment = Alignment.Center) {
                    // HATA DÜZELTİLDİ: { 1f } yerine 1f
                    CircularProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.size(80.dp),
                        color = Color.White.copy(alpha = 0.3f),
                        strokeWidth = 8.dp,
                    )
                    // HATA DÜZELTİLDİ: { score / 100f } yerine score / 100f
                    CircularProgressIndicator(
                        progress = score / 100f,
                        modifier = Modifier.size(80.dp),
                        color = Color.White,
                        strokeWidth = 8.dp,
                        trackColor = Color.Transparent,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$score",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = "Skor",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Sağ Taraf: Bilgiler
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Günlük Durum",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if(score > 80) "Harika Görünüyorsun!" else "Dikkatli Olmalısın",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Cloud,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = weatherImpact,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            maxLines = 2,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ActionGrid(
    onTextAnalysis: () -> Unit,
    onManualAnalysis: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // 1. Text Analysis (Büyük Kart)
            ActionCard(
                title = "Metin Analizi",
                subtitle = "Yapay Zeka Destekli",
                icon = Icons.Rounded.AutoAwesome,
                color = MedicalBlue,
                modifier = Modifier.weight(1f),
                onClick = onTextAnalysis
            )

            // 2. Manual Analysis
            ActionCard(
                title = "Manuel",
                subtitle = "Liste Seçimi",
                icon = Icons.Rounded.Checklist,
                color = WarningOrange,
                modifier = Modifier.weight(1f),
                onClick = onManualAnalysis
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // 3. Voice Analysis (Mock - Pasif)
            ActionCard(
                title = "Ses Analizi",
                subtitle = "Yakında",
                icon = Icons.Rounded.Mic,
                color = Color.Gray,
                modifier = Modifier.weight(1f),
                enabled = false,
                onClick = {}
            )

            // 4. Camera Analysis (Mock - Pasif)
            ActionCard(
                title = "Görüntü",
                subtitle = "Deri & Yara",
                icon = Icons.Rounded.CameraAlt,
                color = Color.Gray,
                modifier = Modifier.weight(1f),
                enabled = false,
                onClick = {}
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (enabled) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (enabled) DarkText else Color.Gray
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun HealthTipCard(tip: com.semptom.ai.ui.screens.analysis.HealthTip, onDismiss: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Günün İpucu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Icon(
                    Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = SafeGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = tip.category.uppercase(),
                            color = SafeGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tip.title,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tip.content,
                        color = DarkText.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RecentActivitySection(history: List<com.semptom.ai.ui.screens.analysis.AnalysisHistoryItem>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Son Analizler",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            TextButton(onClick = { /* Tümünü gör */ }) {
                Text("Tümü", color = MedicalBlue, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Henüz bir analiz geçmişi yok", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            history.forEach { item ->
                HistoryItemRow(item)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun HistoryItemRow(item: com.semptom.ai.ui.screens.analysis.AnalysisHistoryItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Background based on Type
            val iconBgColor = when(item.type) {
                com.semptom.ai.ui.screens.analysis.AnalysisType.TEXT -> MedicalBlue.copy(alpha = 0.1f)
                com.semptom.ai.ui.screens.analysis.AnalysisType.MANUEL -> WarningOrange.copy(alpha = 0.1f)
                else -> Color.Gray.copy(alpha = 0.1f)
            }
            val iconColor = when(item.type) {
                com.semptom.ai.ui.screens.analysis.AnalysisType.TEXT -> MedicalBlue
                com.semptom.ai.ui.screens.analysis.AnalysisType.MANUEL -> WarningOrange
                else -> Color.Gray
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(item.type) {
                        com.semptom.ai.ui.screens.analysis.AnalysisType.TEXT -> Icons.Rounded.Description
                        com.semptom.ai.ui.screens.analysis.AnalysisType.MANUEL -> Icons.Rounded.Checklist
                        com.semptom.ai.ui.screens.analysis.AnalysisType.VOICE -> Icons.Rounded.Mic
                        else -> Icons.Rounded.Help
                    },
                    contentDescription = null,
                    tint = iconColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DarkText
                )
                Text(
                    text = item.date,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Risk Badge
            val (riskColor, riskBg) = when(item.riskLevel) {
                "Düşük" -> SafeGreen to SafeGreen.copy(alpha = 0.1f)
                "Orta" -> WarningOrange to WarningOrange.copy(alpha = 0.1f)
                "Yüksek" -> Color.Red to Color.Red.copy(alpha = 0.1f)
                else -> Color.Gray to Color.Gray.copy(alpha = 0.1f)
            }

            Box(
                modifier = Modifier
                    .background(riskBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.riskLevel,
                    color = riskColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MedicalBlue)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Verileriniz güvenle yükleniyor...", color = Color.Gray, fontSize = 14.sp)
        }
    }
}