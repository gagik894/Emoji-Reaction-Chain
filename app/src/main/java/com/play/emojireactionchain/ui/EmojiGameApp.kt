package com.play.emojireactionchain.ui

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.play.emojireactionchain.model.GameMode


object Routes {
    const val TUTORIAL = "tutorial"
    const val START = "start"
    const val NORMAL_MODE = "normal"
    const val TIMED_MODE = "timed"
    const val SURVIVAL_MODE = "survival"
    const val BLITZ_MODE = "blitz"
}

object AdManager {
    private var gamePlayCount = 0
    private var shouldShowAdOnHomeReturn = false

    fun incrementGamePlayCount(): Int {
        println("increment Game play count: $gamePlayCount")
        gamePlayCount++
        return gamePlayCount
    }

    fun shouldShowAd(): Boolean {
        println("ShouldShowAd Game play count: $gamePlayCount")
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

    fun resetGamePlayCount() {
        gamePlayCount = 0
    }
}

@Composable
fun EmojiGameApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    var showTutorial by rememberSaveable { mutableStateOf(isFirstLaunch(context)) }  // Check first launch

    val activity = context as? Activity
    val interstitialAdState = rememberInterstitialAd("ca-app-pub-2523891738770793/6480157179")

    NavHost(
        navController = navController,
        startDestination = if (showTutorial) Routes.TUTORIAL else Routes.START
    ) {
        composable(Routes.TUTORIAL) {
            TutorialScreen(
                onTutorialFinished = {
                    showTutorial = false
                    markFirstLaunchComplete(context) // Mark tutorial as shown
                    navController.navigate(Routes.START) {
                        popUpTo(Routes.TUTORIAL) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.START) {
            // Check if we should show an ad on returning to home
            LaunchedEffect(true) {
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

            ModeSelectionScreen { mode ->
                val route = when (mode) {
                    GameMode.NORMAL -> Routes.NORMAL_MODE
                    GameMode.TIMED -> Routes.TIMED_MODE
                    GameMode.SURVIVAL -> Routes.SURVIVAL_MODE
                    GameMode.BLITZ -> Routes.BLITZ_MODE
                }
                navController.navigate(route)
            }
        }
        composable(Routes.NORMAL_MODE) {
            NormalModeScreen(onNavigateToStart = {
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