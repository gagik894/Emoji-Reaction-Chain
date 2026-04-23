package com.play.emojireactionchain.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.play.emojireactionchain.ui.theme.WarningOrange
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
            // Blitz max time is 3 seconds
            val progress = (remainingQuestionTimeMs / 3000f).coerceIn(0f, 1f)
            BlitzTimerWithVisualBar(remainingQuestionTimeMs / 1000.0, progress)
        }
    )
}

@SuppressLint("DefaultLocale")
@Composable
private fun BlitzTimerWithVisualBar(seconds: Double, progress: Float) {
    val isUrgent = seconds < 1.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val color = if (isUrgent) ErrorRed else WarningOrange
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = color.copy(alpha = 0.16f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Bolt, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    Text(
                        text = String.format("%.2f", seconds),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 0.sp
                        ),
                        color = color,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
        
        VisualTimerBar(progress = progress)
    }
}
