package com.play.emojireactionchain.ui

import androidx.compose.animation.core.EaseInOutSine
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
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.play.emojireactionchain.ui.theme.PrimarySoft
import com.play.emojireactionchain.ui.theme.SecondarySoft

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
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFD9E2FF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E1B4B)))
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
    // Adjusted alpha for better visibility, especially on light theme
    val alpha = if (isDark) 0.15f else 0.45f
    
    val infiniteTransition = rememberInfiniteTransition(label = "background_float")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y_offset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Strategic placement of decorations to avoid being covered by main UI elements
        // Upper left area
        Icon(
            imageVector = Icons.Filled.Gamepad,
            contentDescription = null,
            tint = PrimarySoft.copy(alpha = alpha),
            modifier = Modifier
                .size(40.dp)
                .offset(x = 30.dp, y = 100.dp + yOffset.dp)
                .graphicsLayer(alpha = alpha)
        )
        
        // Upper right area
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = PrimarySoft.copy(alpha = alpha),
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-30).dp, y = 150.dp - yOffset.dp)
                .graphicsLayer(alpha = alpha)
        )
        
        // Lower left area
        Icon(
            imageVector = Icons.Filled.Attractions,
            contentDescription = null,
            tint = PrimarySoft.copy(alpha = alpha),
            modifier = Modifier
                .size(44.dp)
                .align(Alignment.BottomStart)
                .offset(x = 40.dp, y = (-120).dp + yOffset.dp)
                .graphicsLayer(alpha = alpha)
        )
        
        // Lower right area
        Icon(
            imageVector = Icons.Filled.Rocket,
            contentDescription = null,
            tint = SecondarySoft.copy(alpha = alpha),
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-40).dp, y = (-180).dp - yOffset.dp)
                .graphicsLayer(alpha = alpha)
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw soft circles in the background
            drawCircle(
                color = PrimarySoft.copy(alpha = alpha),
                radius = 180.dp.toPx(),
                center = Offset(size.width * 0.85f, size.height * 0.15f + yOffset.dp.toPx())
            )
            drawCircle(
                color = SecondarySoft.copy(alpha = alpha),
                radius = 250.dp.toPx(),
                center = Offset(size.width * 0.15f, size.height * 0.85f - yOffset.dp.toPx())
            )
        }
    }
}
