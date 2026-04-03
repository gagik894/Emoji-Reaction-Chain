package com.play.emojireactionchain.ui.screens

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.play.emojireactionchain.R
import com.play.emojireactionchain.ui.components.QuestionProgress
import com.play.emojireactionchain.ui.gameViewModelFactory
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.SoundManager
import com.play.emojireactionchain.viewModel.NormalGameViewModel

@Composable
fun NormalGameScreen(
    onNavigateToStart: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val highScoreManager = remember { HighScoreManager(context) }

    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }

    val viewModel: NormalGameViewModel = viewModel(
        factory = gameViewModelFactory { NormalGameViewModel(soundManager, highScoreManager) }
    )

    val gameState by viewModel.gameState.collectAsState()

    BaseGameScreen(
        gameModeNameRes = R.string.mode_normal_name,
        gameDescriptionRes = R.string.pregame_normal_description,
        gameState = gameState,
        onStartGame = { viewModel.startGame() },
        onNavigateToStart = onNavigateToStart,
        onChoiceSelected = viewModel::handleChoice,
        onHandleAdReward = { viewModel.handleAdReward() },
        bonusAnimationDelay = 1500L,
        centerContent = {
            if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                QuestionProgress(gameState.questionNumber, gameState.totalQuestions)
            }
        }
    )
}
