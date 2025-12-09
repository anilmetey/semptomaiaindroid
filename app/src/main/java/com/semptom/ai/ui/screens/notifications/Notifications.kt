package com.semptom.ai.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- RENK PALETİ ---
private val MedicalBlue = Color(0xFF1976D2)
private val SoftGray = Color(0xFFF5F6F8)
private val DarkText = Color(0xFF1A1C1E)
private val WarningOrange = Color(0xFFFF9800)
private val AlertRed = Color(0xFFD32F2F)
private val SafeGreen = Color(0xFF4CAF50)

// --- DATA MODELS ---
enum class NotificationType { ALERT, INFO, SUCCESS, REMINDER }

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: NotificationType,
    val isRead: Boolean = false
)

// --- UI STATE ---
data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<NotificationItem> = emptyList()
)

// --- VIEWMODEL ---
@HiltViewModel
class NotificationsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            delay(1000)
            val mockData = listOf(
                NotificationItem("1", "İlaç Hatırlatıcısı", "Antibiyotik alma saatiniz geldi (20:00).", "Şimdi", NotificationType.REMINDER, false),
                NotificationItem("2", "Yüksek Hava Basıncı", "Bugün migren riskiniz yüksek olabilir, bol su için.", "2 sa önce", NotificationType.ALERT, false),
                NotificationItem("3", "Analiz Tamamlandı", "Gönderdiğiniz cilt lekesi analizi sonuçlandı.", "5 sa önce", NotificationType.SUCCESS, true),
                NotificationItem("4", "Sistem Güncellemesi", "SemptomAI veritabanı yeni hastalıklarla güncellendi.", "Dün", NotificationType.INFO, true),
                NotificationItem("5", "Haftalık Rapor", "Geçen haftaki sağlık durumunuz stabil görünüyor.", "2 gün önce", NotificationType.INFO, true)
            )
            _uiState.value = NotificationsUiState(isLoading = false, notifications = mockData)
        }
    }

    fun markAsRead(id: String) {
        val currentList = _uiState.value.notifications.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        _uiState.value = _uiState.value.copy(notifications = currentList)
    }

    fun markAllAsRead() {
        val currentList = _uiState.value.notifications.map { it.copy(isRead = true) }
        _uiState.value = _uiState.value.copy(notifications = currentList)
    }
}

// --- SCREEN ---
@OptIn(ExperimentalMaterial3Api::class) // HATA ÇÖZÜMÜ 1: Experimental API uyarısını susturur
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = SoftGray,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Bildirimler",
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = DarkText)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Text("Tümünü Oku", color = MedicalBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SoftGray)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MedicalBlue)
                }
            } else if (uiState.notifications.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.notifications, key = { it.id }) { notification ->
                        NotificationCard(
                            item = notification,
                            onClick = { viewModel.markAsRead(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun NotificationCard(item: NotificationItem, onClick: () -> Unit) {
    val backgroundColor = if (item.isRead) Color.White else Color.White

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isRead) 1.dp else 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // İkon Kutusu
            val (icon, color) = when (item.type) {
                NotificationType.ALERT -> Icons.Rounded.Warning to WarningOrange
                NotificationType.SUCCESS -> Icons.Rounded.CheckCircle to SafeGreen
                NotificationType.REMINDER -> Icons.Rounded.AccessTime to AlertRed
                NotificationType.INFO -> Icons.Rounded.Info to MedicalBlue
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // İçerik
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (item.isRead) DarkText.copy(alpha = 0.7f) else DarkText
                    )

                    // Okunmamış Noktası
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MedicalBlue, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.message,
                    fontSize = 13.sp,
                    color = DarkText.copy(alpha = if (item.isRead) 0.5f else 0.8f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.time,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.NotificationsOff,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Bildiriminiz yok", color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}