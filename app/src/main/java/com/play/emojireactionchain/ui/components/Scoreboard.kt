package com.play.emojireactionchain.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.play.emojireactionchain.ui.theme.SecondarySoft
import com.play.emojireactionchain.ui.theme.SuccessGreen
import com.play.emojireactionchain.ui.theme.WarningOrange

@Composable
fun Scoreboard(
    score: Int,
    highScore: Int,
    lives: Int?,
    currentStreakCount: Int,
    onBack: (() -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val hasMiddleStat = lives != null || currentStreakCount > 1

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isLandscape) 2.dp else 4.dp),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(
                start = if (onBack != null) 6.dp else 12.dp,
                end = 12.dp,
                top = if (isLandscape) 8.dp else 9.dp,
                bottom = if (isLandscape) 8.dp else 9.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            onBack?.let {
                IconButton(
                    onClick = it,
                    modifier = Modifier
                        .size(if (isLandscape) 40.dp else 44.dp)
                        .background(PrimarySoft.copy(alpha = 0.14f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.game_header_back_description),
                        tint = PrimarySoft
                    )
                }
            }

            StatBlock(
                label = stringResource(R.string.score_label),
                value = score.toString(),
                icon = Icons.Rounded.Stars,
                color = PrimarySoft,
                modifier = Modifier.weight(1f)
            )

            MiniDivider()

            if (hasMiddleStat) {
                if (lives != null) {
                    LivesBlock(
                        lives = lives,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    StatBlock(
                        label = stringResource(R.string.streak_label_short),
                        value = currentStreakCount.toString(),
                        icon = Icons.Rounded.LocalFireDepartment,
                        color = WarningOrange,
                        modifier = Modifier.weight(1f)
                    )
                }

                MiniDivider()
            }

            StatBlock(
                label = stringResource(R.string.best_label),
                value = highScore.toString(),
                icon = Icons.Rounded.EmojiEvents,
                color = if (lives == null) SecondarySoft else SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatBlock(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.14f)) {
            Box(modifier = Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(3.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                maxLines = 1,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 21.sp),
                color = color,
                maxLines = 1,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun LivesBlock(lives: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.lives_label),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = SecondarySoft,
                    modifier = Modifier
                        .size(21.dp)
                        .graphicsLayer { alpha = if (index < lives) 1f else 0.22f }
                )
            }
        }
    }
}

@Composable
private fun MiniDivider() {
    Box(
        modifier = Modifier
            .size(width = 1.dp, height = 42.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    )
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
