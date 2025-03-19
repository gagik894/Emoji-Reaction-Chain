package com.play.emojireactionchain.ui

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun GameHeader(showBack: Boolean = true, onBack: () -> Unit = {}) { // Add parameters
    Column {
        BannerAd(adUnitId = "ca-app-pub-3940256099942544/9214589741")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically, // Vertically center content
            horizontalArrangement = if (showBack) Arrangement.SpaceBetween else Arrangement.Center // Conditional arrangement
        ) {
            if (showBack) { // Conditionally show the back button
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .clickable(onClick = onBack) // Make it clickable
                        .padding(8.dp), // Add some padding
                    tint = MaterialTheme.colorScheme.primary // Use a consistent color
                )
            }
            Text(
                "Emoji Reaction Chain",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )

            // Empty composable on the right when the back button is present, for symmetry.
            if (showBack) {
                Spacer(modifier = Modifier.width(20.dp))
            }
        }
    }
}

@Composable
fun Scoreboard(score: Int, highScore: Int, lives: Int?, currentStreakCount: Int) {
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
            lives?.let {
                Row {
                    for (i in 1..it) {
                        Text(
                            text = "❤️",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    for (i in it + 1..3) {
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
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) { // Use BoxWithConstraints
        this.maxWidth
        var fontSize = 40.sp
        var readyToDraw by remember { mutableStateOf(false) }

        // Use onTextLayout to adjust font size *before* drawing
        val textStyle = remember { mutableStateOf(TextStyle(fontSize = fontSize)) }

        Text(
            text = emojiChain.joinToString(" "), // Join to a single string
            fontSize = fontSize,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = textStyle.value,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis, // Required for onTextLayout
            onTextLayout = { textLayoutResult ->
                if (textLayoutResult.didOverflowWidth) {
                    fontSize *= 0.9f // Reduce font size by 10%
                    textStyle.value = textStyle.value.copy(fontSize = fontSize)
                } else {
                    readyToDraw = true
                }
            }
        )
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
            val isCorrect =
                choiceEmoji == correctAnswerEmoji // Correct regardless of whether it's chosen
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
                Text(
                    text = choiceEmoji,
                    fontSize = 36.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            LaunchedEffect(isCorrectAnswer, isChosen) {
                if (isChosen && isCorrectAnswer != null) { // Only animate after a choice is made
                    if (isCorrect) {
                        buttonStates[choiceEmoji]?.let { currentState ->
                            buttonStates[choiceEmoji] =
                                currentState.copy(
                                    animatedColor = mutableStateOf(
                                        Color.Green.copy(
                                            alpha = 0.7f
                                        )
                                    )
                                )
                            currentState.scale.animateTo(1.2f, tween(100))
                            currentState.scale.animateTo(1f, tween(150))
                            buttonStates[choiceEmoji] =
                                currentState.copy(animatedColor = mutableStateOf(null)) // Reset after animation
                        }
                    } else if (isIncorrectlyChosen) { //incorrect and chosen
                        buttonStates[choiceEmoji]?.let { currentState ->
                            buttonStates[choiceEmoji] =
                                currentState.copy(
                                    animatedColor = mutableStateOf(
                                        Color.Red.copy(
                                            alpha = 0.7f
                                        )
                                    )
                                )
                            density.run {
                                currentState.shakeOffset.animateTo(
                                    20.dp.toPx(),
                                    repeatable(
                                        5,
                                        tween(50, easing = FastOutLinearInEasing),
                                        RepeatMode.Reverse
                                    )
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
            .padding(horizontal = 16.dp),
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
fun NormalModeScreen(
    onNavigateToStart: () -> Unit
) { // Removed default viewModel parameter
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
            GameHeader(
                onBack = onNavigateToStart,
            )
            if (gameState.questionNumber == 0) {
                PreGameContent(
                    gameModeName = "Normal Mode",
                    gameDescription = "Answer questions to increase your score!",
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
    message: @Composable () -> Unit,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    isError: Boolean = true // New parameter with default to preserve existing behavior
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        },
        text = message,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmButtonText, style = MaterialTheme.typography.bodyMedium)
            }
        },
        dismissButton = {
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text(
                        "Go Back",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    )
}

@Composable
fun YouWonDialog(
    gameState: GameState,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(true) }
    if (!showDialog) return
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
        onDismiss = {
            showDialog = false
            onBack()
        },
        isError = false
    )
}

@Composable
fun YouLostDialog(
    gameState: GameState,
    onPlayAgain: () -> Unit,
    reason: LossReason,
    onWatchAd: (() -> Unit)? = null,
    isLoading: Boolean = false,
    adWatched: Boolean = false,
    onBack: () -> Unit = {}
) {
    val message = when (reason) {
        LossReason.OutOfLives -> "You ran out of lives!"
        LossReason.TimeOut -> "Time's Up!"
    }
    var showDialog by remember { mutableStateOf(true) }
    if (!showDialog) return
    StyledAlertDialog(
        title = "Game Over!",
        message = {
            Column {
                Text(message, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Final Score: ${gameState.score}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "High Score: ${gameState.highScore}",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Show ad button if available
                if (onWatchAd != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        enabled = !isLoading,
                        onClick = onWatchAd,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when {
                            isLoading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Loading Ad...")
                            }

                            adWatched -> {
                                Text(
                                    "Continue Game",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            else -> {
                                Text(
                                    "Watch Ad to Continue Game",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButtonText = "Play Again",
        onConfirm = onPlayAgain,
        onDismiss = {
            showDialog = false
            onBack()
        }
    )
}


@Composable
fun GameResultHandler(
    gameState: GameState,
    onStartGame: () -> Unit,
    onHandleAdReward: () -> Unit,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val rewardedAdState = rememberRewardedAd("ca-app-pub-3940256099942544/5224354917")
    val interstitialAdState = rememberInterstitialAd("ca-app-pub-3940256099942544/1033173712")
    var isLoading by remember { mutableStateOf(false) }

    // Track ad states
    var adWatched by remember { mutableStateOf(false) }
    var previousGameResult by remember { mutableStateOf<GameResult?>(null) }
    var interstitialShown by remember { mutableStateOf(false) }

    // Reset adWatched when game result changes
    LaunchedEffect(gameState.gameResult) {
        if (gameState.gameResult != previousGameResult) {
            adWatched = false
            previousGameResult = gameState.gameResult

            // Check if we should show an interstitial ad (game completion)
            if ((gameState.gameResult is GameResult.Won || gameState.gameResult is GameResult.Lost)
                && AdManager.shouldShowAd() && !interstitialShown
            ) {
                interstitialShown = true
                showInterstitialAd(
                    interstitialAd = interstitialAdState.interstitialAd,
                    activity = activity,
                    onAdClosed = {
                        interstitialAdState.loadAd() // Reload for next time
                    }
                )
            }
        }
    }

    // Rest of your GameResultHandler implementation...
    when (val result = gameState.gameResult) {
        GameResult.InProgress -> {
            // No dialog when game is in progress
        }

        GameResult.Won -> {
            // Track completed game for ad logic
            LaunchedEffect(Unit) {
                AdManager.incrementGamePlayCount()
            }

            YouWonDialog(
                gameState = gameState,
                onPlayAgain = { onStartGame() },
                onBack = {
                    AdManager.markShowAdOnHomeReturn()
                    onBack()
                }
            )
        }

        is GameResult.Lost -> {
            // Track completed game for ad logic
            LaunchedEffect(Unit) {
                AdManager.incrementGamePlayCount()
            }

            YouLostDialog(
                reason = result.reason,
                gameState = gameState,
                onPlayAgain = { onStartGame() },
                onBack = {
                    AdManager.markShowAdOnHomeReturn()
                    onBack()
                },
            )
        }

        is GameResult.AdContinueOffered -> {
            YouLostDialog(
                gameState = gameState,
                onPlayAgain = { onStartGame() },
                reason = (result.underlyingResult as GameResult.Lost).reason,
                onWatchAd = {
                    if (adWatched) {
                        // If ad was already watched, directly call the callback
                        onHandleAdReward()
                    } else {
                        isLoading = true
                        showRewardedAd(
                            rewardedAd = rewardedAdState.rewardedAd,
                            activity = activity,
                            onUserEarnedReward = {
                                adWatched = true
                                isLoading = false
                            },
                            onAdClosed = {
                                isLoading = false
                                rewardedAdState.loadAd() // Reload for next time
                            }
                        )
                    }
                },
                isLoading = isLoading,
                adWatched = adWatched,
                onBack = {
                    AdManager.markShowAdOnHomeReturn()
                    onBack()
                },
            )
        }
    }
}