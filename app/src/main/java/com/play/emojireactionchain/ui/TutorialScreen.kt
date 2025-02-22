package com.play.emojireactionchain.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TutorialScreen(onTutorialFinished: () -> Unit) { // Callback to notify EmojiGameApp when tutorial is done
    var currentStep by remember { mutableIntStateOf(0) }

    val tutorialSteps = listOf(
        "Welcome to Emoji Reaction Chain!\n\nYour goal is to complete the emoji chain by choosing the emoji that logically continues the sequence.",
        "An emoji chain will be shown at the top.\n\nBelow are your choices. Tap a button to choose.",
        "Emojis are connected by rules!\n\nFor example: 🍎, 🍌, 🍇 are all Fruits in Sequential order.\n\nFigure out the rule and choose the next emoji.",
        "Correct answer: Green flash, sound, score up!\n\nIncorrect answer: Red flash, sound, Game Over!",
        "Your score increases with correct answers.\n\nTry to get the highest score!\n\nTap 'Start Playing!' to begin!",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            "How to Play",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            tutorialSteps[currentStep],
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (currentStep > 0) {
                Button(onClick = { currentStep-- }) {
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.width(80.dp))
            }

            if (currentStep < tutorialSteps.size - 1) {
                Button(onClick = { currentStep++ }) {
                    Text("Next")
                }
            } else {
                Button(onClick = onTutorialFinished) { // Just call onTutorialFinished - EmojiGameApp handles state
                    Text("Start Playing!")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onTutorialFinished) { // Just call onTutorialFinished - EmojiGameApp handles state
            Text("Skip Tutorial")
        }
    }
}