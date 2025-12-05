package com.semptom.ai.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PrimaryBlack = Color(0xFF1C1C1C)
private val DarkBlueBorder = Color(0xFF0D47A1)
private val LightBlueAccent = Color(0xFF3B82F6)

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit
) {
    // Animation states
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 800,
            easing = EaseOutCubic
        ),
        label = "logo_scale"
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 600,
            easing = EaseInOutCubic
        ),
        label = "logo_alpha"
    )
    
    val textAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = 400,
            easing = EaseInOutCubic
        ),
        label = "text_alpha"
    )
    
    val pulseScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val gradientOffset by animateFloatAsState(
        targetValue = 1000f,
        animationSpec = tween(
            durationMillis = 2000,
            easing = LinearEasing
        ),
        label = "gradient_offset"
    )

    // Navigate after animation
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2500)
        onNavigateToMain()
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
                    startY = -gradientOffset,
                    endY = gradientOffset
                )
            )
    ) {
        // Animated background particles - simplified
        repeat(3) { index ->
            val particleScale by animateFloatAsState(
                targetValue = if (index % 2 == 0) 1.1f else 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500 + index * 300, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particle_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .offset(
                        x = (80 + index * 100).dp,
                        y = (120 + index * 60).dp
                    )
                    .graphicsLayer {
                        scaleX = particleScale
                        scaleY = particleScale
                        alpha = 0.08f
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo container with modern design
            Surface(
                modifier = Modifier
                    .size(180.dp)
                    .graphicsLayer {
                        scaleX = logoScale * pulseScale
                        scaleY = logoScale * pulseScale
                        alpha = logoAlpha
                    }
                    .shadow(
                        elevation = 25.dp,
                        shape = CircleShape,
                        ambientColor = Color.White.copy(alpha = 0.6f),
                        spotColor = Color.White.copy(alpha = 0.6f)
                    ),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(
                    3.dp,
                    Color.White.copy(alpha = 0.4f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Heart with pulse logo
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Logo",
                            modifier = Modifier.size(70.dp),
                            tint = Color.White
                        )

                        // SymptomAI text
                        Text(
                            text = "SymptomAI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App name with modern typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SymptomAI",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    fontSize = 56.sp,
                    letterSpacing = 3.sp,
                    modifier = Modifier.graphicsLayer {
                        alpha = textAlpha
                        translationY = -20.dp.toPx() * (1 - textAlpha)
                        scaleX = 0.9f + (0.1f * textAlpha)
                        scaleY = 0.9f + (0.1f * textAlpha)
                    }
                )

                Text(
                    text = "Akıllı Sağlık Asistanınız",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Light,
                    color = Color.Black.copy(alpha = 0.7f),
                    fontSize = 18.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.graphicsLayer {
                        alpha = textAlpha
                        translationY = 20.dp.toPx() * (1 - textAlpha)
                    }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator with modern design
            Surface(
                modifier = Modifier
                    .size(90.dp)
                    .graphicsLayer {
                        alpha = textAlpha
                        rotationZ = 360f * (1 - textAlpha)
                    },
                shape = RoundedCornerShape(45.dp),
                color = Color.Black.copy(alpha = 0.08f),
                border = BorderStroke(
                    4.dp,
                    Color.Black.copy(alpha = 0.25f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(45.dp),
                        color = Color.Black.copy(alpha = 0.8f),
                        strokeWidth = 5.dp
                    )
                }
            }
        }

        // Bottom decorative elements
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(200.dp)
                .graphicsLayer {
                    alpha = 0.3f
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.1f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
        )
    }
}
