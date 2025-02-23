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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.play.emojireactionchain.viewModel.TimedGameViewModel
import kotlinx.coroutines.delay

// Custom ViewModel Factory for TimedGameViewModel
class TimedGameViewModelFactory(
    private val soundManager: SoundManager,
    private val highScoreManager: HighScoreManager
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimedGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimedGameViewModel(soundManager, highScoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun TimedModeScreen(
    onNavigateToStart: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val highScoreManager = remember { HighScoreManager(context) }

    val viewModel: TimedGameViewModel = viewModel(
        key = "TimedGameViewModel", // Use a consistent key
        factory = TimedGameViewModelFactory(soundManager, highScoreManager)
    )
    val gameState by viewModel.gameState.collectAsState() // Use collectAsStateWithLifecycle
    val remainingTime by viewModel.remainingGameTimeFlow.collectAsState() // Observe the flow


    // --- Time Bonus Animation ---
    var showTimeBonusAnimation by remember { mutableStateOf(false) }
    var currentBonusPointsForAnimation by remember { mutableStateOf(0) }

    LaunchedEffect(gameState.currentTimeBonus) {
        if (gameState.currentTimeBonus > 0) {
            showTimeBonusAnimation = true
            currentBonusPointsForAnimation = gameState.currentTimeBonus
            delay(500) // Shorter animation for Blitz
            showTimeBonusAnimation = false
        }
    }

    Box {
        GameScreenLayout {
            GameHeader(
                onBack = onNavigateToStart,
            )
            if (gameState.questionNumber == 0) {
                PreGameContent(
                    gameModeName = "Timed Mode",
                    gameDescription = "Answer as many questions as you can before time runs out! Correct answers add time.",
                    highScore = gameState.highScore,
                    onStartGame = { viewModel.startGame() }
                )
            } else {
                Scoreboard(
                    score = gameState.score,
                    highScore = gameState.highScore,
                    lives = gameState.lives, // Even though it's always 1, good for consistency
                    currentStreakCount = gameState.currentStreakCount
                )
                Row( // Put QuestionProgress and Timer in a Row
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically // Vertically center
                ) {
                    //removed question progress

                    Text( // Display the timer
                        text = String.format("%.1f", remainingTime / 1000.0),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (remainingTime < 5000) Color.Red else MaterialTheme.colorScheme.onSurface, //red last 5 seconds
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

                when (gameState.gameResult) {
                    GameResult.InProgress -> {

                    }

                    GameResult.Won -> { // This won't happen in the current Timed mode design
                        YouWonDialog(gameState = gameState, onPlayAgain = { viewModel.startGame() })
                    }

                    is GameResult.Lost -> {
                        // Use a TimeUpDialog instead of YouLostDialog
                        TimeUpDialog(
                            gameState = gameState, // Pass the gameState for score display
                            onPlayAgain = {
                                viewModel.startGame()
                            }
                        )
                    }
                }
            }
        }
        if (showTimeBonusAnimation) {
            TimeBonusAnimation(bonusPoints = currentBonusPointsForAnimation)
        }
    }
}
