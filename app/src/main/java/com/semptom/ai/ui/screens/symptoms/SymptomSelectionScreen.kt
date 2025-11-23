package com.semptom.ai.ui.screens.symptoms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.semptom.ai.R
import com.semptom.ai.domain.model.Symptom
import com.semptom.ai.domain.model.SymptomCategory
import kotlinx.coroutines.launch

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
                        Text("Semptomlarınızı Seçin")
                        Text(
                            "${uiState.selectedSymptoms.size} semptom seçildi",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
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
                        enabled = uiState.selectedSymptoms.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "İleri",
                            tint = if (uiState.selectedSymptoms.isNotEmpty()) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (uiState.isLoading) {
                LoadingIndicator()
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Search and filter section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(16.dp)
                    ) {
                        // Search bar
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::searchSymptoms,
                            label = { Text("Semptom ara...") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Category filter chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            item { // All categories item
                                FilterChip(
                                    selected = uiState.selectedCategory == null,
                                    onClick = { viewModel.filterByCategory(null) },
                                    label = { Text("Tümü") },
                                    leadingIcon = if (uiState.selectedCategory == null) {
                                        { Icon(Icons.Default.Check, "", Modifier.size(FilterChipDefaults.IconSize)) }
                                    } else null
                                )
                            }
                            
                            items(SymptomCategory.values()) { category ->
                                FilterChip(
                                    selected = uiState.selectedCategory == category,
                                    onClick = { viewModel.filterByCategory(category) },
                                    label = { Text(category.displayName) },
                                    leadingIcon = if (uiState.selectedCategory == category) {
                                        { Icon(Icons.Default.Check, "", Modifier.size(FilterChipDefaults.IconSize)) }
                                    } else null
                                )
                            }
                        }
                    }
                    // Symptoms List
                    if (uiState.filteredSymptoms.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty()) 
                                    "'${uiState.searchQuery}' ile eşleşen semptom bulunamadı" 
                                else 
                                    "Semptom bulunamadı",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    
                    // Selected symptoms quick actions
                    AnimatedVisibility(
                        visible = uiState.selectedSymptoms.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            tonalElevation = 8.dp,
                            shadowElevation = 4.dp,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${uiState.selectedSymptoms.size} semptom seçildi",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Button(
                                    onClick = { 
                                        if (viewModel.validateSelection()) {
                                            viewModel.saveSelectedSymptoms()
                                            viewModel.proceedToNext()
                                        }
                                    },
                                    enabled = uiState.selectedSymptoms.isNotEmpty(),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("Devam Et")
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
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSymptomClick() },
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = symptom.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = symptom.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (isSelected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Şiddet: ${symptom.getSeverityText()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Slider(
                        value = symptom.severity.toFloat(),
                        onValueChange = { value ->
                            onSeverityChange(value.toInt())
                        },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Category chip
            Surface(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = symptom.category.displayName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
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
