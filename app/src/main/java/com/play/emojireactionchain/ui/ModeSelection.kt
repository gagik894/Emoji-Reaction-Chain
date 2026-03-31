package com.play.emojireactionchain.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.play.emojireactionchain.R
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.ui.theme.PastelBlue
import com.play.emojireactionchain.ui.theme.PastelGreen
import com.play.emojireactionchain.ui.theme.PastelPink
import com.play.emojireactionchain.ui.theme.PastelYellow
import com.play.emojireactionchain.ui.theme.PrimarySoft
import com.play.emojireactionchain.ui.theme.SecondarySoft
import com.play.emojireactionchain.ui.theme.SurfaceWhite
import com.play.emojireactionchain.ui.theme.TertiarySoft
import com.play.emojireactionchain.ui.theme.TextMain

private data class GameModeItem(
    val mode: GameMode,
    val nameRes: Int,
    val subtitleRes: Int,
    val icon: ImageVector,
    val cardColor: Color,
    val accentColor: Color
)

private val gameModes = listOf(
    GameModeItem(GameMode.NORMAL, R.string.mode_normal_name, R.string.mode_normal_subtitle, Icons.Filled.PlayArrow, PastelGreen, Color(0xFF43A047)),
    GameModeItem(GameMode.TIMED, R.string.mode_timed_name, R.string.mode_timed_subtitle, Icons.Filled.Timer, PastelBlue, Color(0xFF1E88E5)),
    GameModeItem(GameMode.SURVIVAL, R.string.mode_survival_name, R.string.mode_survival_subtitle, Icons.Filled.Shield, PastelYellow, Color(0xFFFBC02D)),
    GameModeItem(GameMode.BLITZ, R.string.mode_blitz_name, R.string.mode_blitz_subtitle, Icons.Filled.Bolt, PastelPink, SecondarySoft)
)

@Composable
fun ModeSelectionScreen(
    dailyStreak: Int,
    bestScores: Map<GameMode, Int>,
    onModeSelected: (GameMode) -> Unit
) {
    var showHeader by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showHeader = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = showHeader) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp,
                            lineHeight = 42.sp
                        ),
                        color = PrimarySoft,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            HomeStatsRowWithStyle(
                dailyStreak = dailyStreak,
                bestScores = bestScores
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(gameModes) { item ->
                    GameModeCardStyled(
                        icon = item.icon,
                        modeName = stringResource(item.nameRes),
                        modeSubtitle = stringResource(item.subtitleRes),
                        cardColor = item.cardColor,
                        accentColor = item.accentColor,
                        onModeSelected = { onModeSelected(item.mode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeStatsRowWithStyle(
    dailyStreak: Int,
    bestScores: Map<GameMode, Int>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp))
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(TertiarySoft.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔥", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.daily_streak_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.daily_streak_value, dailyStreak),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextMain
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.best_scores_label),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        val modeOrder = listOf(GameMode.NORMAL, GameMode.TIMED, GameMode.SURVIVAL, GameMode.BLITZ)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(modeOrder) { mode ->
                val shortLabel = stringResource(shortModeLabelRes(mode))
                val score = bestScores[mode] ?: 0
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = null,
                    modifier = Modifier.shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(getModeAccent(mode), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$shortLabel: $score",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = TextMain
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameModeCardStyled(
    icon: ImageVector,
    modeName: String,
    modeSubtitle: String,
    cardColor: Color,
    accentColor: Color,
    onModeSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(180.dp)
            .fillMaxWidth()
            .clickable { onModeSelected() }
            .shadow(4.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = accentColor
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = modeName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextMain,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = modeSubtitle,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 14.sp),
                    color = TextMain.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun shortModeLabelRes(mode: GameMode): Int {
    return when (mode) {
        GameMode.NORMAL -> R.string.mode_short_normal
        GameMode.TIMED -> R.string.mode_short_timed
        GameMode.SURVIVAL -> R.string.mode_short_survival
        GameMode.BLITZ -> R.string.mode_short_blitz
    }
}

private fun getModeAccent(mode: GameMode): Color {
    return when (mode) {
        GameMode.NORMAL -> Color(0xFF43A047)
        GameMode.TIMED -> Color(0xFF1E88E5)
        GameMode.SURVIVAL -> Color(0xFFFBC02D)
        GameMode.BLITZ -> SecondarySoft
    }
}
