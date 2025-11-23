package com.semptom.ai.ui.screens.disclaimer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@HiltViewModel
class DisclaimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val DISCLAIMER_ACCEPTED_KEY = booleanPreferencesKey("disclaimer_accepted")
    
    val hasAcceptedDisclaimer: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DISCLAIMER_ACCEPTED_KEY] ?: false
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    fun acceptDisclaimer() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[DISCLAIMER_ACCEPTED_KEY] = true
            }
        }
    }
}
