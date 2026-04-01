package com.play.emojireactionchain.ui

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
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
    if (bonusPoints <= 0) return

    val translateYPx = remember { Animatable(0f) }
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val density = LocalDensity.current

    LaunchedEffect(bonusPoints) {
        translateYPx.animateTo(-150f, animationSpec = tween(300))
        scale.animateTo(1.3f, animationSpec = tween(200))
        alpha.animateTo(1f, animationSpec = tween(200))
        translateYPx.animateTo(0f, animationSpec = tween(400))
        scale.animateTo(1f, animationSpec = tween(300))
        alpha.animateTo(0f, animationSpec = tween(300))
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+$bonusPoints Time Bonus!",
            style = MaterialTheme.typography.titleLarge,
            color = SuccessGreen,
            fontSize = 32.sp,
            modifier = Modifier
                .offset(y = with(density) { translateYPx.value.toDp() })
                .graphicsLayer(scaleX = scale.value, scaleY = scale.value, alpha = alpha.value)
        )
    }
}

@Composable
fun GameHeader(showBack: Boolean = true, onBack: () -> Unit = {}) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column {
        if (!isLandscape) BannerAd(adUnitId = "ca-app-pub-2523891738770793/9481725035")
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextMain
                    )
                }
            }

            Text(
                text = "Emoji Chain",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                ),
                color = TextMain,
                modifier = Modifier.weight(1f),
            )

            if (showBack) Spacer(modifier = Modifier.width(40.dp))
        }
    }
}

@Composable
fun Scoreboard(score: Int, highScore: Int, lives: Int?, currentStreakCount: Int) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isLandscape) 4.dp else 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Score section
        Column {
            Text(
                text = "SCORE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp
                ),
                color = PrimarySoft
            )
        }

        // Streak indicator
        if (currentStreakCount > 1) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WarningOrange.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "🔥 $currentStreakCount",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = WarningOrange,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Lives/Best section
        Column(horizontalAlignment = Alignment.End) {
            lives?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { index ->
                        Text(
                            text = "❤️",
                            fontSize = 18.sp,
                            modifier = Modifier.graphicsLayer {
                                alpha = if (index < it) 1f else 0.2f
                            }
                        )
                    }
                }
            }
            Text(
                text = "BEST: $highScore",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
        }
    }
}

@Composable
fun QuestionProgress(questionNumber: Int, totalQuestions: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                "$questionNumber / $totalQuestions",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun EmojiChainDisplay(emojiChain: List<String>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            var fontSize by remember { mutableStateOf(48.sp) }
            Text(
                text = emojiChain.joinToString(" "),
                style = TextStyle(fontSize = fontSize, fontWeight = FontWeight.Bold),
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { if (it.didOverflowWidth) fontSize *= 0.9f }
            )
        }
    }
}

@Composable
fun AnimatedChoiceButton(
    choiceEmoji: String,
    isCorrectAnswer: Boolean?,
    correctAnswerEmoji: String,
    onChoiceSelected: (String) -> Unit
) {
    var isChosen by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(isCorrectAnswer) {
        if (isCorrectAnswer == null) isChosen = false
    }

    val isCorrect = choiceEmoji == correctAnswerEmoji
    val showResult = isCorrectAnswer != null
    
    val backgroundColor = when {
        showResult && isCorrect -> SuccessGreen
        showResult && isChosen && !isCorrect -> ErrorRed
        else -> MaterialTheme.colorScheme.surface
    }
    
    val contentColor = if (showResult && (isCorrect || isChosen)) Color.White else TextMain

    LaunchedEffect(isCorrectAnswer, isChosen) {
        if (isChosen && showResult) {
            if (isCorrect) {
                scale.animateTo(1.1f, tween(100))
                scale.animateTo(1f, tween(100))
            } else {
                repeat(3) {
                    shakeOffset.animateTo(10f, tween(50))
                    shakeOffset.animateTo(-10f, tween(50))
                }
                shakeOffset.animateTo(0f, tween(50))
            }
        }
    }

    Surface(
        onClick = {
            if (!showResult) {
                isChosen = true
                onChoiceSelected(choiceEmoji)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                translationX = shakeOffset.value
            },
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = if (!showResult) BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant) else null,
        shadowElevation = if (showResult) 0.dp else 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = choiceEmoji, fontSize = 36.sp)
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            choices.forEach { choice ->
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedChoiceButton(choice, isCorrectAnswer, correctAnswerEmoji, onChoiceSelected)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            choices.forEach { choice ->
                AnimatedChoiceButton(choice, isCorrectAnswer, correctAnswerEmoji, onChoiceSelected)
            }
        }
    }
}

