package com.play.emojireactionchain.ui

import android.annotation.SuppressLint
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.ui.theme.EmojiGameTheme

@SuppressLint("UnrememberedMutableState")
@Composable
fun EmojiGameApp() {
    var isShowingTutorial by remember { mutableStateOf(true) }
    var selectedGameMode by remember { mutableStateOf<GameMode?>(null) } // Allow nullable GameMode

    EmojiGameTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isShowingTutorial) {
                TutorialScreen(onTutorialFinished = {
                    isShowingTutorial = false
                })
            } else if (selectedGameMode == null) { // Show mode selection if no mode is chosen
                ModeSelectionScreen(onModeSelected = { gameMode ->
                    selectedGameMode = gameMode
                })
            } else {
                when (selectedGameMode) {
                    GameMode.NORMAL -> NormalModeScreen()
                    GameMode.TIMED -> NormalModeScreen()
                    GameMode.DECODING -> NormalModeScreen()
                    GameMode.BLITZ -> NormalModeScreen()
                    null -> TODO()
                }
            }
        }
    }
}