package com.play.emojireactionchain.ui

import androidx.compose.animation.core.* // Import animation related
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.* // Import remember, mutableStateOf, LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer // Import graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.play.emojireactionchain.model.GameState
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.SoundManager
import com.play.emojireactionchain.viewModel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun TimeBonusAnimation(bonusPoints: Int) {
    if (bonusPoints <= 0) return // Don't show animation if no bonus points

    val translateYPx = remember { Animatable(0f) } // Animate Float in pixels
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val density = LocalDensity.current // Get density for DP to PX conversion

    LaunchedEffect(bonusPoints) { // Animate when bonusPoints changes (on new bonus)
        translateYPx.animateTo(
            -150f, // Throw upwards in pixels (adjust value as needed)
            animationSpec = tween(durationMillis = 300)
        )
        scale.animateTo(
            1.3f, // Scale up slightly less
            animationSpec = tween(durationMillis = 200)
        )
        alpha.animateTo(
            1f, // Fade in
            animationSpec = tween(durationMillis = 200)
        )
        translateYPx.animateTo(
            0f, // Fall back down to center
            animationSpec = tween(durationMillis = 400)
        )
        scale.animateTo(
            1f, // Scale back to normal
            animationSpec = tween(durationMillis = 300)
        )
        alpha.animateTo(
            0f, // Fade out
            animationSpec = tween(durationMillis = 300)
        )
    }

    Box( // Wrap Text in Box to allow central positioning
        modifier = Modifier.fillMaxSize(), // Fill the available space
        contentAlignment = Alignment.Center // Center content in the Box
    ) {
        Text(
            text = "+$bonusPoints Time Bonus!",
            style = MaterialTheme.typography.titleLarge, // Use titleLarge for more impact
            color = Color.Green.copy(alpha = 0.95f), // Slightly brighter green
            fontSize = 32.sp, // Slightly larger font
            modifier = Modifier
                .offset(y = with(density) { translateYPx.value.toDp() }) // Convert float pixels to Dp for offset
                .graphicsLayer(scaleX = scale.value, scaleY = scale.value, alpha = alpha.value)
        )
    }
}

