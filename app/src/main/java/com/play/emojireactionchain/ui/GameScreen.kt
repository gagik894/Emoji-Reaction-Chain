package com.play.emojireactionchain.ui

import androidx.compose.animation.core.* // Import animation related
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.GameState
import com.play.emojireactionchain.model.LossReason
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.SoundManager
import com.play.emojireactionchain.viewModel.NormalGameViewModel
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
fun GameHeader() {
    Text(
        "Emoji Reaction Chain",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun Scoreboard(score: Int, highScore: Int, lives: Int, currentStreakCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                "Score: $score",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Highest Score: $highScore",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            if (currentStreakCount > 0) {
                Text(
                    "Streak: $currentStreakCount 🔥",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Red.copy(alpha = 0.9f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                for (i in 1..lives) {
                    Text(
                        text = "❤️",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                for (i in lives + 1..3) {
                    Text(
                        text = "🤍",
                        fontSize = 24.sp,
                        color = Color.LightGray.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionProgress(questionNumber: Int, totalQuestions: Int) {
    Text(
        "Question: $questionNumber / $totalQuestions",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 24.dp)
    )
}

@Composable
fun EmojiChainDisplay(emojiChain: List<String>) {
    Row(horizontalArrangement = Arrangement.Center) {
        emojiChain.forEach { emoji ->
            Text(
                text = emoji,
                fontSize = 40.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun ChoiceButtons(
    choices: List<String>,
    correctAnswerEmoji: String,
    isCorrectAnswer: Boolean?,
    onChoiceSelected: (String) -> Unit
) {
    val density = LocalDensity.current

    val buttonStates = remember {
        mutableStateMapOf<String, ButtonState>().apply {
            choices.forEach { put(it, ButtonState()) }
        }
    }

    // Reset button states when isCorrectAnswer changes (new question)
    LaunchedEffect(isCorrectAnswer) {
        if (isCorrectAnswer == null) { // Only reset when transitioning to a new question
            choices.forEach { choice ->
                buttonStates[choice] = ButtonState() // Reset to default state
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        choices.forEach { choiceEmoji ->
            val buttonState = buttonStates[choiceEmoji] ?: ButtonState() // SAFE access with default

            val isChosen = buttonState.isChosen
            val isCorrect = choiceEmoji == correctAnswerEmoji // Correct regardless of whether it's chosen
            val isIncorrectlyChosen = isCorrectAnswer == false && isChosen // Incorrect AND chosen


            val backgroundColor = when {
                buttonState.animatedColor.value != null -> buttonState.animatedColor.value!!
                isCorrect && isCorrectAnswer != null -> Color.Green.copy(alpha = 0.7f) // Always green if correct, after answer revealed
                isIncorrectlyChosen -> Color.Red.copy(alpha = 0.7f) // Red only if incorrectly *chosen*
                else -> MaterialTheme.colorScheme.primary
            }


            Button(
                onClick = {
                    if (isCorrectAnswer == null) {
                        onChoiceSelected(choiceEmoji)
                        buttonStates[choiceEmoji] = buttonState.copy(isChosen = true)
                    }
                },
                enabled = isCorrectAnswer == null, // Disable buttons after a choice is made
                colors = ButtonDefaults.buttonColors(
                    containerColor = backgroundColor,
                    disabledContainerColor = backgroundColor // Use the same color when disabled!
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = buttonState.scale.value
                        scaleY = buttonState.scale.value
                        translationX = buttonState.shakeOffset.value
                    }
            ) {
                Text(text = choiceEmoji, fontSize = 36.sp, style = MaterialTheme.typography.bodyMedium)
            }

            LaunchedEffect(isCorrectAnswer, isChosen) {
                if (isChosen && isCorrectAnswer != null) { // Only animate after a choice is made
                    if (isCorrect) {
                        buttonStates[choiceEmoji]?.let { currentState ->
                            buttonStates[choiceEmoji] =
                                currentState.copy(animatedColor = mutableStateOf(Color.Green.copy(alpha = 0.7f)))
                            currentState.scale.animateTo(1.2f, tween(100))
                            currentState.scale.animateTo(1f, tween(150))
                            buttonStates[choiceEmoji] =
                                currentState.copy(animatedColor = mutableStateOf(null)) // Reset after animation
                        }
                    } else if (isIncorrectlyChosen) { //incorrect and chosen
                        buttonStates[choiceEmoji]?.let { currentState ->
                            buttonStates[choiceEmoji] =
                                currentState.copy(animatedColor = mutableStateOf(Color.Red.copy(alpha = 0.7f)))
                            density.run {
                                currentState.shakeOffset.animateTo(
                                    20.dp.toPx(),
                                    repeatable(5, tween(50, easing = FastOutLinearInEasing), RepeatMode.Reverse)
                                )
                                currentState.shakeOffset.animateTo(0f, tween(100))
                            }
                            buttonStates[choiceEmoji] =
                                currentState.copy(animatedColor = mutableStateOf(null)) // Reset after animation
                        }
                    }
                }
            }
        }
    }
}


data class ButtonState(
    val isChosen: Boolean = false,
    val scale: Animatable<Float, AnimationVector1D> = Animatable(1f),
    val shakeOffset: Animatable<Float, AnimationVector1D> = Animatable(0f),
    val animatedColor: MutableState<Color?> = mutableStateOf(null)
)

//Optional GameScreen Layout
@Composable
fun GameScreenLayout(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        content()
    }
}


// Custom ViewModel Factory
class NormalGameViewModelFactory(
    private val soundManager: SoundManager,
    private val highScoreManager: HighScoreManager
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NormalGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NormalGameViewModel(soundManager, highScoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


@Composable
fun NormalModeScreen() { // Removed default viewModel parameter
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val highScoreManager = remember { HighScoreManager(context) }

    // Use the custom factory
    val viewModel: NormalGameViewModel = viewModel(
        factory = NormalGameViewModelFactory(soundManager, highScoreManager)
    )

    val gameState by viewModel.gameState.collectAsState()

    var showTimeBonusAnimation by remember { mutableStateOf(false) }
    var currentBonusPointsForAnimation by remember { mutableStateOf(0) }

    LaunchedEffect(gameState.currentTimeBonus) {
        if (gameState.currentTimeBonus > 0) {
            showTimeBonusAnimation = true
            currentBonusPointsForAnimation = gameState.currentTimeBonus
            delay(1500)
            showTimeBonusAnimation = false
        }
    }


    Box {
        GameScreenLayout {
            GameHeader()
            Scoreboard(
                score = gameState.score,
                highScore = gameState.highScore,
                lives = gameState.lives,
                currentStreakCount = gameState.currentStreakCount
            )
            QuestionProgress(
                questionNumber = gameState.questionNumber,
                totalQuestions = gameState.totalQuestions
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
                    // Show game UI (choices, etc.)
                    if (gameState.questionNumber == 0) {
                        StyledActionButton(text = "Start Game") {
                            viewModel.startGame(GameMode.NORMAL)
                        }
                    }
                }

                GameResult.Won -> {
                    YouWonDialog(gameState = gameState, onPlayAgain = { viewModel.resetGame() })
                }

                is GameResult.Lost -> { // Note the 'is' check for the sealed class
                    YouLostDialog(
                        gameState = gameState,
                        onPlayAgain = { viewModel.resetGame(); viewModel.startGame(GameMode.TIMED) },
                        reason = (gameState.gameResult as GameResult.Lost).reason
                    )
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
fun StyledAlertDialog(
    title: String,
    message: @Composable () -> Unit, // Changed to a Composable
    confirmButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null // Make onDismiss optional and nullable
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() }, // Use safe call
        title = {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error // Or any color you prefer
            )
        },
        text = message,  // Use the Composable message
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmButtonText, style = MaterialTheme.typography.bodyMedium)
            }
        },
        dismissButton = {
            if (onDismiss != null) { // Only show dismiss button if onDismiss is provided
                TextButton(onClick = onDismiss) {
                    Text("Dismiss", style = MaterialTheme.typography.bodyMedium) // Or your preferred text
                }
            }
        }
    )
}

@Composable
fun YouWonDialog(gameState: GameState, onPlayAgain: () -> Unit) {
    StyledAlertDialog(
        title = "Congratulations!",
        message = {
            Column {
                Text(
                    "You completed all questions!",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Final Score: ${gameState.score}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "High Score: ${gameState.highScore}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButtonText = "Play Again",
        onConfirm = onPlayAgain,
        onDismiss = null // No dismiss button
    )
}

@Composable
fun YouLostDialog(gameState: GameState, onPlayAgain: () -> Unit, reason: LossReason) {
    val message = when (reason) {
        LossReason.OutOfLives -> "You ran out of lives!"
        LossReason.TimeOut -> "Time's Up!"
    }

    StyledAlertDialog(
        title = "Game Over!",
        message = { // Pass a Composable for the message content
            Column {
                Text(
                    message,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Final Score: ${gameState.score}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "High Score: ${gameState.highScore}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButtonText = "Play Again",
        onConfirm = onPlayAgain,
        onDismiss = null // No dismiss button
    )
}

@Composable
fun TimeUpDialog(gameState: com.play.emojireactionchain.model.GameState, onPlayAgain: () -> Unit) {

    StyledAlertDialog(
        title = "Time's Up!",
        message = { // Pass a Composable for the message content
            Column {
                Text(
                    "Your final score:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Final Score: ${gameState.score}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "High Score: ${gameState.highScore}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButtonText = "Play Again",
        onConfirm = onPlayAgain,
        onDismiss = null // No dismiss button
    )
}