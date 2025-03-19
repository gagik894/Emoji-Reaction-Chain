package com.play.emojireactionchain.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.SoundManager
import com.play.emojireactionchain.viewModel.BlitzGameViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Custom ViewModel Factory for BlitzGameViewModel
class BlitzGameViewModelFactory(
    private val soundManager: SoundManager,
    private val highScoreManager: HighScoreManager
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlitzGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BlitzGameViewModel(soundManager, highScoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun BlitzModeScreen(
    onNavigateToStart: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val highScoreManager = remember { HighScoreManager(context) }

    val viewModel: BlitzGameViewModel = viewModel(
        key = "BlitzGameViewModel",
        factory = BlitzGameViewModelFactory(soundManager, highScoreManager)
    )
    val gameState by viewModel.gameState.collectAsState()

    // --- Time Bonus Animation ---
    var showTimeBonusAnimation by remember { mutableStateOf(false) }
    var currentBonusPointsForAnimation by remember { mutableIntStateOf(0) }

    LaunchedEffect(gameState.currentTimeBonus) {
        if (gameState.currentTimeBonus > 0) {
            showTimeBonusAnimation = true
            currentBonusPointsForAnimation = gameState.currentTimeBonus
            delay(500)
            showTimeBonusAnimation = false
        }
    }


    // --- Timer Logic ---
    val coroutineScope = rememberCoroutineScope() // Get a scope tied to the Composable
    var timerJob by remember { mutableStateOf<Job?>(null) } // Keep track of the timer job
    var remainingTime by remember { mutableDoubleStateOf(viewModel.maxTimePerQuestionSeconds.toDouble()) } // Start with full time

    LaunchedEffect(gameState.questionNumber, gameState.gameResult) {
        println("questionNumber: ${gameState.questionNumber}, gameResult: ${gameState.gameResult}")
        if (gameState.questionNumber > 0 && gameState.gameResult == GameResult.InProgress) {
            timerJob?.cancel()
            remainingTime = viewModel.maxTimePerQuestionSeconds.toDouble()

            timerJob = coroutineScope.launch {
                val startTime = System.currentTimeMillis()
                while (remainingTime > 0) {
                    val elapsed = System.currentTimeMillis() - startTime
                    remainingTime =
                        ((viewModel.maxTimePerQuestionSeconds * 1000L - elapsed) / 1000.0).coerceAtLeast(
                            0.0
                        )
                    delay(25)
                }
                // Removed the call to any timeout handler here.
            }
        } else {
            timerJob?.cancel()
            remainingTime = viewModel.maxTimePerQuestionSeconds.toDouble()
        }
    }
    Box {
        GameScreenLayout {
            GameHeader(
                onBack = onNavigateToStart,
            )
            if (gameState.questionNumber == 0) {
                PreGameContent(
                    gameModeName = "Blitz Mode",
                    gameDescription = "Answer questions as fast as you can!  One mistake and it's game over!",
                    highScore = gameState.highScore,
                    onStartGame = { viewModel.startGame() }
                )
            } else {


                Scoreboard(
                    score = gameState.score,
                    highScore = gameState.highScore,
                    lives = gameState.lives,
                    currentStreakCount = gameState.currentStreakCount
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("%.1f", remainingTime),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (remainingTime < 1) Color.Red else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }

                EmojiChainDisplay(emojiChain = gameState.emojiChain)

                ChoiceButtons(
                    choices = gameState.choices,
                    correctAnswerEmoji = gameState.correctAnswerEmoji,
                    isCorrectAnswer = gameState.isCorrectAnswer,
                    onChoiceSelected = { choice -> viewModel.handleChoice(choice) }
                )

                GameResultHandler(
                    gameState = gameState,
                    onStartGame = { viewModel.startGame() },
                    onHandleAdReward = {
                        viewModel.handleAdReward()
                    },
                    onBack = onNavigateToStart
                )
            }
        }

        if (showTimeBonusAnimation) {
            TimeBonusAnimation(bonusPoints = currentBonusPointsForAnimation)
        }
    }
}