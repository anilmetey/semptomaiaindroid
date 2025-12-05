package com.semptom.ai.ui.screens.analysis

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val PrimaryBlack = Color(0xFF1C1C1C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextAnalysisScreen(
    onBack: () -> Unit,
    viewModel: TextAnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var text by remember { mutableStateOf("") }
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),
                            Color(0xFF90CAF9)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Geri Iconu - en üstte satır içinde
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = PrimaryBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                // Başlık
                Text(
                    text = "Metin Analizi",
                    color = PrimaryBlack,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Şikayetlerinizi yazarak yapay zeka analizi yaptırın",
                    color = PrimaryBlack.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )
                
                // Metin Giriş Alanı
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = {
                            Text(
                                text = "Buraya şikayetlerinizi yazın...\n\nÖrnek:\nBaşım ağrıyor ve ateşim var. Öksürüğüm de başladı.",
                                color = PrimaryBlack.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Analiz Butonu
                Button(
                    onClick = { 
                        viewModel.analyzeText(text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    enabled = text.isNotBlank() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = "Analiz Et",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analizi Başlat",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                 }
                }
                
                // Sonuç Alanı (detaylı analiz)
                uiState.result?.let { analysis ->
                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = analysis.title,
                                color = PrimaryBlack,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = analysis.description,
                                color = PrimaryBlack.copy(alpha = 0.85f),
                                fontSize = 14.sp
                            )

                            if (analysis.recommendations.isNotEmpty()) {
                                Text(
                                    text = "Önerilen Adımlar",
                                    color = PrimaryBlack,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    analysis.recommendations.forEach { rec ->
                                        Row(
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "• ",
                                                color = PrimaryBlack,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = rec,
                                                color = PrimaryBlack,
                                                fontSize = 14.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                 
                 // Hata Mesajı
                 uiState.error?.let { error ->
                     Spacer(modifier = Modifier.height(16.dp))
                     
                     Card(
                         modifier = Modifier.fillMaxWidth(),
                         shape = RoundedCornerShape(12.dp),
                         colors = CardDefaults.cardColors(
                             containerColor = Color(0xFFFFEBEE)
                         )
                     ) {
                         Row(
                             modifier = Modifier.padding(16.dp),
                             verticalAlignment = Alignment.CenterVertically,
                             horizontalArrangement = Arrangement.spacedBy(12.dp)
                         ) {
                             Icon(
                                 Icons.Default.Error,
                                 contentDescription = "Hata",
                                 tint = Color(0xFFD32F2F),
                                 modifier = Modifier.size(20.dp)
                             )
                             
                             Text(
                                 text = error,
                                 color = Color(0xFFD32F2F),
                                 fontSize = 14.sp
                             )
                         }
                     }
                 }
                 
                 Spacer(modifier = Modifier.height(32.dp))
             }
         }
     }
 }
