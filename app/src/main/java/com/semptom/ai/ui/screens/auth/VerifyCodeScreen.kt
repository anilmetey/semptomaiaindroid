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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val DarkBlueBorder = Color(0xFF0D47A1) // Koyu mavi kenarlık rengi
private val PrimaryBlack = Color(0xFF1C1C1C) // Koyu arka plan metin rengi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyCodeScreen(
    email: String,
    onCodeVerified: (String) -> Unit,
    onBackToForgotPassword: () -> Unit,
    viewModel: AuthViewModel
) {
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()
    
    var code1 by remember { mutableStateOf("") }
    var code2 by remember { mutableStateOf("") }
    var code3 by remember { mutableStateOf("") }
    var code4 by remember { mutableStateOf("") }
    var code5 by remember { mutableStateOf("") }
    var code6 by remember { mutableStateOf("") }
    var submittedCode by remember { mutableStateOf("") }
    
    val fullCode = "$code1$code2$code3$code4$code5$code6"
    val canSubmit = fullCode.length == 6 && !uiState.isLoading
    
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutBack)
    )

    // Kod başarıyla doğrulandıktan sonra ResetPassword ekranına kodu ilet
    LaunchedEffect(uiState.isCodeVerified) {
        if (uiState.isCodeVerified && submittedCode.length == 6) {
            onCodeVerified(submittedCode)
        }
    }

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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBackToForgotPassword,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Geri",
                        tint = PrimaryBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logo with glass effect
            Surface(
                modifier = Modifier
                    .size(90.dp)
                    .scale(scale)
                    .shadow(
                        elevation = 15.dp,
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
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = PrimaryBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Doğrulama Kodu",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = PrimaryBlack,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "E-posta adresinize gönderilen 6 haneli kodu girin",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlack,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                color = PrimaryBlack.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

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
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Kodu Girin",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlack,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = "6 haneli doğrulama kodunu giriniz",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryBlack
                    )

                    // Code input fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CodeInputField(
                            value = code1,
                            onValueChange = { 
                                code1 = it
                                if (it.isNotEmpty()) focusManager.moveFocus(FocusDirection.Right)
                            },
                            focusManager = focusManager
                        )
                        CodeInputField(
                            value = code2,
                            onValueChange = { 
                                code2 = it
                                if (it.isNotEmpty()) focusManager.moveFocus(FocusDirection.Right)
                            },
                            focusManager = focusManager
                        )
                        CodeInputField(
                            value = code3,
                            onValueChange = { 
                                code3 = it
                                if (it.isNotEmpty()) focusManager.moveFocus(FocusDirection.Right)
                            },
                            focusManager = focusManager
                        )
                        CodeInputField(
                            value = code4,
                            onValueChange = { 
                                code4 = it
                                if (it.isNotEmpty()) focusManager.moveFocus(FocusDirection.Right)
                            },
                            focusManager = focusManager
                        )
                        CodeInputField(
                            value = code5,
                            onValueChange = { 
                                code5 = it
                                if (it.isNotEmpty()) focusManager.moveFocus(FocusDirection.Right)
                            },
                            focusManager = focusManager
                        )
                        CodeInputField(
                            value = code6,
                            onValueChange = { 
                                code6 = it
                                if (it.isNotEmpty()) focusManager.moveFocus(FocusDirection.Right)
                            },
                            focusManager = focusManager
                        )
                    }
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFF6B6B).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = uiState.error!!, color = PrimaryBlack, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Verify button
            Button(
                onClick = { 
                    if (canSubmit) {
                        submittedCode = fullCode
                        viewModel.verifyCode(email, fullCode)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(32.dp), ambientColor = Color.White.copy(alpha = 0.3f), spotColor = Color.White.copy(alpha = 0.3f)),
                enabled = canSubmit,
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = Color.White)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 3.dp)
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Doğrula", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resend code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Kod gelmedi mi?", style = MaterialTheme.typography.bodyLarge, color = PrimaryBlack.copy(alpha = 0.8f))
                TextButton(onClick = { viewModel.resendCode(email) }) {
                    Text(text = "Tekrar Gönder", color = PrimaryBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CodeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    focusManager: FocusManager
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= 1) {
                onValueChange(newValue)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Right) }
        ),
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .width(48.dp)
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DarkBlueBorder,
            unfocusedBorderColor = DarkBlueBorder.copy(alpha = 0.6f),
            focusedLabelColor = DarkBlueBorder,
            cursorColor = DarkBlueBorder
        )
    )
}
