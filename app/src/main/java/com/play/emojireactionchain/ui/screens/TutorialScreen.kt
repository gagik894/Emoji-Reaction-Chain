package com.play.emojireactionchain.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.play.emojireactionchain.R
import com.play.emojireactionchain.ui.theme.PrimarySoft
import com.play.emojireactionchain.ui.theme.SecondarySoft
import com.play.emojireactionchain.ui.theme.TextMain

private data class TutorialStep(
    val titleRes: Int,
    val bodyRes: Int,
    val emoji: String,
    val bubbleColor: Color
)

@Composable
fun TutorialScreen(onTutorialFinished: () -> Unit) {
    val steps = listOf(
        TutorialStep(
            titleRes = R.string.tutorial_step_1_title,
            bodyRes = R.string.tutorial_step_1_body,
            emoji = "🧩",
            bubbleColor = Color(0xFFE8F5E9)
        ),
        TutorialStep(
            titleRes = R.string.tutorial_step_2_title,
            bodyRes = R.string.tutorial_step_2_body,
            emoji = "⚡",
            bubbleColor = Color(0xFFE3F2FD)
        ),
        TutorialStep(
            titleRes = R.string.tutorial_step_3_title,
            bodyRes = R.string.tutorial_step_3_body,
            emoji = "🎯",
            bubbleColor = Color(0xFFFCE4EC)
        )
    )

    var currentStep by remember { mutableIntStateOf(0) }
    val isLastStep = currentStep == steps.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.tutorial_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = PrimarySoft,
                fontSize = 32.sp
            ),
            modifier = Modifier.padding(top = 16.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mascot Emoji
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White, CircleShape)
                    .shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 50.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn(
                            animationSpec = tween(300)
                        )).togetherWith(
                            slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut(
                                animationSpec = tween(300)
                            ))
                    } else {
                        (slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn(
                            animationSpec = tween(300)
                        )).togetherWith(
                            slideOutHorizontally(animationSpec = tween(300)) { width -> width } + fadeOut(
                                animationSpec = tween(300)
                            ))
                    }
                },
                label = "TutorialStepAnimation"
            ) { targetStepIndex ->
                val targetStep = steps[targetStepIndex]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(targetStep.bubbleColor, RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(targetStep.emoji, fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(targetStep.titleRes),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(targetStep.bodyRes),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 22.sp,
                            color = TextMain.copy(alpha = 0.8f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    onClick = { currentStep-- },
                    shape = RoundedCornerShape(28.dp),
                    border = null
                ) {
                    Text(
                        stringResource(R.string.tutorial_action_back),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    onClick = onTutorialFinished,
                    shape = RoundedCornerShape(28.dp),
                    border = null
                ) {
                    Text(
                        stringResource(R.string.tutorial_action_skip),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp)),
                onClick = {
                    if (isLastStep) onTutorialFinished() else currentStep++
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLastStep) SecondarySoft else PrimarySoft
                )
            ) {
                Text(
                    text = if (isLastStep) {
                        stringResource(R.string.tutorial_action_start)
                    } else {
                        stringResource(R.string.tutorial_action_next)
                    },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
        }
    }
}