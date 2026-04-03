package com.play.emojireactionchain.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.play.emojireactionchain.ui.GameBackground
import com.play.emojireactionchain.ui.theme.EmojiGameTheme
import com.play.emojireactionchain.ui.theme.ErrorRed
import com.play.emojireactionchain.ui.theme.SuccessGreen

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
        showResult && isChosen -> ErrorRed
        else -> MaterialTheme.colorScheme.surface
    }

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

@Preview(showBackground = true)
@Composable
fun ChoiceButtonsPreview() {
    EmojiGameTheme {
        GameBackground {
            ChoiceButtons(
                choices = listOf("🚒", "👨‍🚒", "💧", "🔥"),
                correctAnswerEmoji = "🚒",
                isCorrectAnswer = null,
                onChoiceSelected = {}
            )
        }
    }
}
