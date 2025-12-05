package com.semptom.ai.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val DarkBlueBorder = Color(0xFF0D47A1) // Koyu mavi kenarlık rengi
private val PrimaryBlack = Color(0xFF1C1C1C) // Koyu arka plan metin rengi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateToVerifyCode: (String) -> Unit,
    onBackToLogin: () -> Unit,
    onResetSuccess: () -> Unit,
    onNavigateToEmailConfig: () -> Unit,
    viewModel: AuthViewModel
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var isResetSent by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(30) }

    val emailValid by remember(email) {
        mutableStateOf(email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
    }
    val canSubmit = !uiState.isLoading && emailValid

    var scale by remember { mutableStateOf(0.8f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 800, easing = EaseOutBack),
        label = "logo_scale"
    )

    LaunchedEffect(Unit) {
        scale = 1f
    }

    // Watch for email sending completion
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && email.isNotEmpty() && !isResetSent) {
            isResetSent = true
        }
    }

    // Countdown timer
    LaunchedEffect(isResetSent, timeLeft) {
        if (isResetSent && timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    // Navigate after delay
    LaunchedEffect(isResetSent) {
        if (isResetSent) {
            delay(3000) // 3 seconds to see the success message
            onNavigateToVerifyCode(email)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A), // Deep Blue
                        Color(0xFF3B82F6), // Primary Blue
                        Color(0xFF60A5FA), // Light Blue
                        Color(0xFFDBEAFE)  // Very Light Blue
                    ),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        // Background decoration
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Floating circles
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = (-50).dp, y = 100.dp)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = 250.dp, y = 300.dp)
                    .background(
                        Color.White.copy(alpha = 0.08f),
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = (-80).dp, y = 500.dp)
                    .background(
                        Color.White.copy(alpha = 0.06f),
                        CircleShape
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo with glass effect
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .scale(animatedScale)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        ambientColor = Color.White.copy(alpha = 0.3f),
                        spotColor = Color.White.copy(alpha = 0.3f)
                    ),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        modifier = Modifier.size(70.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Şifremi Unuttum",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = PrimaryBlack,
                letterSpacing = 1.sp
            )

            Text(
                text = "Şifre sıfırlama bağlantısı gönder",
                style = MaterialTheme.typography.bodyLarge,
                color = PrimaryBlack,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Modern divider
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                PrimaryBlack,
                                Color.Transparent
                            )
                        ),
                        RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (!isResetSent) {
                // Glass card for form
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .blur(0.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White.copy(alpha = 0.12f),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            text = "E-posta adresinizi girin",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlack,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "Şifre sıfırlama bağlantısı",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryBlack
                        )

                        // Email field with glass effect
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { 
                                Text(
                                    "E-posta",
                                    color = PrimaryBlack.copy(alpha = 0.6f)
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = DarkBlueBorder
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (emailValid && !uiState.isLoading) {
                                        viewModel.sendVerificationCode(email)
                                    }
                                }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            isError = email.isNotEmpty() && !emailValid,
                            supportingText = {
                                if (email.isNotEmpty() && !emailValid) {
                                    Text(
                                        "Geçerli bir e-posta girin",
                                        color = Color(0xFFFF6B6B)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkBlueBorder,
                                unfocusedBorderColor = DarkBlueBorder.copy(alpha = 0.6f),
                                focusedLabelColor = DarkBlueBorder,
                                cursorColor = DarkBlueBorder,
                                errorBorderColor = Color(0xFFFF6B6B)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Modern send button
                Button(
                    onClick = {
                        if (emailValid && !uiState.isLoading) {
                            // Send verification code through AuthViewModel
                            viewModel.sendVerificationCode(email)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = Color.White.copy(alpha = 0.3f),
                            spotColor = Color.White.copy(alpha = 0.3f)
                        ),
                    enabled = canSubmit,
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlack,
                        contentColor = Color.White
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Doğrulama Kodu Gönder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Success message
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = PrimaryBlack
                        )

                        Text(
                            text = "Başarılı!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlack,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Doğrulama kodu\n${email}\nadresine gönderildi",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PrimaryBlack,
                            textAlign = TextAlign.Center
                        )



                        Text(
                            text = "Yönlendiriliyor... (${timeLeft}s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryBlack.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Back to login button
                OutlinedButton(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryBlack
                    )
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Giriş Ekranına Dön",
                        style = MaterialTheme.typography.bodyLarge,
                        color = PrimaryBlack
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Email config button
                TextButton(
                    onClick = onNavigateToEmailConfig,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "E-posta Ayarları",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryBlack
                    )
                }
            }

            // Back to login text button
            if (!isResetSent) {
                TextButton(onClick = onBackToLogin) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = PrimaryBlack
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Giriş Ekranına Dön",
                            color = PrimaryBlack,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
