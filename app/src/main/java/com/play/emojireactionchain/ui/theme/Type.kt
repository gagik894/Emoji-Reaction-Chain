package com.play.emojireactionchain.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Custom Typography for Emoji Game
val appTypography = Typography( // Define custom Typography
    headlineMedium = TextStyle( // Style for "Emoji Reaction Chain" title
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default // Or choose a specific font family
    ),
    bodyLarge = TextStyle( // Style for "Score"
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Default
    ),
    headlineSmall = TextStyle( // Style for "Game Over!"
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Default
    ),
    bodyMedium = TextStyle( // Style for "Final Score" and button text
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Default
    )
    // You can add more custom styles here if needed
)