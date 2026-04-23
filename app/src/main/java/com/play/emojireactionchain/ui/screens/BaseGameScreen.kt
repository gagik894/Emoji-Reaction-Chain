package com.play.emojireactionchain.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.play.emojireactionchain.R
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.GameState
import com.play.emojireactionchain.ui.AdManager
import com.play.emojireactionchain.ui.components.BottomBannerAd
import com.play.emojireactionchain.ui.components.ChoiceButtons
import com.play.emojireactionchain.ui.components.EmojiChainDisplay
import com.play.emojireactionchain.ui.components.EngagementStrip
import com.play.emojireactionchain.ui.components.GameEndDialog
import com.play.emojireactionchain.ui.components.GameHeader
import com.play.emojireactionchain.ui.components.PreGameContent
import com.play.emojireactionchain.ui.components.Scoreboard
import com.play.emojireactionchain.ui.components.TimeBonusAnimation
import com.play.emojireactionchain.ui.rememberInterstitialAd
import com.play.emojireactionchain.ui.rememberRewardedAd
import com.play.emojireactionchain.ui.showInterstitialAd
import com.play.emojireactionchain.ui.showRewardedAd
import com.play.emojireactionchain.ui.theme.EmojiGameTheme
import com.play.emojireactionchain.ui.theme.ErrorRed
import com.play.emojireactionchain.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

@Composable
fun VisualTimerBar(
    progress: Float, // 1.0 (full) to 0.0 (empty)
    modifier: Modifier = Modifier
) {
    val barColor by animateColorAsState(
        targetValue = if (progress > 0.3f) SuccessGreen else ErrorRed,
        label = "timer_color"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp),
        shape = RoundedCornerShape(7.dp),
        color = Color.Black.copy(alpha = 0.1f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxSize()
                .clip(RoundedCornerShape(7.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(barColor.copy(alpha = 0.8f), barColor)
                    )
                )
        )
    }
}

