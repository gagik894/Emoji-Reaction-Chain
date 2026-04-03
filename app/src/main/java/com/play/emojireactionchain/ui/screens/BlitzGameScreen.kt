package com.play.emojireactionchain.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.play.emojireactionchain.R
import com.play.emojireactionchain.ui.gameViewModelFactory
import com.play.emojireactionchain.ui.theme.ErrorRed
import com.play.emojireactionchain.ui.theme.TextMain
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
        centerContent = {
            BlitzTimerDisplay(remainingQuestionTimeMs / 1000.0)
        }
    )
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