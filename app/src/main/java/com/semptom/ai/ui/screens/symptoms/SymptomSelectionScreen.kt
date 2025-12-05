package com.semptom.ai.ui.screens.symptoms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.semptom.ai.domain.model.Symptom
import com.semptom.ai.domain.model.SymptomCategory
import kotlinx.coroutines.launch

private val PrimaryBlack = Color(0xFF1C1C1C)
private val DarkBlueBorder = Color(0xFF0D47A1)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SymptomSelectionScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: SymptomSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Show error message if any
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Short
                )
                // Clear error after showing
                viewModel.clearError()
            }
        }
    }
    
    // Handle navigation to next screen
    LaunchedEffect(uiState.navigateToNext) {
        if (uiState.navigateToNext) {
            onNext()
            viewModel.navigationHandled()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Semptomlarınızı Seçin", 
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlack
                        )
                        Text(
                            "${uiState.selectedSymptoms.size} semptom seçildi",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryBlack.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Geri",
                            tint = PrimaryBlack
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            if (viewModel.validateSelection()) {
                                viewModel.saveSelectedSymptoms()
                                viewModel.proceedToNext()
                            }
                        },
                        enabled = uiState.selectedSymptoms.isNotEmpty(),
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (uiState.selectedSymptoms.isNotEmpty()) 
                                    DarkBlueBorder.copy(alpha = 0.2f)
                                else 
                                    Color.White.copy(alpha = 0.1f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "İleri",
                            tint = if (uiState.selectedSymptoms.isNotEmpty()) 
                                DarkBlueBorder 
                            else 
                                PrimaryBlack.copy(alpha = 0.4f)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A),
                            Color(0xFF3B82F6),
                            Color(0xFF60A5FA),
                            Color(0xFFDBEAFE)
                        ),
                        startY = 0f,
                        endY = 1000f
                    )
                )
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                LoadingIndicator()
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Search and filter section - modern tasarım
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp, 
            PrimaryBlack.copy(alpha = 0.3f)
        ),
        shadowElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Search bar - modern
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = DarkBlueBorder,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Semptom Ara",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlack
                                )
                            }
                            
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = viewModel::searchSymptoms,
                                placeholder = { Text("Semptom adını yazın...", color = PrimaryBlack.copy(alpha = 0.6f)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = DarkBlueBorder
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DarkBlueBorder,
                                    unfocusedBorderColor = DarkBlueBorder.copy(alpha = 0.6f),
                                    cursorColor = DarkBlueBorder
                                )
                            )

                            // Category filter chips - modern
                            Text(
                                text = "Kategoriler",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlack
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                item { // All categories item
                                    FilterChip(
                                        selected = uiState.selectedCategory == null,
                                        onClick = { viewModel.filterByCategory(null) },
                                        label = { Text("Tümü", color = if (uiState.selectedCategory == null) Color.White else PrimaryBlack) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = DarkBlueBorder,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                                
                                items(SymptomCategory.values()) { category ->
                                    FilterChip(
                                        selected = uiState.selectedCategory == category,
                                        onClick = { viewModel.filterByCategory(category) },
                                        label = { Text(category.displayName, color = if (uiState.selectedCategory == category) Color.White else PrimaryBlack) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = DarkBlueBorder,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                    // Symptoms List - modern tasarım
                    if (uiState.filteredSymptoms.isEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp, 
            PrimaryBlack.copy(alpha = 0.3f)
        ),
        shadowElevation = 0.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(80.dp),
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Icon(
                                                Icons.Default.SearchOff,
                                                contentDescription = null,
                                                modifier = Modifier.size(40.dp),
                                                tint = PrimaryBlack
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (uiState.searchQuery.isNotEmpty()) 
                                            "'${uiState.searchQuery}' ile eşleşen semptom bulunamadı" 
                                        else 
                                            "Semptom bulunamadı",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = PrimaryBlack,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.filteredSymptoms,
                                key = { it.id }
                            ) { symptom ->
                                SymptomCard(
                                    symptom = symptom,
                                    isSelected = symptom.isSelected,
                                    onSymptomClick = { viewModel.toggleSymptom(symptom) },
                                    onSeverityChange = { newSeverity ->
                                        viewModel.updateSymptomSeverity(symptom.id, newSeverity)
                                    },
                                    modifier = Modifier.animateItemPlacement()
                                )
                            }
                        }
                    }
                    
                    // Selected symptoms quick actions - modern tasarım
                    AnimatedVisibility(
                        visible = uiState.selectedSymptoms.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp, 
            PrimaryBlack.copy(alpha = 0.3f)
        ),
        shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${uiState.selectedSymptoms.size} semptom seçildi",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlack
                                    )
                                    Text(
                                        text = "Analiz için hazır",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PrimaryBlack.copy(alpha = 0.7f)
                                    )
                                }
                                
                                Button(
                                    onClick = { 
                                        if (viewModel.validateSelection()) {
                                            viewModel.saveSelectedSymptoms()
                                            viewModel.proceedToNext()
                                        }
                                    },
                                    enabled = uiState.selectedSymptoms.isNotEmpty(),
                                    modifier = Modifier
                                        .height(56.dp)
                                        .shadow(
                                            elevation = 8.dp,
                                            shape = RoundedCornerShape(28.dp),
                                            ambientColor = Color.White.copy(alpha = 0.3f),
                                            spotColor = Color.White.copy(alpha = 0.3f)
                                        ),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkBlueBorder,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Devam Et",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SymptomCard(
    symptom: Symptom,
    isSelected: Boolean,
    onSymptomClick: () -> Unit,
    onSeverityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        DarkBlueBorder
    } else {
        Color.White.copy(alpha = 0.3f)
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(durationMillis = 200, easing = EaseOutBack)
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onSymptomClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) {
            DarkBlueBorder.copy(alpha = 0.15f)
        } else {
            Color.Transparent
        },
        shadowElevation = 0.dp,
        border = BorderStroke(
            1.dp, 
            PrimaryBlack.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = symptom.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlack
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = symptom.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryBlack.copy(alpha = 0.8f)
                    )
                }
                
                // Category chip - modern
                Surface(
                    color = if (isSelected) {
                        DarkBlueBorder
                    } else {
                        Color.White.copy(alpha = 0.25f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = symptom.category.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) {
                            Color.White
                        } else {
                            PrimaryBlack
                        },
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (isSelected) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = DarkBlueBorder,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Şiddet: ${symptom.getSeverityText()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryBlack
                        )
                    }
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Slider(
                            value = symptom.severity.toFloat(),
                            onValueChange = { value ->
                                onSeverityChange(value.toInt())
                            },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = DarkBlueBorder,
                                activeTrackColor = DarkBlueBorder,
                                inactiveTrackColor = DarkBlueBorder.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
