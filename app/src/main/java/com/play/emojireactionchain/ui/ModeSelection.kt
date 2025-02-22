package com.play.emojireactionchain.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.play.emojireactionchain.model.GameMode

@Composable
fun ModeSelectionScreen(onModeSelected: (GameMode) -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Choose a Game Mode",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 columns
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GameModeCard(
                    icon = Icons.Filled.PlayArrow,
                    modeName = "Normal Mode",
                    cardColor = Color(0xFFA5D6A7),
                    iconTint = Color(0xFF4CAF50)
                ) { onModeSelected(GameMode.NORMAL) }
            }
            item {
                GameModeCard(
                    icon = Icons.Filled.Timer,
                    modeName = "Timed Mode",
                    cardColor = Color(0xFF90CAF9),
                    iconTint = Color(0xFF2196F3)
                ) { onModeSelected(GameMode.TIMED) }
            }
            item {
                GameModeCard(
                    icon = Icons.Filled.Lock,
                    modeName = "Decoding Mode",
                    cardColor = Color(0xFFFFD54F),
                    iconTint = Color(0xFFFF9800)
                ) { onModeSelected(GameMode.DECODING) }
            }
            item {
                GameModeCard(
                    icon = Icons.Filled.Bolt, // Example icon for Blitz Mode
                    modeName = "Blitz Mode",
                    cardColor = Color(0xFFF48FB1), // Example color
                    iconTint = Color(0xFFE91E63)  // Example color
                ) { onModeSelected(GameMode.BLITZ) } // Assuming you add a BLITZ enum value
            }
        }
    }
}

@Composable
fun GameModeCard(
    icon: ImageVector,
    modeName: String,
    cardColor: Color,
    iconTint: Color,
    onModeSelected: () -> Unit
) {
    Card (
        onClick = onModeSelected,
        modifier = Modifier.size(150.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = modeName,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 8.dp),
                tint = iconTint
            )
            Text(
                modeName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}