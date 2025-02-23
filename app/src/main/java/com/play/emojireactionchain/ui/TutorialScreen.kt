package com.play.emojireactionchain.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.play.emojireactionchain.model.GameMode
import kotlinx.coroutines.delay

@Composable
fun TutorialScreen(onTutorialFinished: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(0) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        delay(200) // Shorter delay
        visible = true
    }

    val tutorialSteps = listOf(
        "Welcome to Emoji Reaction Chain!\n\nComplete the emoji chain by choosing the next logical emoji." to "🎉",
        "Chain at the top, choices below. Tap to choose!" to "👆",
        "Rules connect emojis!\n\nSequential: 🍎, 🍌, 🍇\nOpposites: 😀, 😢\nMix-Up: 🚗, 🐶, 🚕\nSynonym: 😊, 😀" to "🤔",
        "Game Modes:\n\n${GameMode.NORMAL.name}\n${GameMode.TIMED.name}\n${GameMode.SURVIVAL.name}\n${GameMode.BLITZ.name}" to "🕹️",
        "Get the highest score! Tap below to begin!" to "🏆"
    )

    val (currentText, currentEmoji) = tutorialSteps[currentStep]

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .statusBarsPadding()
            ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Use Arrangement.Top
        ) {
            AnimatedVisibility( // Title animation
                visible = visible,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut()
            ) {
                Text(
                    "How to Play",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 32.dp) // More space below title
                )
            }
            Spacer(modifier = Modifier.weight(0.5f)) // Push buttons to the bottom

            // Use Crossfade to smoothly transition between steps
            Crossfade(
                targetState = currentStep,
                animationSpec = tween(durationMillis = 500), label = ""
            ) { step ->
                val (text, emoji) = tutorialSteps[step]
                Row(
                    verticalAlignment = Alignment.CenterVertically, // Center vertically
                    horizontalArrangement = Arrangement.spacedBy(16.dp), // Space between emoji and text
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        emoji,
                        fontSize = 32.sp, // Smaller emoji
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f) // Allow text to take available space
                            .padding(vertical = 16.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart // Align text to start, for better readability
                    ) {
                        Text(
                            text,
                            textAlign = TextAlign.Start, // Align text to start
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

            }


            Spacer(modifier = Modifier.weight(1f)) // Push buttons to the bottom

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                if (currentStep > 0) {
                    Button(
                        onClick = { currentStep-- },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text("Previous", color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                if (currentStep < tutorialSteps.size - 1) {
                    Button(onClick = { currentStep++ }, shape = RoundedCornerShape(50)) {
                        Text("Next")
                    }
                } else {
                    Button(
                        onClick = onTutorialFinished,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text("Start Playing!", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            AnimatedVisibility( // Skip button animation
                visible = visible && currentStep < tutorialSteps.size - 1,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Button(
                    onClick = onTutorialFinished,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Skip Tutorial")
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Add a spacer at the bottom.
        }
    }
}