package com.play.emojireactionchain.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.play.emojireactionchain.R
import com.play.emojireactionchain.ui.gameViewModelFactory
import com.play.emojireactionchain.ui.theme.PrimarySoft
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.SoundManager
import com.play.emojireactionchain.viewModel.SurvivalGameViewModel

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

    BaseGameScreen(
        gameModeNameRes = R.string.mode_survival_name,
        gameDescriptionRes = R.string.pregame_survival_description,
        gameState = gameState,
        onStartGame = { viewModel.startGame() },
        onNavigateToStart = onNavigateToStart,
        onChoiceSelected = viewModel::handleChoice,
        onHandleAdReward = { viewModel.handleAdReward() },
        bonusAnimationDelay = 1000L,
        centerContent = {
            LevelIndicator(viewModel.level)
        }
    )
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
