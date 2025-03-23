package com.play.emojireactionchain.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.play.emojireactionchain.model.GameMode

@Composable
fun ModeSelectionScreen(onModeSelected: (GameMode) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Emoji Reaction Chain",
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 32.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Select Game Mode",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
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
                    icon = Icons.Filled.Shield,
                    modeName = "Survival Mode",
                    cardColor = Color(0xFFFFD54F),
                    iconTint = Color(0xFFFF9800)
                ) { onModeSelected(GameMode.SURVIVAL) }
            }
            item {
                GameModeCard(
                    icon = Icons.Filled.Bolt,
                    modeName = "Blitz Mode",
                    cardColor = Color(0xFFF48FB1),
                    iconTint = Color(0xFFE91E63)
                ) { onModeSelected(GameMode.BLITZ) }
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
    Card(
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
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
    }
}