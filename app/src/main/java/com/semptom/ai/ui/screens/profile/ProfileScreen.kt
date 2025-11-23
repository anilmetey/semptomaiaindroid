package com.semptom.ai.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.semptom.ai.R
import com.semptom.ai.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Age Group
            Text(
                text = stringResource(R.string.profile_age),
                style = MaterialTheme.typography.titleMedium
            )
            AgeGroup.values().forEach { age ->
                FilterChip(
                    selected = uiState.profile.ageGroup == age,
                    onClick = { viewModel.updateAgeGroup(age) },
                    label = { Text(age.displayName) }
                )
            }
            
            Divider()
            
            // Sex
            Text(
                text = stringResource(R.string.profile_sex),
                style = MaterialTheme.typography.titleMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Sex.values().forEach { sex ->
                    FilterChip(
                        selected = uiState.profile.sex == sex,
                        onClick = { viewModel.updateSex(sex) },
                        label = { Text(sex.displayName) }
                    )
                }
            }
            
            Divider()
            
            // Chronic Diseases
            Text(
                text = stringResource(R.string.profile_chronic),
                style = MaterialTheme.typography.titleMedium
            )
            ChronicDisease.values().forEach { disease ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(disease.displayName)
                    Checkbox(
                        checked = disease in uiState.profile.chronicDiseases,
                        onCheckedChange = { viewModel.toggleChronicDisease(disease) }
                    )
                }
            }
            
            Divider()
            
            // Allergies
            Text(
                text = stringResource(R.string.profile_allergies),
                style = MaterialTheme.typography.titleMedium
            )
            Allergy.values().forEach { allergy ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(allergy.displayName)
                    Checkbox(
                        checked = allergy in uiState.profile.allergies,
                        onCheckedChange = { viewModel.toggleAllergy(allergy) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save Button
            Button(
                onClick = { viewModel.saveProfile() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.profile_save))
            }
            
            if (uiState.isSaved) {
                Text(
                    text = stringResource(R.string.profile_saved),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logout Button
            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Çıkış Yap")
            }
        }
    }
}