@Composable
fun GameScreenLayout(content: @Composable () -> Unit) {
    GameBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val highScoreManager = remember { HighScoreManager(context) }

    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }

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
            GameHeader(onBack = onNavigateToStart)
            
            if (gameState.questionNumber == 0) {
                PreGameContent(
                    gameModeName = "Normal Mode",
                    gameDescription = "Answer questions to increase your score!",
                    highScore = gameState.highScore,
                    onStartGame = { viewModel.startGame() }
                )
            } else {
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                
                if (isLandscape) {
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Scoreboard(gameState.score, gameState.highScore, gameState.lives, gameState.currentStreakCount)
                            EmojiChainDisplay(gameState.emojiChain)
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                            ChoiceButtons(gameState.choices, gameState.correctAnswerEmoji, gameState.isCorrectAnswer, viewModel::handleChoice)
                        }
                    }
                } else {
                    Scoreboard(gameState.score, gameState.highScore, gameState.lives, gameState.currentStreakCount)
                    QuestionProgress(gameState.questionNumber, gameState.totalQuestions)
                    EmojiChainDisplay(gameState.emojiChain)
                    ChoiceButtons(gameState.choices, gameState.correctAnswerEmoji, gameState.isCorrectAnswer, viewModel::handleChoice)
                }

                GameResultHandler(
                    gameState = gameState,
                    onStartGame = { viewModel.startGame() },
                    onHandleAdReward = { viewModel.handleAdReward() },
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
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimarySoft)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                color = if (isError) ErrorRed else PrimarySoft,
                fontWeight = FontWeight.Black
            )
        },
        text = message,
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = PrimarySoft)) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text(dismissButtonText, color = TextSecondary)
                }
            }
        },
        shape = RoundedCornerShape(28.dp)
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
    val title = if (isWon) "Victory!" else "Nice Try!"
    val mainMessage = if (isWon) "You're a master!" else {
        when (reason) {
            LossReason.OutOfLives -> "Out of lives!"
            LossReason.TimeOut -> "Time's Up!"
            null -> "Game Over!"
        }
    }

    StyledAlertDialog(
        title = title,
        message = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(mainMessage, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SCORE", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("${gameState.score}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = PrimarySoft)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("BEST: ${gameState.highScore}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                if (onWatchAd != null && !isWon) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        enabled = !isLoading,
                        onClick = onWatchAd,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondarySoft)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (adWatched) "CONTINUE" else "WATCH AD TO CONTINUE")
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

    var adWatched by remember { mutableStateOf(false) }
    var previousGameResult by remember { mutableStateOf<GameResult?>(null) }

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

    LaunchedEffect(gameState.gameResult) {
        if (gameState.gameResult != previousGameResult) {
            adWatched = false
            previousGameResult = gameState.gameResult
            if ((gameState.gameResult is GameResult.Won || gameState.gameResult is GameResult.Lost)
                && AdManager.shouldShowAd()
            ) {
                showInterstitialAd(
                    interstitialAd = interstitialAdState.interstitialAd,
                    activity = activity,
                    onAdClosed = { interstitialAdState.loadAd() }
                )
            }
        }
    }

    when (val result = gameState.gameResult) {
        GameResult.InProgress -> {}

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