@Composable
fun NormalModeScreen() {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val highScoreManager = remember { HighScoreManager(context) } // Instantiate HighScoreManager
    val gameViewModel: GameViewModel = viewModel {
        GameViewModel(
            soundManager = soundManager,
            highScoreManager = highScoreManager
        ) // Pass HighScoreManager to ViewModel
    }
    val gameState by gameViewModel.gameState.collectAsState()


    var showTimeBonusAnimation by remember { mutableStateOf(false) }
    var currentBonusPointsForAnimation by remember { mutableStateOf(0) } // To pass bonus points to animation

    LaunchedEffect(gameState.currentTimeBonus) {
        if (gameState.currentTimeBonus > 0) {
            showTimeBonusAnimation = true
            currentBonusPointsForAnimation = gameState.currentTimeBonus
        }
    }

    // LaunchedEffect to reset showTimeBonusAnimation after animation duration
    LaunchedEffect(showTimeBonusAnimation) {
        if (showTimeBonusAnimation) {
            delay(1500) // Duration to show animation (adjust as needed)
            showTimeBonusAnimation = false // Hide animation after delay
        }
    }
    // Animation States for button feedback
    val buttonScale = remember { Animatable(1f) } // Scale animation for button pulse/pop
    val buttonShakeOffset = remember { Animatable(0f) } // Offset animation for button shake
    val animatedButtonColor =
        remember { mutableStateOf<Color?>(null) } // Track button color animation
    val density = LocalDensity.current
    // LaunchedEffect to trigger animations based on gameState updates
    LaunchedEffect(gameState.isCorrectAnswer) {
        gameState.isCorrectAnswer?.let { isCorrect ->
            if (isCorrect) {
                animatedButtonColor.value =
                    Color.Green.copy(alpha = 0.7f) // Set to green for correct
                buttonScale.animateTo(1.2f, animationSpec = tween(durationMillis = 200)) // Scale up
                buttonScale.animateTo(
                    1f,
                    animationSpec = tween(durationMillis = 150)
                ) // Scale back down
                animatedButtonColor.value = null // Reset animated color
            } else {
                animatedButtonColor.value = Color.Red.copy(alpha = 0.7f)
                // CORRECTED LINE: Use LocalDensity.current.run { ... } to provide Density context
                density.run {
                    buttonShakeOffset.animateTo(
                        20.dp.toPx(),
                        animationSpec = repeatable(
                            5,
                            animation = tween(durationMillis = 50, easing = FastOutLinearInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    buttonShakeOffset.animateTo(
                        0.dp.toPx(),
                        animationSpec = tween(durationMillis = 100)
                    )
                }
                animatedButtonColor.value = null
            }
        }
    }


    Box() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                "Emoji Reaction Chain",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge, // Use headlineLarge for bigger size
                color = MaterialTheme.colorScheme.primary, // Use primary color for title
                modifier = Modifier.padding(bottom = 12.dp) // Slightly more bottom padding
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row( // Row for Score, High Score, and Lives, space between
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Keep SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) { // Column for Score and Lives - Align Start
                    Text(
                        "Score: ${gameState.score}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        // High Score - Keep on the right
                        "Highest Score: ${gameState.highScore}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) { // Align Lives to the right
                    if (gameState.currentStreakCount > 0) { // Conditionally show Streak Count if it's > 0
                        Text(
                            "Streak: ${gameState.currentStreakCount} 🔥", // Added fire emoji for streak
                            style = MaterialTheme.typography.titleMedium, // Use titleMedium for streak count
                            color = Color.Red.copy(alpha = 0.9f) // Yellow/gold color for streak
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row { // Row for Lives (Keep Lives on the right side)
                        for (i in 1..gameState.lives) {
                            Text(
                                text = "❤️", // Heart emoji
                                fontSize = 24.sp, // Heart size
                                color = MaterialTheme.colorScheme.error // Hearts in error color (red)
                            )
                        }
                        for (i in gameState.lives + 1..3) { // Optional: Grey hearts for lost lives (if starting with 3 lives)
                            Text(
                                text = "🤍", // White heart emoji for lost lives (optional)
                                fontSize = 24.sp,
                                color = Color.LightGray.copy(alpha = 0.6f) // Greyed out hearts
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Increased spacing below score/high score

            // Question Progress Indicator
            Text(
                "Question: ${gameState.questionNumber} / ${gameState.totalQuestions}",
                style = MaterialTheme.typography.titleMedium, // Use titleMedium for question progress
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // Muted color - less prominent
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp) // Increased bottom padding to separate from emojis
            )
            Spacer(modifier = Modifier.height(8.dp)) // Reduced spacer height - already has padding

            // Emoji Chain Display
            // Emoji Chain Display
            Row(horizontalArrangement = Arrangement.Center) {
                gameState.emojiChain.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 40.sp,
                        modifier = Modifier.padding(horizontal = 8.dp) // Add horizontal padding between emojis
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Choice Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gameState.choices.forEach { choiceEmoji ->
                    val isCorrect =
                        gameState.isCorrectAnswer == true && choiceEmoji == gameState.correctAnswerEmoji
                    val isIncorrect =
                        gameState.isCorrectAnswer == false && choiceEmoji == gameState.correctAnswerEmoji

                    Button(
                        onClick = { gameViewModel.handleChoice(choiceEmoji) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = animatedButtonColor.value
                                ?: when { // Use animated color, or default based on correctness
                                    isCorrect -> Color.Green.copy(alpha = 0.7f) // Still set correct/incorrect colors for default state too (though animation will override briefly)
                                    isIncorrect -> Color.Red.copy(alpha = 0.7f)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { // Apply graphicsLayer for animations
                                scaleX = buttonScale.value // Apply scale animation
                                scaleY = buttonScale.value
                                translationX =
                                    buttonShakeOffset.value // Apply shake animation (horizontal translation)
                            }
                    ) {
                        Text(
                            text = choiceEmoji,
                            fontSize = 36.sp,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            if (gameState.isGameOver) {
                if (gameState.isCorrectAnswer == true) { // Check if game over is due to winning (all questions completed)
                    YouWonDialog(
                        gameState = gameState,
                        onPlayAgain = { gameViewModel.resetGame() }) // Show YouWonDialog
                } else { // Game over is due to losing lives
                    YouLostDialog(
                        gameState = gameState,
                        onPlayAgain = { gameViewModel.resetGame() }) // Show YouLostDialog
                }
            } else { // If game is NOT over, show "Start Game" button (or nothing if game in progress)
                if (gameState.questionNumber == 0) {
                    StyledActionButton(text = "Start Game") { gameViewModel.startGame() }
                }
            }
        }
        if (showTimeBonusAnimation) {
            TimeBonusAnimation(bonusPoints = currentBonusPointsForAnimation)
        }
    }
}

@Composable
fun StyledActionButton(text: String, onClick: () -> Unit) {
    var buttonScale by remember { mutableFloatStateOf(1f) }
    val scaleAnimation by animateFloatAsState(
        targetValue = buttonScale, animationSpec = spring(),
        label = ""
    )

    Button(
        onClick = {
            buttonScale = 0.95f // Slightly less scaling for action buttons
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth(0.6f) // Make buttons slightly narrower
            .padding(vertical = 8.dp) // Add vertical padding
            .graphicsLayer(scaleX = scaleAnimation, scaleY = scaleAnimation),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium, // Use titleMedium for button text
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun YouWonDialog(gameState: GameState, onPlayAgain: () -> Unit) {
    androidx.compose.material3.AlertDialog( // Use Material 3 AlertDialog
        onDismissRequest = { /* Prevent dismiss on outside click if needed */ }, // Or handle dismiss if you want
        title = {
            Text(
                "Congratulations!",
                style = MaterialTheme.typography.headlineSmall
            ) // Celebratory title
        },
        text = {
            Column {
                Text(
                    "You completed all questions!",
                    style = MaterialTheme.typography.bodyLarge
                ) // Win message
                Spacer(modifier = Modifier.height(8.dp))
                Text("Final Score: ${gameState.score}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "High Score: ${gameState.highScore}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onPlayAgain) { // "Play Again" button
                Text("Play Again", style = MaterialTheme.typography.bodyMedium)
            }
        },
        dismissButton = null // No dismiss button in this case, force user to choose "Play Again"
    )
}

@Composable
fun YouLostDialog(gameState: GameState, onPlayAgain: () -> Unit) {
    androidx.compose.material3.AlertDialog( // Use Material 3 AlertDialog
        onDismissRequest = { /* Prevent dismiss on outside click if needed */ }, // Or handle dismiss if needed
        title = {
            Text(
                "Game Over!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            ) // Game Over title, error color
        },
        text = {
            Column {
                Text(
                    "You ran out of lives!",
                    style = MaterialTheme.typography.bodyLarge
                ) // Losing message
                Spacer(modifier = Modifier.height(8.dp))
                Text("Final Score: ${gameState.score}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "High Score: ${gameState.highScore}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onPlayAgain) { // "Play Again" button
                Text("Play Again", style = MaterialTheme.typography.bodyMedium)
            }
        },
        dismissButton = null // No dismiss button
    )
}