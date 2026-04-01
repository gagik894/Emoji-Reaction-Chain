package com.play.emojireactionchain.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.play.emojireactionchain.R
import com.play.emojireactionchain.ui.theme.PrimarySoft
import com.play.emojireactionchain.ui.theme.TextMain
import com.play.emojireactionchain.ui.theme.TextSecondary
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.SoundManager
import com.play.emojireactionchain.viewModel.SurvivalGameViewModel
import kotlinx.coroutines.delay

@Composable
fun SurvivalModeScreen(onNavigateToStart: () -> Unit = {}) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val highScoreManager = remember { HighScoreManager(context) }

    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }

    val viewModel: SurvivalGameViewModel = viewModel(
        key = "SurvivalGameViewModel",
        factory = gameViewModelFactory { SurvivalGameViewModel(soundManager, highScoreManager) }
    )
    val gameState by viewModel.gameState.collectAsState()

    var showTimeBonusAnimation by remember { mutableStateOf(false) }
    var currentBonusPointsForAnimation by remember { mutableIntStateOf(0) }

    LaunchedEffect(gameState.currentTimeBonus) {
        if (gameState.currentTimeBonus > 0) {
            showTimeBonusAnimation = true
            currentBonusPointsForAnimation = gameState.currentTimeBonus
            delay(1000)
            showTimeBonusAnimation = false
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box {
        GameScreenLayout {
            GameHeader(onBack = onNavigateToStart)

            if (gameState.questionNumber == 0) {
                PreGameContent(
                    R.string.mode_survival_name,
                    R.string.pregame_survival_description,
                    highScore = gameState.highScore,
                    onStartGame = { viewModel.startGame() }
                )
            } else {
                if (isLandscape) {
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Scoreboard(gameState.score, gameState.highScore, gameState.lives, gameState.currentStreakCount)
                            EngagementStrip(gameState.isBonusRound, gameState.streakMissionProgress, gameState.streakMissionTarget)
                            HintCard(gameState.hintRes, gameState.categoryEmoji)
                            LevelIndicator(viewModel.level)
                            EmojiChainDisplay(gameState.emojiChain)
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                            ChoiceButtons(gameState.choices, gameState.correctAnswerEmoji, gameState.isCorrectAnswer, viewModel::handleChoice)
                        }
                    }
                } else {
                    Scoreboard(gameState.score, gameState.highScore, gameState.lives, gameState.currentStreakCount)
                    EngagementStrip(gameState.isBonusRound, gameState.streakMissionProgress, gameState.streakMissionTarget)
                    HintCard(gameState.hintRes, gameState.categoryEmoji)
                    LevelIndicator(viewModel.level)
                    EmojiChainDisplay(gameState.emojiChain)
                    ChoiceButtons(gameState.choices, gameState.correctAnswerEmoji, gameState.isCorrectAnswer, viewModel::handleChoice)
                }

                GameResultHandler(
                    gameState = gameState,
                    onStartGame = { viewModel.startGame() },
                    onHandleAdReward = { viewModel.handleAdReward() },
                    onBack = onNavigateToStart
                )
            }
        }

        if (showTimeBonusAnimation) {
            TimeBonusAnimation(bonusPoints = currentBonusPointsForAnimation)
        }
    }
}

@Composable
private fun LevelIndicator(level: Int) {
    Surface(
        modifier = Modifier.padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.small,
        color = PrimarySoft.copy(alpha = 0.1f)
    ) {
        Text(
            text = stringResource(R.string.survival_level, level),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Black,
                color = PrimarySoft
            )
        )
    }
}

@Composable
fun PreGameContent(
    gameModeNameRes: Int,
    gameDescriptionRes: Int,
    highScore: Int,
    onStartGame: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(gameModeNameRes),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = TextMain
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(gameDescriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        if (highScore > 0) {
            Text(
                text = stringResource(R.string.pregame_best_score, highScore),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = PrimarySoft
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        StyledActionButton(text = stringResource(R.string.pregame_start_playing), onClick = onStartGame)
    }
}