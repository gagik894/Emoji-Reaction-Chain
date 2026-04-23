package com.play.emojireactionchain.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.play.emojireactionchain.ui.GameBackground
import com.play.emojireactionchain.ui.theme.EmojiGameTheme

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun EmojiChainDisplay(emojiChain: List<String>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(34.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        shadowElevation = 8.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            val slotCount = (emojiChain.size + 1).coerceAtLeast(3)
            val tileSize = ((maxWidth - 40.dp) / slotCount.toFloat()).coerceIn(42.dp, 70.dp)
            val connectorWidth = if (slotCount >= 6) 6.dp else 10.dp

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                emojiChain.forEachIndexed { index, emoji ->
                    EmojiPathTile(emoji = emoji, size = tileSize, index = index)
                    ChainConnector(width = connectorWidth)
                }
                MysteryTile(size = tileSize)
            }
        }
    }
}

@Composable
private fun EmojiPathTile(emoji: String, size: Dp, index: Int) {
    val theme = MaterialTheme.colorScheme
    val colors = listOf(theme.primary, theme.secondary, theme.tertiary)
    val tint = colors[index % colors.size]

    Surface(
        modifier = Modifier
            .size(size)
            .shadow(8.dp, RoundedCornerShape(22.dp), clip = false),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .background(tint.copy(alpha = 0.12f))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = (size.value * 0.52f).sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MysteryTile(size: Dp) {
    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 10.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Help,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

@Composable
private fun ChainConnector(width: Dp) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .size(width = width, height = 26.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)) {
            Icon(
                imageVector = Icons.Rounded.Link,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                modifier = Modifier
                    .size(18.dp)
                    .padding(2.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmojiChainDisplayShortPreview() {
    EmojiGameTheme {
        GameBackground {
            EmojiChainDisplay(emojiChain = listOf("A", "B"))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmojiChainDisplayLongPreview() {
    EmojiGameTheme {
        GameBackground {
            EmojiChainDisplay(emojiChain = listOf("A", "B", "C", "D", "E"))
        }
    }
}
