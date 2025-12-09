package com.semptom.ai.ui.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.semptom.ai.R
import com.semptom.ai.domain.model.*

private val PrimaryBlack = Color(0xFF1C1C1C)
private val DarkBlueBorder = Color(0xFF0D47A1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutBack)
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE3F2FD),
                shadowElevation = 4.dp,
                border = BorderStroke(
                    1.dp,
                    Color(0xFF90CAF9).copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE3F2FD),
                shadowElevation = 8.dp,
                border = BorderStroke(
                    1.dp,
                    Color(0xFF90CAF9).copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    // Navigation icons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Home
                        IconButton(
                            onClick = onNavigateToHome,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = Color(0xFF2196F3).copy(alpha = 0.1f),
                                border = BorderStroke(2.dp, Color(0xFF2196F3))
                            ) {
                                Icon(
                                    Icons.Rounded.Home,
                                    contentDescription = "Ana Sayfa",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(4.dp)
                                )
                            }
                        }
                        
                        // Journal
                        IconButton(
                            onClick = onNavigateToJournal,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = Color(0xFF2196F3).copy(alpha = 0.1f),
                                border = BorderStroke(2.dp, Color(0xFF2196F3))
                            ) {
                                Icon(
                                    Icons.Rounded.Book,
                                    contentDescription = "Günlük",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(4.dp)
                                )
                            }
                        }
                        
                        // Statistics
                        IconButton(
                            onClick = onNavigateToStatistics,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = Color(0xFF2196F3).copy(alpha = 0.1f),
                                border = BorderStroke(2.dp, Color(0xFF2196F3))
                            ) {
                                Icon(
                                    Icons.Rounded.BarChart,
                                    contentDescription = "İstatistikler",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(4.dp)
                                )
                            }
                        }
                        
                        // Profile
                        IconButton(
                            onClick = { /* Current screen */ },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = Color(0xFF2196F3).copy(alpha = 0.1f),
                                border = BorderStroke(2.dp, Color(0xFF2196F3))
                            ) {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = "Profil",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),
                            Color(0xFFBBDEFB),
                            Color(0xFF90CAF9)
                        ),
                        startY = 0f,
                        endY = 1000f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profil başlık kartı - modern tasarım
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color.White.copy(alpha = 0.3f),
                            spotColor = Color.White.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Profil avatarı
                        Surface(
                            modifier = Modifier
                                .size(80.dp)
                                .scale(scale)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = CircleShape,
                                    ambientColor = Color.White.copy(alpha = 0.3f),
                                    spotColor = Color.White.copy(alpha = 0.3f)
                                ),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.3f),
                                                Color.White.copy(alpha = 0.1f)
                                            )
                                        )
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = PrimaryBlack
                                )
                            }
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Profil Bilgileri",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlack,
                                letterSpacing = 0.5.sp
                            )
                            
                            Text(
                                text = "Sağlık profilinizi kişiselleştirin",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryBlack.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        // Modern ayırıcı
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(3.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            PrimaryBlack.copy(alpha = 0.6f),
                                            Color.Transparent
                                        )
                                    ),
                                    RoundedCornerShape(1.5.dp)
                                )
                        )
                    }
                }

                // Yaş + Cinsiyet - cam kart tasarımı
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color.White.copy(alpha = 0.2f),
                            spotColor = Color.White.copy(alpha = 0.2f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cake,
                                contentDescription = null,
                                tint = DarkBlueBorder,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(R.string.profile_age),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlack
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AgeGroup.values().forEach { age ->
                                FilterChip(
                                    selected = uiState.profile.ageGroup == age,
                                    onClick = { viewModel.updateAgeGroup(age) },
                                    label = { Text(age.displayName, color = if (uiState.profile.ageGroup == age) Color.White else PrimaryBlack) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DarkBlueBorder,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        
                        Divider(
                            color = PrimaryBlack.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = DarkBlueBorder,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(R.string.profile_sex),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlack
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Sex.values().forEach { sex ->
                                FilterChip(
                                    selected = uiState.profile.sex == sex,
                                    onClick = { viewModel.updateSex(sex) },
                                    label = { Text(sex.displayName, color = if (uiState.profile.sex == sex) Color.White else PrimaryBlack) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DarkBlueBorder,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Kronik hastalıklar - cam kart tasarımı
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color.White.copy(alpha = 0.2f),
                            spotColor = Color.White.copy(alpha = 0.2f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = DarkBlueBorder,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(R.string.profile_chronic),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlack
                            )
                        }
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ChronicDisease.values().forEach { disease ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = disease.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PrimaryBlack,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Checkbox(
                                        checked = disease in uiState.profile.chronicDiseases,
                                        onCheckedChange = { viewModel.toggleChronicDisease(disease) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = DarkBlueBorder
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Alerjiler - cam kart tasarımı
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color.White.copy(alpha = 0.2f),
                            spotColor = Color.White.copy(alpha = 0.2f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = DarkBlueBorder,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(R.string.profile_allergies),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlack
                            )
                        }
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Allergy.values().forEach { allergy ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = allergy.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PrimaryBlack,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Checkbox(
                                        checked = allergy in uiState.profile.allergies,
                                        onCheckedChange = { viewModel.toggleAllergy(allergy) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = DarkBlueBorder
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Kaydet / Çıkış - modern buton tasarımı
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.saveProfile() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = Color.White.copy(alpha = 0.3f),
                                spotColor = Color.White.copy(alpha = 0.3f)
                            ),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlack,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.profile_save),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    OutlinedButton(
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = Color.White.copy(alpha = 0.2f),
                                spotColor = Color.White.copy(alpha = 0.2f)
                            ),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF6B6B)
                        ),
                        border = BorderStroke(2.dp, Color(0xFFFF6B6B).copy(alpha = 0.6f))
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Çıkış Yap",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Başarı mesajı - modern tasarım
                if (uiState.isSaved) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.profile_saved),
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryBlack,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}