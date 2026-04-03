package com.play.emojireactionchain.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.play.emojireactionchain.R
import com.play.emojireactionchain.ui.GameBackground
import com.play.emojireactionchain.ui.theme.EmojiGameTheme
import com.play.emojireactionchain.ui.theme.PrimarySoft
import com.play.emojireactionchain.ui.theme.TextSecondary
import com.play.emojireactionchain.ui.theme.WarningOrange

@Composable
fun Scoreboard(score: Int, highScore: Int, lives: Int?, currentStreakCount: Int) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isLandscape) 4.dp else 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Score section
        Column {
            Text(
                text = stringResource(R.string.score_label),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp
                ),
                color = PrimarySoft
            )
        }

        // Streak indicator
        if (currentStreakCount > 1) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WarningOrange.copy(alpha = 0.15f)
            ) {
                Text(
                    text = stringResource(R.string.streak_label, currentStreakCount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = WarningOrange,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Lives/Best section
        Column(horizontalAlignment = Alignment.End) {
            lives?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { index ->
                        Text(
                            text = "❤️",
                            fontSize = 18.sp,
                            modifier = Modifier.graphicsLayer {
                                alpha = if (index < it) 1f else 0.2f
                            }
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.best_score_label, highScore),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScoreboardPreview() {
    EmojiGameTheme {
        GameBackground {
            Scoreboard(score = 150, highScore = 300, lives = 2, currentStreakCount = 3)
        }
    }
}
