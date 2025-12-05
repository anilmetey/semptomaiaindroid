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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val DarkBlueBorder = Color(0xFF0D47A1) // Koyu mavi kenarlık rengi
private val PrimaryBlack = Color(0xFF1C1C1C) // Koyu arka plan metin rengi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val emailValid by remember(email) {
        mutableStateOf(email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
    }
    val passwordsMatch by remember(password, confirmPassword) {
        mutableStateOf(password.isNotBlank() && confirmPassword.isNotBlank() && password == confirmPassword)
    }
    val canSubmit = !uiState.isLoading && fullName.isNotBlank() && emailValid && passwordsMatch && password.length >= 6

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutBack)
    )

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onRegisterSuccess()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp)) // Sıkıştırıldı

            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onNavigateToLogin,
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

            Spacer(modifier = Modifier.height(24.dp)) // Sıkıştırıldı

            // Logo with glass effect (Boyut 70.dp'ye düşürüldü)
            Surface(
                modifier = Modifier
                    .size(70.dp)
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
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp), // Icon size adjusted
                        tint = PrimaryBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Sıkıştırıldı

            Text(
                text = "Yeni Hesap",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = PrimaryBlack,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "SemptomAI'ye katılın",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlack,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp)) // Sıkıştırıldı

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

            Spacer(modifier = Modifier.height(24.dp)) // Sıkıştırıldı

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
                        .padding(24.dp), // Padding sıkıştırıldı
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Sıkıştırıldı
                ) {
                    Text(
                        text = "Kayıt Ol",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlack,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = "Yeni hesap oluşturun",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryBlack
                    )

                    // Full Name Field
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = { Text("Ad Soyad", color = PrimaryBlack.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = DarkBlueBorder) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBlueBorder,
                            unfocusedBorderColor = DarkBlueBorder.copy(alpha = 0.6f),
                            focusedLabelColor = DarkBlueBorder,
                            cursorColor = DarkBlueBorder,
                            errorBorderColor = Color(0xFFFF6B6B)
                        )
                    )

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email", color = PrimaryBlack.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = DarkBlueBorder) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        isError = email.isNotEmpty() && !emailValid,
                        supportingText = {
                            if (email.isNotEmpty() && !emailValid) {
                                Text("Geçerli bir e-posta girin", color = Color(0xFFFF6B6B))
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

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Şifre (min. 6 karakter)", color = PrimaryBlack.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = DarkBlueBorder) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = DarkBlueBorder)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        isError = password.isNotEmpty() && password.length < 6,
                        supportingText = {
                            if (password.isNotEmpty() && password.length < 6) {
                                Text("Şifre en az 6 karakter olmalı", color = Color(0xFFFF6B6B))
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

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Şifre Tekrar", color = PrimaryBlack.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = DarkBlueBorder) },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = DarkBlueBorder)
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); if (canSubmit) { viewModel.register(email, password, fullName) } }),
                        singleLine = true,
                        isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                                Text("Şifreler eşleşmiyor", color = Color(0xFFFF6B6B))
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
            // Glass card for form END

            // Error messages
            if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                Spacer(modifier = Modifier.height(12.dp)) // Sıkıştırıldı
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFF6B6B).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Şifreler eşleşmiyor", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(12.dp)) // Sıkıştırıldı
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFF6B6B).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = uiState.error!!, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) // Sıkıştırıldı

            // Register button
            Button(
                onClick = { if (canSubmit) { viewModel.register(email, password, fullName) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp) // Boyut sıkıştırıldı
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(32.dp), ambientColor = Color.White.copy(alpha = 0.3f), spotColor = Color.White.copy(alpha = 0.3f)),
                enabled = canSubmit,
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = Color.White)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 3.dp)
                } else {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Kayıt Ol", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp)) // Sıkıştırıldı

            // Login Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Zaten hesabınız var mı?", style = MaterialTheme.typography.bodyLarge, color = PrimaryBlack.copy(alpha = 0.8f))
                TextButton(onClick = onNavigateToLogin) {
                    Text(text = "Giriş Yap", color = PrimaryBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) // Bottom spacing sıkıştırıldı
        }
    }
}