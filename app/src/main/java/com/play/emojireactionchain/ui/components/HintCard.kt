package com.play.emojireactionchain.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.play.emojireactionchain.R
import com.play.emojireactionchain.ui.GameBackground
import com.play.emojireactionchain.ui.theme.EmojiGameTheme
import com.play.emojireactionchain.ui.theme.SecondarySoft
import com.play.emojireactionchain.ui.theme.TextMain
import com.play.emojireactionchain.ui.theme.TextSecondary
import com.play.emojireactionchain.ui.theme.WarningOrange

@Composable
fun HintCard(hintRes: Int?, categoryEmoji: String?) {
    if (hintRes == null) return

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(24.dp),
            color = if (expanded) SecondarySoft.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
            border = BorderStroke(1.dp, if (expanded) SecondarySoft else SecondarySoft.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Hint",
                    tint = if (expanded) WarningOrange else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                if (categoryEmoji != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = categoryEmoji, fontSize = 20.sp)
                }
                AnimatedVisibility(
                    visible = !expanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "?",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.9f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, SecondarySoft.copy(alpha = 0.2f)),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = stringResource(hintRes),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextMain,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HintCardPreview() {
    EmojiGameTheme {
        GameBackground {
            HintCard(hintRes = R.string.pregame_normal_description, categoryEmoji = "🍕")
        }
    }
}
