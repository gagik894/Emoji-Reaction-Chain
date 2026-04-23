package com.play.emojireactionchain.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.play.emojireactionchain.ui.GameBackground
import com.play.emojireactionchain.ui.theme.EmojiGameTheme
import com.play.emojireactionchain.ui.theme.ErrorRed
import com.play.emojireactionchain.ui.theme.PrimarySoft
import com.play.emojireactionchain.ui.theme.SuccessGreen

@Composable
fun AnimatedChoiceButton(
    choiceEmoji: String,
    isCorrectAnswer: Boolean?,
    correctAnswerEmoji: String,
    onChoiceSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isChosen by remember(choiceEmoji) { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(isCorrectAnswer) {
        if (isCorrectAnswer == null) isChosen = false
    }

    val isCorrect = choiceEmoji == correctAnswerEmoji
    val showResult = isCorrectAnswer != null

    val targetColor = when {
        showResult && isCorrect -> SuccessGreen
        showResult && isChosen -> ErrorRed
        else -> MaterialTheme.colorScheme.surface
    }
    val backgroundColor by animateColorAsState(targetColor, label = "choice_color")
    val contentColor = if (showResult && (isCorrect || isChosen)) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    LaunchedEffect(isCorrectAnswer, isChosen) {
        if (isChosen && showResult) {
            if (isCorrect) {
                scale.animateTo(1.1f, tween(100))
                scale.animateTo(1f, tween(120))
            } else {
                repeat(3) {
                    shakeOffset.animateTo(10f, tween(45))
                    shakeOffset.animateTo(-10f, tween(45))
                }
                shakeOffset.animateTo(0f, tween(45))
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
        modifier = modifier
            .height(86.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                translationX = shakeOffset.value
                alpha = if (showResult && !isCorrect && !isChosen) 0.52f else 1f
            },
        shape = RoundedCornerShape(26.dp),
        color = backgroundColor,
        border = BorderStroke(2.dp, if (showResult) backgroundColor else PrimarySoft.copy(alpha = 0.28f)),
        shadowElevation = if (showResult) 2.dp else 9.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (showResult) {
                        Brush.verticalGradient(listOf(backgroundColor, backgroundColor))
                    } else {
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.surface, PrimarySoft.copy(alpha = 0.08f))
                        )
                    }
                )
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = choiceEmoji, fontSize = 40.sp, color = contentColor, maxLines = 1)
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
    val rows = choices.chunked(2)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(if (isLandscape) 10.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            rows.forEach { rowChoices ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    if (rowChoices.size == 1) {
                        Box(modifier = Modifier.weight(0.5f))
                    }
                    rowChoices.forEach { choice ->
                        AnimatedChoiceButton(
                            choice,
                            isCorrectAnswer,
                            correctAnswerEmoji,
                            onChoiceSelected,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowChoices.size == 1) {
                        Box(modifier = Modifier.weight(0.5f))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnimatedChoiceButtonPreview() {
    EmojiGameTheme {
        GameBackground {
            AnimatedChoiceButton(
                choiceEmoji = "A",
                isCorrectAnswer = true,
                correctAnswerEmoji = "A",
                onChoiceSelected = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChoiceButtonsFourChoicesPreview() {
    EmojiGameTheme {
        GameBackground {
            ChoiceButtons(
                choices = listOf("A", "B", "C", "D"),
                correctAnswerEmoji = "A",
                isCorrectAnswer = null,
                onChoiceSelected = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChoiceButtonsThreeChoicesPreview() {
    EmojiGameTheme {
        GameBackground {
            ChoiceButtons(
                choices = listOf("A", "B", "C"),
                correctAnswerEmoji = "B",
                isCorrectAnswer = null,
                onChoiceSelected = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChoiceButtonsResultPreview() {
    EmojiGameTheme {
        GameBackground {
            ChoiceButtons(
                choices = listOf("A", "B", "C", "D"),
                correctAnswerEmoji = "C",
                isCorrectAnswer = true,
                onChoiceSelected = {}
            )
        }
    }
}
