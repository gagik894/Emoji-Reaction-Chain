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
import androidx.compose.material.icons.rounded.Timer
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
import com.play.emojireactionchain.ui.theme.InfoBlue
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.SoundManager
import com.play.emojireactionchain.viewModel.TimedGameViewModel

@SuppressLint("DefaultLocale")
@Composable
fun TimedModeScreen(
    onNavigateToStart: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val highScoreManager = remember { HighScoreManager(context) }

    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }

    val viewModel: TimedGameViewModel = viewModel(
        key = "TimedGameViewModel",
        factory = gameViewModelFactory { TimedGameViewModel(soundManager, highScoreManager) }
    )
    val gameState by viewModel.gameState.collectAsState()
    val remainingTime by viewModel.remainingGameTimeFlow.collectAsState()

    BaseGameScreen(
        gameModeNameRes = R.string.mode_timed_name,
        gameDescriptionRes = R.string.pregame_timed_description,
        gameState = gameState,
        onStartGame = { viewModel.startGame() },
        onNavigateToStart = onNavigateToStart,
        onChoiceSelected = viewModel::handleChoice,
        onHandleAdReward = { viewModel.handleAdReward() },
        bonusAnimationDelay = 1000L,
        showLivesInScoreboard = false,
        centerContent = {
            // Reference time for progress bar is 60 seconds
            val progress = (remainingTime / 60000f).coerceIn(0f, 1f)
            TimerWithVisualBar(remainingTime, progress)
        }
    )
}

@SuppressLint("DefaultLocale")
@Composable
private fun TimerWithVisualBar(remainingTime: Long, progress: Float) {
    val seconds = remainingTime / 1000.0
    val isUrgent = remainingTime < 10000

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val color = if (isUrgent) ErrorRed else InfoBlue
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    Text(
                        text = String.format("%.1f", seconds),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
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
