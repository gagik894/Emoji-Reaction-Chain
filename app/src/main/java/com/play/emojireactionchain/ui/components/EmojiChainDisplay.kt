package com.play.emojireactionchain.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.play.emojireactionchain.ui.GameBackground
import com.play.emojireactionchain.ui.theme.EmojiGameTheme

@Composable
fun EmojiChainDisplay(emojiChain: List<String>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            var fontSize by remember { mutableStateOf(48.sp) }
            Text(
                text = emojiChain.joinToString(" "),
                style = TextStyle(fontSize = fontSize, fontWeight = FontWeight.Bold),
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { if (it.didOverflowWidth) fontSize *= 0.9f }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmojiChainDisplayPreview() {
    EmojiGameTheme {
        GameBackground {
            EmojiChainDisplay(emojiChain = listOf("🔥", "🪵", "🔥", "🚒"))
        }
    }
}
