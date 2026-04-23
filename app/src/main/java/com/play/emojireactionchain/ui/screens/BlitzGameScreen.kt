package com.play.emojireactionchain.ui.screens

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.play.emojireactionchain.R
import com.play.emojireactionchain.ui.gameViewModelFactory
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.SoundManager
import com.play.emojireactionchain.viewModel.BlitzGameViewModel

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

    BaseGameScreen(
        gameModeNameRes = R.string.mode_blitz_name,
        gameDescriptionRes = R.string.pregame_blitz_description,
        gameState = gameState,
        onStartGame = { viewModel.startGame() },
        onNavigateToStart = onNavigateToStart,
        onChoiceSelected = viewModel::handleChoice,
        onHandleAdReward = { viewModel.handleAdReward() },
        bonusAnimationDelay = 800L,
        timerProgress = (remainingQuestionTimeMs / 3000f).coerceIn(0f, 1f),
        centerContent = {}
    )
}
