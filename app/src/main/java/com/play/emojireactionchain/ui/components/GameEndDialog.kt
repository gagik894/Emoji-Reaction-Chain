package com.play.emojireactionchain.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.play.emojireactionchain.R
import com.play.emojireactionchain.model.GameState
import com.play.emojireactionchain.model.LossReason
import com.play.emojireactionchain.ui.GameBackground
import com.play.emojireactionchain.ui.theme.EmojiGameTheme
import com.play.emojireactionchain.ui.theme.ErrorRed
import com.play.emojireactionchain.ui.theme.PrimarySoft
import com.play.emojireactionchain.ui.theme.SecondarySoft
import com.play.emojireactionchain.ui.theme.TextSecondary

@Composable
fun StyledAlertDialog(
    title: String,
    message: @Composable () -> Unit,
    confirmButtonText: String,
    dismissButtonText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    isError: Boolean = true
) {
    val dismissText = dismissButtonText ?: stringResource(R.string.dialog_cancel)

    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = if (isError) ErrorRed else PrimarySoft,
                fontWeight = FontWeight.Black
            )
        },
        text = message,
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = PrimarySoft)) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text(dismissText, color = TextSecondary)
                }
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun GameEndDialog(
    isWon: Boolean,
    reason: LossReason? = null,
    gameState: GameState,
    onPlayAgain: () -> Unit,
    onWatchAd: (() -> Unit)? = null,
    isLoading: Boolean = false,
    adWatched: Boolean = false,
    onBack: () -> Unit = {}
) {
    val title = stringResource(if (isWon) R.string.game_end_title_victory else R.string.game_end_title_nice_try)
    val mainMessage = if (isWon) stringResource(R.string.game_end_message_master) else {
        when (reason) {
            LossReason.OutOfLives -> stringResource(R.string.game_end_message_out_of_lives)
            LossReason.TimeOut -> stringResource(R.string.game_end_message_time_up)
            LossReason.GenerationFailed -> stringResource(R.string.game_end_message_generation_failed)
            null -> stringResource(R.string.game_end_message_game_over)
        }
    }

    StyledAlertDialog(
        title = title,
        message = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(mainMessage, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.score_label), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("${gameState.score}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = PrimarySoft)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(stringResource(R.string.best_score_label, gameState.highScore), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                if (onWatchAd != null && !isWon) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        enabled = !isLoading,
                        onClick = onWatchAd,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondarySoft)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(if (adWatched) R.string.game_end_continue else R.string.game_end_watch_ad))
                        }
                    }
                }
            }
        },
        confirmButtonText = stringResource(R.string.game_end_play_again),
        onConfirm = onPlayAgain,
        onDismiss = onBack,
        isError = !isWon
    )
}

@Preview(showBackground = true)
@Composable
fun GameEndDialogPreview() {
    EmojiGameTheme {
        GameBackground {
            GameEndDialog(
                isWon = false,
                reason = LossReason.OutOfLives,
                gameState = GameState(score = 1500, highScore = 3000),
                onPlayAgain = {},
                onWatchAd = {}
            )
        }
    }
}
