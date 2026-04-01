package com.play.emojireactionchain.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.play.emojireactionchain.R
import com.play.emojireactionchain.ui.theme.ErrorRed
import com.play.emojireactionchain.ui.theme.TextMain
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.SoundManager
import com.play.emojireactionchain.viewModel.BlitzGameViewModel
import kotlinx.coroutines.delay

@SuppressLint("DefaultLocale")
@Composable
fun BlitzModeScreen(
    onNavigateToStart: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val highScoreManager = remember { HighScoreManager(context) }

    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }

    val viewModel: BlitzGameViewModel = viewModel(
        key = "BlitzGameViewModel",
        factory = gameViewModelFactory { BlitzGameViewModel(soundManager, highScoreManager) }
    )
    val gameState by viewModel.gameState.collectAsState()
    val remainingQuestionTimeMs by viewModel.remainingQuestionTimeMsFlow.collectAsState()

    var showTimeBonusAnimation by remember { mutableStateOf(false) }
    var currentBonusPointsForAnimation by remember { mutableIntStateOf(0) }

    LaunchedEffect(gameState.currentTimeBonus) {
        if (gameState.currentTimeBonus > 0) {
            showTimeBonusAnimation = true
            currentBonusPointsForAnimation = gameState.currentTimeBonus
            delay(800)
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
                    R.string.mode_blitz_name,
                    R.string.pregame_blitz_description,
                    highScore = gameState.highScore,
                    onStartGame = { viewModel.startGame() }
                )
            } else {
                if (isLandscape) {
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Scoreboard(gameState.score, gameState.highScore, gameState.lives, gameState.currentStreakCount)
                            BlitzTimerDisplay(remainingQuestionTimeMs / 1000.0)
                            EmojiChainDisplay(gameState.emojiChain)
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                            ChoiceButtons(gameState.choices, gameState.correctAnswerEmoji, gameState.isCorrectAnswer, viewModel::handleChoice)
                        }
                    }
                } else {
                    Scoreboard(gameState.score, gameState.highScore, gameState.lives, gameState.currentStreakCount)
                    BlitzTimerDisplay(remainingQuestionTimeMs / 1000.0)
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

@SuppressLint("DefaultLocale")
@Composable
private fun BlitzTimerDisplay(seconds: Double) {
    val isUrgent = seconds < 1.0
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format("%.2f", seconds),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 44.sp,
                letterSpacing = (-1).sp
            ),
            color = if (isUrgent) ErrorRed else TextMain
        )
    }
}
