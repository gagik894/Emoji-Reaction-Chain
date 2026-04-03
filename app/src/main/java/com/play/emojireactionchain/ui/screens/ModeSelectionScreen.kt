package com.play.emojireactionchain.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.play.emojireactionchain.R
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.ui.HomeUiState
import com.play.emojireactionchain.ui.theme.PrimarySoft
import com.play.emojireactionchain.ui.theme.SecondarySoft
import com.play.emojireactionchain.ui.theme.TextSecondary
import com.play.emojireactionchain.ui.theme.WarningOrange

private data class GameModeItem(
    val mode: GameMode,
    val nameRes: Int,
    val subtitleRes: Int,
    val icon: ImageVector,
    val colors: List<Color>,
    val emoji: String
)

private val gameModes = listOf(
    GameModeItem(
        GameMode.NORMAL, R.string.mode_normal_name, R.string.mode_normal_subtitle,
        Icons.Filled.PlayArrow, listOf(Color(0xFF43A047), Color(0xFF2E7D32)), "🟢"
    ),
    GameModeItem(
        GameMode.TIMED, R.string.mode_timed_name, R.string.mode_timed_subtitle,
        Icons.Filled.Timer, listOf(Color(0xFF1E88E5), Color(0xFF1565C0)), "⏳"
    ),
    GameModeItem(
        GameMode.SURVIVAL, R.string.mode_survival_name, R.string.mode_survival_subtitle,
        Icons.Filled.Shield, listOf(Color(0xFFFBC02D), Color(0xFFF57F17)), "🛡️"
    ),
    GameModeItem(
        GameMode.BLITZ, R.string.mode_blitz_name, R.string.mode_blitz_subtitle,
        Icons.Filled.Bolt, listOf(Color(0xFFD81B60), Color(0xFFAD1457)), "⚡"
    )
)

@Composable
fun ModeSelectionScreen(
    uiState: HomeUiState,
    onModeSelected: (GameMode) -> Unit,
    onCollectionSelected: () -> Unit
) {
    val isPreview = LocalInspectionMode.current
    var visible by remember { mutableStateOf(isPreview) }
    LaunchedEffect(Unit) { if (!isPreview) visible = true }

    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            Spacer(modifier = Modifier.height(24.dp))

            HeaderSection(visible, isDark, uiState.dailyStreak, onCollectionSelected)

            if (uiState.dailyStickerEmoji != null) {
                Spacer(modifier = Modifier.height(16.dp))
                StickerCelebrationCard(uiState.dailyStickerEmoji)
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionTitleSection(visible, isDark)

            Spacer(modifier = Modifier.height(20.dp))

            GameModesGrid(isPreview, uiState.modeHighScores, onModeSelected)
    }
}

@Composable
private fun HeaderSection(
    visible: Boolean,
    isDark: Boolean,
    dailyStreak: Int,
    onCollectionSelected: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { -40 })
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.mode_selection_brand_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        brush = Brush.horizontalGradient(listOf(PrimarySoft, SecondarySoft)),
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = stringResource(R.string.mode_selection_brand_subtitle),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isDark) Color.White.copy(alpha = 0.5f) else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )
            }

            StreakBadge(dailyStreak, onCollectionSelected)
        }
    }
}

@Composable
private fun StickerCelebrationCard(stickerEmoji: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SecondarySoft.copy(alpha = 0.22f),
        border = BorderStroke(2.dp, Brush.linearGradient(listOf(PrimarySoft, SecondarySoft)))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "✨", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.sticker_daily_reward, stickerEmoji),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stickerEmoji, fontSize = 28.sp)
        }
    }
}

@Composable
private fun SectionTitleSection(visible: Boolean, isDark: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600, 200))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.mode_selection_ready_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else PrimarySoft
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .weight(1f)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                (if (isDark) Color.White else PrimarySoft).copy(alpha = if (isDark) 0.35f else 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun GameModesGrid(
    isPreview: Boolean,
    bestScores: Map<GameMode, Int>,
    onModeSelected: (GameMode) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        itemsIndexed(gameModes) { index, item ->
            var itemVisible by remember { mutableStateOf(isPreview) }
            LaunchedEffect(Unit) {
                if (!isPreview) {
                    kotlinx.coroutines.delay(150L + index * 80L)
                    itemVisible = true
                }
            }
            AnimatedVisibility(
                visible = itemVisible,
                enter = fadeIn(tween(500)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                GameHeroCard(
                    item = item,
                    highScore = bestScores[item.mode] ?: 0,
                    onClick = { onModeSelected(item.mode) }
                )
            }
        }
    }
}

@Composable
private fun StreakBadge(streak: Int, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streak_scale"
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .shadow(20.dp, CircleShape, ambientColor = WarningOrange)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(3.dp, Brush.linearGradient(listOf(WarningOrange, Color(0xFFFFD54F), WarningOrange)))
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🔥", fontSize = 28.sp)
                Text(
                    text = streak.toString(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun GameHeroCard(
    item: GameModeItem,
    highScore: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "card_scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            modifier = Modifier
                .aspectRatio(0.85f)
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(item.colors))
                    .padding(16.dp)
            ) {
                HighScoreBadge(highScore, Modifier.align(Alignment.TopEnd))

                // Background decorative emoji
                Text(
                    text = item.emoji,
                    fontSize = 90.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer(alpha = 0.15f)
                        .rotate(-15f)
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    IconContainer(item.icon)

                    Text(
                        text = stringResource(item.nameRes).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(item.subtitleRes),
            style = MaterialTheme.typography.labelSmall.copy(
                lineHeight = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.ExtraBold
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun HighScoreBadge(highScore: Int, modifier: Modifier = Modifier) {
    if (highScore > 0) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.25f),
            modifier = modifier
        ) {
            Text(
                text = stringResource(R.string.mode_selection_high_score, highScore),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun IconContainer(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(56.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.25f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