@Composable
fun GameScreenLayout(content: @Composable ColumnScope.() -> Unit) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)
                    )
                )
            )
            .statusBarsPadding()
            .padding(bottom = navBarPadding)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 920.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
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
                                onHandleAdReward()
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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BaseGameScreen(
    gameModeNameRes: Int,
    gameDescriptionRes: Int,
    gameState: GameState,
    onStartGame: () -> Unit,
    onNavigateToStart: () -> Unit,
    onChoiceSelected: (String) -> Unit,
    onHandleAdReward: () -> Unit,
    bonusAnimationDelay: Long = 1000L,
    showLivesInScoreboard: Boolean = true,
    centerContent: @Composable () -> Unit = {}
) {
    var showTimeBonusAnimation by remember { mutableStateOf(false) }
    var currentBonusPointsForAnimation by remember { mutableIntStateOf(0) }

    LaunchedEffect(gameState.currentTimeBonus) {
        if (gameState.currentTimeBonus > 0) {
            showTimeBonusAnimation = true
            currentBonusPointsForAnimation = gameState.currentTimeBonus
            delay(bonusAnimationDelay)
            showTimeBonusAnimation = false
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box {
        GameScreenLayout {
            GameHeader(showBack = gameState.questionNumber == 0, onBack = onNavigateToStart)

            if (gameState.questionNumber == 0) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    PreGameContent(
                        gameModeNameRes,
                        gameDescriptionRes,
                        highScore = gameState.highScore,
                        onStartGame = onStartGame
                    )
                }
                BottomBannerAd()
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val compactHeight = maxHeight < 620.dp

                    if (isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(0.9f),
                                verticalArrangement = Arrangement.spacedBy(if (compactHeight) 8.dp else 12.dp)
                            ) {
                                Scoreboard(
                                    gameState.score,
                                    gameState.highScore,
                                    if (showLivesInScoreboard) gameState.lives else null,
                                    gameState.currentStreakCount
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    EngagementStrip(
                                        gameState.isBonusRound,
                                        gameState.streakMissionProgress,
                                        gameState.streakMissionTarget
                                    )
                                    centerContent()
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1.15f),
                                verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
                            ) {
                                EmojiChainDisplay(gameState.emojiChain)
                                ChoiceButtons(
                                    gameState.choices,
                                    gameState.correctAnswerEmoji,
                                    gameState.isCorrectAnswer,
                                    onChoiceSelected
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(if (compactHeight) 8.dp else 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Scoreboard(
                                gameState.score,
                                gameState.highScore,
                                if (showLivesInScoreboard) gameState.lives else null,
                                gameState.currentStreakCount
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                EngagementStrip(
                                    gameState.isBonusRound,
                                    gameState.streakMissionProgress,
                                    gameState.streakMissionTarget
                                )
                                centerContent()
                            }
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                EmojiChainDisplay(gameState.emojiChain)
                            }
                            ChoiceButtons(
                                gameState.choices,
                                gameState.correctAnswerEmoji,
                                gameState.isCorrectAnswer,
                                onChoiceSelected
                            )
                        }
                    }
                }
                BottomBannerAd()

                GameResultHandler(
                    gameState = gameState,
                    onStartGame = onStartGame,
                    onHandleAdReward = onHandleAdReward,
                    onBack = onNavigateToStart
                )
            }
        }

        if (showTimeBonusAnimation) {
            TimeBonusAnimation(bonusPoints = currentBonusPointsForAnimation)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BaseGameScreenPreGamePreview() {
    EmojiGameTheme {
        BaseGameScreen(
            gameModeNameRes = R.string.mode_normal_name,
            gameDescriptionRes = R.string.pregame_normal_description,
            gameState = GameState(),
            onStartGame = {},
            onNavigateToStart = {},
            onChoiceSelected = {},
            onHandleAdReward = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BaseGameScreenInProgressPreview() {
    EmojiGameTheme {
        BaseGameScreen(
            gameModeNameRes = R.string.mode_normal_name,
            gameDescriptionRes = R.string.pregame_normal_description,
            gameState = GameState(
                questionNumber = 1,
                score = 100,
                highScore = 500,
                emojiChain = listOf("🍎", "🍌"),
                choices = listOf("🍒", "🍇", "🍉", "🍍"),
                correctAnswerEmoji = "🍒",
                streakMissionTarget = 5,
                streakMissionProgress = 2
            ),
            onStartGame = {},
            onNavigateToStart = {},
            onChoiceSelected = {},
            onHandleAdReward = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BaseGameScreenBonusRoundPreview() {
    EmojiGameTheme {
        BaseGameScreen(
            gameModeNameRes = R.string.mode_timed_name,
            gameDescriptionRes = R.string.pregame_timed_description,
            gameState = GameState(
                questionNumber = 10,
                score = 1250,
                highScore = 2000,
                emojiChain = listOf("🚀", "🛸", "🪐"),
                choices = listOf("🌌", "🌠", "🌑", "☀️"),
                correctAnswerEmoji = "🌌",
                isBonusRound = true,
                currentStreakCount = 8
            ),
            onStartGame = {},
            onNavigateToStart = {},
            onChoiceSelected = {},
            onHandleAdReward = {},
            showLivesInScoreboard = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BaseGameScreenVictoryPreview() {
    EmojiGameTheme {
        BaseGameScreen(
            gameModeNameRes = R.string.mode_normal_name,
            gameDescriptionRes = R.string.pregame_normal_description,
            gameState = GameState(
                questionNumber = 5,
                score = 500,
                highScore = 500,
                gameResult = GameResult.Won
            ),
            onStartGame = {},
            onNavigateToStart = {},
            onChoiceSelected = {},
            onHandleAdReward = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BaseGameScreenLostPreview() {
    EmojiGameTheme {
        BaseGameScreen(
            gameModeNameRes = R.string.mode_survival_name,
            gameDescriptionRes = R.string.pregame_survival_description,
            gameState = GameState(
                questionNumber = 12,
                score = 1200,
                highScore = 2500,
                lives = 0,
                gameResult = GameResult.Lost(com.play.emojireactionchain.model.LossReason.OutOfLives)
            ),
            onStartGame = {},
            onNavigateToStart = {},
            onChoiceSelected = {},
            onHandleAdReward = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BaseGameScreenLandscapePreview() {
    EmojiGameTheme {
        BaseGameScreen(
            gameModeNameRes = R.string.mode_normal_name,
            gameDescriptionRes = R.string.pregame_normal_description,
            gameState = GameState(
                questionNumber = 1,
                score = 100,
                highScore = 500,
                emojiChain = listOf("🍎", "🍌"),
                choices = listOf("🍒", "🍇", "🍉", "🍍"),
                correctAnswerEmoji = "🍒"
            ),
            onStartGame = {},
            onNavigateToStart = {},
            onChoiceSelected = {},
            onHandleAdReward = {}
        )
    }
}
