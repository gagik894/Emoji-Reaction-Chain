package com.play.emojireactionchain.ui

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.GameState
import com.play.emojireactionchain.model.LossReason
import com.play.emojireactionchain.ui.theme.*
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
fun GameHeader(showBack: Boolean = true, onBack: () -> Unit = {}) {
    Column {
        BannerAd(adUnitId = "ca-app-pub-2523891738770793/9481725035")
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBack) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White, CircleShape)
                            .shadow(2.dp, CircleShape)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp),
                            tint = PrimarySoft
                        )
                    }
                }

                Text(
                    text = "Emoji Chain",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = PrimarySoft,
                    modifier = Modifier.weight(1f),
                )

                if (showBack) {
                    Spacer(modifier = Modifier.width(44.dp))
                }
            }
        }
    }
}

@Composable
fun Scoreboard(score: Int, highScore: Int, lives: Int?, currentStreakCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimarySoft
                )
            }

            if (currentStreakCount > 0) {
                Box(
                    modifier = Modifier
                        .background(SecondarySoft.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$currentStreakCount 🔥",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SecondarySoft
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                lives?.let {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) { index ->
                            Text(
                                text = if (index < it) "❤️" else "🖤",
                                fontSize = 20.sp,
                                modifier = Modifier.graphicsLayer {
                                    alpha = if (index < it) 1f else 0.3f
                                }
                            )
                        }
                    }
                }
                Text(
                    text = "BEST: $highScore",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
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
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        var fontSize by remember { mutableStateOf(40.sp) }

        // Use onTextLayout to adjust font size *before* drawing
        val textStyle = remember(fontSize) { TextStyle(fontSize = fontSize) }

        Text(
            text = emojiChain.joinToString(" "), // Join to a single string
            modifier = Modifier.padding(horizontal = 8.dp),
            style = textStyle,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis, // Required for onTextLayout
            onTextLayout = { textLayoutResult ->
                if (textLayoutResult.didOverflowWidth) {
                    fontSize *= 0.9f // Reduce font size by 10%
                }
            }
        )
    }
}

@Composable
fun AnimatedChoiceButton(
    choiceEmoji: String,
    isCorrectAnswer: Boolean?,
    correctAnswerEmoji: String,
    onChoiceSelected: (String) -> Unit
) {
    val density = LocalDensity.current
    var isChosen by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }
    val shakeOffset = remember { Animatable(0f) }
    val animatedColorState = remember { mutableStateOf<Color?>(null) }

    LaunchedEffect(isCorrectAnswer) {
        if (isCorrectAnswer == null) {
            isChosen = false
            animatedColorState.value = null
        }
    }

    val isCorrect = choiceEmoji == correctAnswerEmoji
    val isIncorrectlyChosen = isCorrectAnswer == false && isChosen

    val backgroundColor = when {
        animatedColorState.value != null -> animatedColorState.value!!
        isCorrect && isCorrectAnswer != null -> SuccessGreen.copy(alpha = 0.9f)
        isIncorrectlyChosen -> ErrorRed.copy(alpha = 0.9f)
        else -> PrimarySoft
    }

    LaunchedEffect(isCorrectAnswer, isChosen) {
        if (isChosen && isCorrectAnswer != null) {
            if (isCorrect) {
                animatedColorState.value = Color.Green.copy(alpha = 0.7f)
                scale.animateTo(1.2f, tween(100))
                scale.animateTo(1f, tween(150))
                animatedColorState.value = null
            } else if (isIncorrectlyChosen) {
                animatedColorState.value = Color.Red.copy(alpha = 0.7f)
                density.run {
                    shakeOffset.animateTo(
                        20.dp.toPx(),
                        repeatable(5, tween(50, easing = FastOutLinearInEasing), RepeatMode.Reverse)
                    )
                    shakeOffset.animateTo(0f, tween(100))
                }
                animatedColorState.value = null
            }
        }
    }

    Button(
        onClick = {
            isChosen = true
            onChoiceSelected(choiceEmoji)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(if (isChosen) 0.dp else 6.dp, RoundedCornerShape(32.dp))
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                translationX = shakeOffset.value
            },
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor, contentColor = Color.White),
        elevation = null
    ) {
        Text(text = choiceEmoji, fontSize = 32.sp)
    }
}

@Composable
fun ChoiceButtons(
    choices: List<String>,
    correctAnswerEmoji: String,
    isCorrectAnswer: Boolean?,
    onChoiceSelected: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        choices.forEach { choiceEmoji ->
            AnimatedChoiceButton(
                choiceEmoji = choiceEmoji,
                isCorrectAnswer = isCorrectAnswer,
                correctAnswerEmoji = correctAnswerEmoji,
                onChoiceSelected = onChoiceSelected
            )
        }
    }
}

