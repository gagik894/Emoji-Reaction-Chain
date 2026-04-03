package com.play.emojireactionchain.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.ui.screens.BlitzModeScreen
import com.play.emojireactionchain.ui.screens.CollectionScreen
import com.play.emojireactionchain.ui.screens.ModeSelectionScreen
import com.play.emojireactionchain.ui.screens.NormalGameScreen
import com.play.emojireactionchain.ui.screens.SurvivalModeScreen
import com.play.emojireactionchain.ui.screens.TimedModeScreen
import com.play.emojireactionchain.ui.screens.TutorialScreen
import com.play.emojireactionchain.utils.AchievementBadgeManager
import com.play.emojireactionchain.utils.AvatarProgressManager
import com.play.emojireactionchain.utils.DailyStreakManager
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.SoundManager
import com.play.emojireactionchain.utils.StickerBookManager

object Routes {
    const val TUTORIAL = "tutorial"
    const val START = "start"
    const val NORMAL_MODE = "normal"
    const val TIMED_MODE = "timed"
    const val SURVIVAL_MODE = "survival"
    const val BLITZ_MODE = "blitz"
    const val COLLECTION = "collection"
}

object AdManager {
    private var gamePlayCount = 0
    private var shouldShowAdOnHomeReturn = false

    fun incrementGamePlayCount(): Int {
        gamePlayCount++
        return gamePlayCount
    }

    fun shouldShowAd(): Boolean {
        return gamePlayCount % 3 == 0 && gamePlayCount > 0
    }

    fun markAdShownOnHomeReturn() {
        shouldShowAdOnHomeReturn = false
    }

    fun markShowAdOnHomeReturn() {
        shouldShowAdOnHomeReturn = true
    }

    fun shouldShowAdOnHomeReturn(): Boolean {
        return shouldShowAdOnHomeReturn
    }
}

@Composable
fun EmojiGameApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val highScoreManager = remember(context) { HighScoreManager(context) }
    val dailyStreakManager = remember(context) { DailyStreakManager(context) }
    val stickerBookManager = remember(context) { StickerBookManager(context) }
    val avatarProgressManager = remember { AvatarProgressManager() }
    val achievementBadgeManager = remember { AchievementBadgeManager() }
    val celebrationSoundManager = remember(context) { SoundManager(context) }

    DisposableEffect(Unit) {
        onDispose { celebrationSoundManager.release() }
    }

    var showTutorial by rememberSaveable { mutableStateOf(isFirstLaunch(context)) }
    var dailyStreak by rememberSaveable { mutableIntStateOf(1) }
    var modeHighScores by remember { mutableStateOf(emptyMap<GameMode, Int>()) }
    var stickerCount by rememberSaveable { mutableIntStateOf(0) }
    var unlockedStickers by remember { mutableStateOf(stickerBookManager.getUnlockedStickers()) }
    var dailyStickerEmoji by rememberSaveable { mutableStateOf<String?>(null) }
    var avatarLevelEmoji by rememberSaveable { mutableStateOf("") }
    var avatarLevelTitle by rememberSaveable { mutableStateOf("") }
    var avatarLevelSubtitle by rememberSaveable { mutableStateOf("") }
    var unlockedBadges by remember { mutableStateOf(emptyList<com.play.emojireactionchain.utils.AchievementBadge>()) }

    val activity = context as? Activity
    val interstitialAdState = rememberInterstitialAd("ca-app-pub-2523891738770793/6480157179")

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        GameBackground {
            NavHost(
                navController = navController,
                startDestination = if (showTutorial) Routes.TUTORIAL else Routes.START,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.TUTORIAL) {
                    TutorialScreen(
                        onTutorialFinished = {
                            showTutorial = false
                            markFirstLaunchComplete(context)
                            navController.navigate(Routes.START) {
                                popUpTo(Routes.TUTORIAL) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.START) {
                    LaunchedEffect(Unit) {
                        dailyStreak = dailyStreakManager.updateAndGetCurrentStreak()
                        modeHighScores = highScoreManager.getAllHighScores()
                        stickerCount = stickerBookManager.getStickerCount()
                        unlockedStickers = stickerBookManager.getUnlockedStickers()
                        dailyStickerEmoji = stickerBookManager.awardDailyStickerIfNeeded()?.sticker
                        
                        if (dailyStickerEmoji != null) {
                            stickerCount = stickerBookManager.getStickerCount()
                            unlockedStickers = stickerBookManager.getUnlockedStickers()
                            
                            celebrationSoundManager.playCorrectSound()
                            celebrationSoundManager.playCorrectHaptic()
                        }

                        val avatarProgress = avatarProgressManager.getAvatarProgress(stickerCount)
                        avatarLevelEmoji = avatarProgress.emoji
                        avatarLevelTitle = avatarProgress.title
                        avatarLevelSubtitle = avatarProgress.subtitle
                        unlockedBadges = achievementBadgeManager.getUnlockedBadges(dailyStreak, modeHighScores, stickerCount)

                        if (AdManager.shouldShowAdOnHomeReturn() && activity != null) {
                            showInterstitialAd(
                                interstitialAd = interstitialAdState.interstitialAd,
                                activity = activity,
                                onAdClosed = {
                                    interstitialAdState.loadAd()
                                    AdManager.markAdShownOnHomeReturn()
                                }
                            )
                        }
                    }

                    ModeSelectionScreen(
                        dailyStreak = dailyStreak,
                        bestScores = modeHighScores,
                        newStickerEmoji = dailyStickerEmoji,
                        onModeSelected = { mode ->
                            val route = when (mode) {
                                GameMode.NORMAL -> Routes.NORMAL_MODE
                                GameMode.TIMED -> Routes.TIMED_MODE
                                GameMode.SURVIVAL -> Routes.SURVIVAL_MODE
                                GameMode.BLITZ -> Routes.BLITZ_MODE
                            }
                            navController.navigate(route)
                        },
                        onCollectionSelected = { navController.navigate(Routes.COLLECTION) }
                    )
                }
                composable(Routes.NORMAL_MODE) {
                    NormalGameScreen(onNavigateToStart = {
                        navController.popBackStack(Routes.START, inclusive = false)
                    })
                }
                composable(Routes.TIMED_MODE) {
                    TimedModeScreen(onNavigateToStart = {
                        navController.popBackStack(Routes.START, inclusive = false)
                    })
                }
                composable(Routes.SURVIVAL_MODE) {
                    SurvivalModeScreen(onNavigateToStart = {
                        navController.popBackStack(Routes.START, inclusive = false)
                    })
                }
                composable(Routes.BLITZ_MODE) {
                    BlitzModeScreen(onNavigateToStart = {
                        navController.popBackStack(Routes.START, inclusive = false)

                    })
                }
                composable(Routes.COLLECTION) {
                    CollectionScreen(
                        unlockedStickers = unlockedStickers,
                        avatarEmoji = avatarLevelEmoji,
                        avatarTitle = avatarLevelTitle,
                        avatarSubtitle = avatarLevelSubtitle,
                        unlockedBadges = unlockedBadges,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

// Helper functions to manage first launch state
private const val PREFS_NAME = "app_prefs"
private const val KEY_FIRST_LAUNCH = "first_launch"

fun isFirstLaunch(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_FIRST_LAUNCH, true) // Default to true (first launch)
}

fun markFirstLaunchComplete(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit() { putBoolean(KEY_FIRST_LAUNCH, false) }
}