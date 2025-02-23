package com.play.emojireactionchain.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.SoundManager
import com.play.emojireactionchain.viewModel.SurvivalGameViewModel
import kotlinx.coroutines.delay

// Custom ViewModel Factory
class SurvivalGameViewModelFactory(
    private val soundManager: SoundManager,
    private val highScoreManager: HighScoreManager
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurvivalGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SurvivalGameViewModel(soundManager, highScoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
@Composable
fun SurvivalModeScreen(onNavigateToStart: () -> Unit = {}) { // Added default value
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val highScoreManager = remember { HighScoreManager(context) }

    val viewModel: SurvivalGameViewModel = viewModel(
        key = "SurvivalGameViewModel",
        factory = SurvivalGameViewModelFactory(soundManager, highScoreManager)
    )
    val gameState by viewModel.gameState.collectAsState()

    // --- Time Bonus Animation ---
    var showTimeBonusAnimation by remember { mutableStateOf(false) }
    var currentBonusPointsForAnimation by remember { mutableStateOf(0) }

    LaunchedEffect(gameState.currentTimeBonus) {
        if (gameState.currentTimeBonus > 0) {
            showTimeBonusAnimation = true
            currentBonusPointsForAnimation = gameState.currentTimeBonus
            delay(500)
            showTimeBonusAnimation = false
        }
    }

    Box {
        GameScreenLayout {
            GameHeader(
                onBack = onNavigateToStart,
            )

            // --- Pre-Game State ---
            if (gameState.questionNumber == 0) {
                PreGameContent(
                    gameModeName = "Survival Mode",
                    gameDescription = "Answer questions correctly to increase your score and level. You have 3 lives!",
                    highScore = gameState.highScore,
                    onStartGame = { viewModel.startGame() }
                )
            } else { // --- In-Game State ---

                Scoreboard(
                    score = gameState.score,
                    highScore = gameState.highScore,
                    lives = gameState.lives,
                    currentStreakCount = gameState.currentStreakCount
                )

                // Display the Level
                Text(
                    text = "Level: ${viewModel.questionLevel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                EmojiChainDisplay(emojiChain = gameState.emojiChain)

                ChoiceButtons(
                    choices = gameState.choices,
                    correctAnswerEmoji = gameState.correctAnswerEmoji,
                    isCorrectAnswer = gameState.isCorrectAnswer,
                    onChoiceSelected = { choice -> viewModel.handleChoice(choice) }
                )

                when (gameState.gameResult) {
                    GameResult.InProgress -> {
                        // No start button here anymore
                    }
                    GameResult.Won -> {
                        YouWonDialog(gameState = gameState, onPlayAgain = { viewModel.startGame(); /* No navigation here*/ })
                    }
                    is GameResult.Lost -> {
                        YouLostDialog( // Use the generic GameOverDialog
                            reason = (gameState.gameResult as GameResult.Lost).reason,
                            gameState = gameState,
                            onPlayAgain = { viewModel.startGame() } // No navigation here
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

@Composable
fun PreGameContent(
    gameModeName: String,
    gameDescription: String,
    highScore: Int,
    onStartGame: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp) // Add some padding
    ) {
        Text(
            gameModeName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            gameDescription,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            "High Score: $highScore",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        StyledActionButton(text = "Start Game") {
            onStartGame()
        }
    }
}