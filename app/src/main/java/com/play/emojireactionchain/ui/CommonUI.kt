package com.play.emojireactionchain.ui

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.play.emojireactionchain.ui.theme.PrimarySoft
import com.play.emojireactionchain.ui.theme.SecondarySoft
import com.play.emojireactionchain.ui.theme.TertiarySoft

/**
 * A reusable animated background decoration component that provides depth and character.
 * It contains floating emojis and animated geometric shapes.
 */
@Composable
fun GameBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    val backgroundBrush = if (!isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF8FAFF),
                Color(0xFFE0E7FF),
                Color(0xFFD1DBFF)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1E1B4B),
                Color(0xFF0F172A)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        FloatingGameDecorations(isDark = isDark)
        content()
    }
}

@Composable
private fun FloatingGameDecorations(isDark: Boolean) {
    val alpha = if (isDark) 0.15f else 0.40f
    val infiniteTransition = rememberInfiniteTransition(label = "background_float")
    
    val yOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y_offset_1"
    )

    val yOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y_offset_2"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // --- LAYER 1: Large items ---
        
        // Top Left
        FloatingItem(
            modifier = Modifier.align(Alignment.TopStart).offset(x = 30.dp, y = 100.dp),
            yOffset = yOffset1, rotation = rotation, alpha = alpha
        ) {
            Text("🎮", fontSize = 36.sp)
        }

        // Top Right
        FloatingItem(
            modifier = Modifier.align(Alignment.TopEnd).offset(x = (-40).dp, y = 120.dp),
            yOffset = yOffset2, rotation = -rotation, alpha = alpha
        ) {
            Icon(Icons.Filled.Star, null, tint = PrimarySoft, modifier = Modifier.size(32.dp))
        }

        // Bottom Left
        FloatingItem(
            modifier = Modifier.align(Alignment.BottomStart).offset(x = 50.dp, y = (-160).dp),
            yOffset = yOffset1, rotation = rotation, alpha = alpha
        ) {
            Icon(Icons.Filled.Rocket, null, tint = SecondarySoft, modifier = Modifier.size(40.dp))
        }

        // Bottom Right
        FloatingItem(
            modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-50).dp, y = (-120).dp),
            yOffset = yOffset2, rotation = -rotation * 0.8f, alpha = alpha
        ) {
            Text("👾", fontSize = 36.sp)
        }

        // --- LAYER 2: Medium/Small items scattered ---

        // Middle Left
        FloatingItem(
            modifier = Modifier.align(Alignment.CenterStart).offset(x = 20.dp, y = (-150).dp),
            yOffset = yOffset2 * 0.6f, rotation = rotation * 0.5f, alpha = alpha * 0.7f
        ) {
            Text("✨", fontSize = 28.sp)
        }

        // Middle Right
        FloatingItem(
            modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-40).dp, y = 80.dp),
            yOffset = yOffset1 * 0.8f, rotation = -rotation * 0.4f, alpha = alpha * 0.7f
        ) {
            Text("🧩", fontSize = 30.sp)
        }

        // Top Center area
        FloatingItem(
            modifier = Modifier.align(Alignment.TopCenter).offset(x = (-80).dp, y = 40.dp),
            yOffset = yOffset2 * 0.4f, rotation = rotation * 1.5f, alpha = alpha * 0.5f
        ) {
            Text("⚡", fontSize = 24.sp)
        }

        // Bottom Center area
        FloatingItem(
            modifier = Modifier.align(Alignment.BottomCenter).offset(x = 70.dp, y = (-60).dp),
            yOffset = yOffset1 * 0.5f, rotation = -rotation, alpha = alpha * 0.6f
        ) {
            Text("💎", fontSize = 26.sp)
        }

        // Random floaters
        FloatingItem(
            modifier = Modifier.align(Alignment.Center).offset(x = 120.dp, y = 180.dp),
            yOffset = yOffset1 * 1.1f, rotation = rotation * 0.7f, alpha = alpha * 0.8f
        ) {
            Text("🕹️", fontSize = 28.sp)
        }

        FloatingItem(
            modifier = Modifier.align(Alignment.Center).offset(x = (-110).dp, y = 220.dp),
            yOffset = yOffset2 * 0.9f, rotation = -rotation * 0.6f, alpha = alpha * 0.6f
        ) {
            Text("🌈", fontSize = 24.sp)
        }
        
        // --- CANVAS: Background Orbs & Accents ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Main decorative orbs
            drawCircle(
                color = PrimarySoft.copy(alpha = alpha * 0.25f),
                radius = 220.dp.toPx(),
                center = Offset(size.width * 0.95f, size.height * 0.15f + yOffset1.dp.toPx())
            )
            drawCircle(
                color = SecondarySoft.copy(alpha = alpha * 0.25f),
                radius = 300.dp.toPx(),
                center = Offset(size.width * 0.05f, size.height * 0.85f - yOffset2.dp.toPx())
            )
            drawCircle(
                color = TertiarySoft.copy(alpha = alpha * 0.2f),
                radius = 160.dp.toPx(),
                center = Offset(size.width * 0.5f, size.height * 0.5f + (yOffset1.dp.toPx() * 0.5f))
            )

            // Tiny background "stars" or particles
            val particleColor = if (isDark) Color.White.copy(alpha = 0.15f) else PrimarySoft.copy(alpha = 0.15f)
            val particles = listOf(
                Offset(size.width * 0.2f, size.height * 0.3f),
                Offset(size.width * 0.7f, size.height * 0.6f),
                Offset(size.width * 0.4f, size.height * 0.1f),
                Offset(size.width * 0.8f, size.height * 0.9f),
                Offset(size.width * 0.1f, size.height * 0.5f),
                Offset(size.width * 0.9f, size.height * 0.4f)
            )
            particles.forEach { pos ->
                drawCircle(color = particleColor, radius = 2.dp.toPx(), center = pos)
            }
        }
    }
}

@Composable
private fun FloatingItem(
    modifier: Modifier,
    yOffset: Float,
    rotation: Float,
    alpha: Float,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = yOffset
                rotationZ = rotation
                this.alpha = alpha
            }
    ) {
        content()
    }
}
