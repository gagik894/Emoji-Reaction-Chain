package com.play.emojireactionchain.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
                    GameMode.TIMED -> TimedModeScreen()
                    GameMode.DECODING -> NormalModeScreen()
                    GameMode.BLITZ -> BlitzModeScreen()
                    null -> TODO()
                }
            }
        }
    }
}