//Optional GameScreen Layout
@Composable
fun GameScreenLayout(content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
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

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    // Use the custom factory
    val viewModel: NormalGameViewModel = viewModel(
        factory = NormalGameViewModelFactory(soundManager, highScoreManager)
    )

    val gameState by viewModel.gameState.collectAsState()

    var showTimeBonusAnimation by remember { mutableStateOf(false) }
    var currentBonusPointsForAnimation by remember { mutableIntStateOf(0) }

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
    val buttonScaleState = remember { mutableFloatStateOf(1f) }
    val scaleAnimation by animateFloatAsState(
        targetValue = buttonScaleState.floatValue,
        animationSpec = spring(),
        label = "buttonScale"
    )

    LaunchedEffect(buttonScaleState.floatValue) {
        if (buttonScaleState.floatValue < 1f) {
            delay(100)
            buttonScaleState.floatValue = 1f
        }
    }

    Button(
        onClick = {
            buttonScaleState.floatValue = 0.95f
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
    dismissButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    isError: Boolean = true
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        },
        text = message,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text(dismissButtonText)
                }
            }
        }
    )
}

@Composable
fun GameEndDialog(
    isWon: Boolean,
    reason: LossReason? = null,
    gameState: GameState,
    onPlayAgain: () -> Unit,
    onWatchAd: (() -> Unit)? = null,
    isLoading: Boolean = false,
    adWatched: Boolean = false,
    onBack: () -> Unit = {}
) {
    val title = if (isWon) "Congratulations!" else "Game Over!"
    val mainMessage = if (isWon) "You completed all questions!" else {
        when (reason) {
            LossReason.OutOfLives -> "You ran out of lives!"
            LossReason.TimeOut -> "Time's Up!"
            null -> "You Lost!"
        }
    }

    StyledAlertDialog(
        title = title,
        message = {
            Column {
                Text(mainMessage, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Final Score: ${gameState.score}", style = MaterialTheme.typography.bodyMedium)
                Text("High Score: ${gameState.highScore}", style = MaterialTheme.typography.bodyMedium)

                if (onWatchAd != null && !isWon) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        enabled = !isLoading,
                        onClick = onWatchAd,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Loading Ad...")
                            } else if (adWatched) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Continue", modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Continue Game")
                            } else {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_media_play),
                                    contentDescription = "Watch Ad",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Watch Ad to Continue")
                            }
                        }
                    }
                }
            }
        },
        confirmButtonText = "Play Again",
        onConfirm = onPlayAgain,
        onDismiss = onBack,
        isError = !isWon
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
    val rewardedAdState = rememberRewardedAd("ca-app-pub-2523891738770793/5350908330")
    val interstitialAdState = rememberInterstitialAd("ca-app-pub-2523891738770793/2652053805")
    val isLoadingState = remember { mutableStateOf(false) }

    // Track ad states
    var adWatched by remember { mutableStateOf(false) }
    var previousGameResult by remember { mutableStateOf<GameResult?>(null) }

    // Function to show interstitial ad and then perform an action
    val showInterstitialAndThen: (action: () -> Unit) -> Unit = { action ->
        if (AdManager.shouldShowAd()) {
            showInterstitialAd(
                interstitialAd = interstitialAdState.interstitialAd,
                activity = activity,
                onAdClosed = {
                    interstitialAdState.loadAd()
                    action()
                }
            )
        } else {
            action()
        }
    }

    // Reset adWatched when game result changes
    LaunchedEffect(gameState.gameResult) {
        if (gameState.gameResult != previousGameResult) {
            adWatched = false
            previousGameResult = gameState.gameResult
            println("Game result changed to ${gameState.gameResult} ${AdManager.shouldShowAd()}")
            // Check if we should show an interstitial ad (game completion)
            if ((gameState.gameResult is GameResult.Won || gameState.gameResult is GameResult.Lost)
                && AdManager.shouldShowAd()
            ) {
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
            LaunchedEffect(Unit) { AdManager.incrementGamePlayCount() }
            GameEndDialog(
                isWon = true,
                gameState = gameState,
                onPlayAgain = { showInterstitialAndThen { onStartGame() } },
                onBack = { AdManager.markShowAdOnHomeReturn(); onBack() }
            )
        }

        is GameResult.Lost -> {
            LaunchedEffect(Unit) { AdManager.incrementGamePlayCount() }
            GameEndDialog(
                isWon = false,
                reason = result.reason,
                gameState = gameState,
                onPlayAgain = { showInterstitialAndThen { onStartGame() } },
                onBack = { AdManager.markShowAdOnHomeReturn(); onBack() }
            )
        }

        is GameResult.AdContinueOffered -> {
            LaunchedEffect(Unit) { AdManager.incrementGamePlayCount() }
            val underlyingLost = result.underlyingResult as? GameResult.Lost
            GameEndDialog(
                isWon = false,
                reason = underlyingLost?.reason,
                gameState = gameState,
                onPlayAgain = { showInterstitialAndThen { onStartGame() } },
                onWatchAd = {
                    if (adWatched) {
                        onHandleAdReward()
                    } else {
                        isLoadingState.value = true
                        showRewardedAd(
                            rewardedAd = rewardedAdState.rewardedAd,
                            activity = activity,
                            onUserEarnedReward = {
                                adWatched = true
                                isLoadingState.value = false
                            },
                            onAdClosed = {
                                isLoadingState.value = false
                                rewardedAdState.loadAd()
                            }
                        )
                    }
                },
                isLoading = isLoadingState.value,
                adWatched = adWatched,
                onBack = { AdManager.markShowAdOnHomeReturn(); onBack() }
            )
        }
    }
}