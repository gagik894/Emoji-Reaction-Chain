package com.play.emojireactionchain.ui.screens

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.GameState
import com.play.emojireactionchain.ui.AdManager
import com.play.emojireactionchain.ui.components.ChoiceButtons
import com.play.emojireactionchain.ui.components.EmojiChainDisplay
import com.play.emojireactionchain.ui.components.EngagementStrip
import com.play.emojireactionchain.ui.components.GameEndDialog
import com.play.emojireactionchain.ui.components.GameHeader
import com.play.emojireactionchain.ui.components.HintCard
import com.play.emojireactionchain.ui.components.PreGameContent
import com.play.emojireactionchain.ui.components.Scoreboard
import com.play.emojireactionchain.ui.components.TimeBonusAnimation
import com.play.emojireactionchain.ui.rememberInterstitialAd
import com.play.emojireactionchain.ui.rememberRewardedAd
import com.play.emojireactionchain.ui.showInterstitialAd
import com.play.emojireactionchain.ui.showRewardedAd
import kotlinx.coroutines.delay

@Composable
fun GameScreenLayout(content: @Composable () -> Unit) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = navBarPadding)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
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
            GameHeader(onBack = onNavigateToStart)

            if (gameState.questionNumber == 0) {
                PreGameContent(
                    gameModeNameRes,
                    gameDescriptionRes,
                    highScore = gameState.highScore,
                    onStartGame = onStartGame
                )
            } else {
                if (isLandscape) {
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Scoreboard(gameState.score, gameState.highScore, if (showLivesInScoreboard) gameState.lives else null, gameState.currentStreakCount)
                            EngagementStrip(gameState.isBonusRound, gameState.streakMissionProgress, gameState.streakMissionTarget)
                            HintCard(gameState.rule?.hintRes, gameState.categoryEmoji)
                            centerContent()
                            EmojiChainDisplay(gameState.emojiChain)
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                            ChoiceButtons(gameState.choices, gameState.correctAnswerEmoji, gameState.isCorrectAnswer, onChoiceSelected)
                        }
                    }
                } else {
                    Scoreboard(gameState.score, gameState.highScore, if (showLivesInScoreboard) gameState.lives else null, gameState.currentStreakCount)
                    EngagementStrip(gameState.isBonusRound, gameState.streakMissionProgress, gameState.streakMissionTarget)
                    HintCard(gameState.rule?.hintRes, gameState.categoryEmoji)
                    centerContent()
                    EmojiChainDisplay(gameState.emojiChain)
                    ChoiceButtons(gameState.choices, gameState.correctAnswerEmoji, gameState.isCorrectAnswer, onChoiceSelected)
                }

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
