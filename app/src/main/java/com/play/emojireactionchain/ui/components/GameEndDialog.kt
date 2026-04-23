package com.play.emojireactionchain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SentimentSatisfied
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.play.emojireactionchain.R
import com.play.emojireactionchain.model.GameState
import com.play.emojireactionchain.model.LossReason
import com.play.emojireactionchain.ui.GameBackground
import com.play.emojireactionchain.ui.theme.EmojiGameTheme
import com.play.emojireactionchain.ui.theme.PrimarySoft
import com.play.emojireactionchain.ui.theme.SecondarySoft

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
    val accent = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val dismissText = dismissButtonText ?: stringResource(R.string.dialog_cancel)

    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(shape = CircleShape, color = accent.copy(alpha = 0.12f)) {
                    Icon(
                        imageVector = if (isError) Icons.Rounded.SentimentSatisfied else Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = accent,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = message,
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimarySoft)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(confirmButtonText, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text(
                        dismissText,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        shape = RoundedCornerShape(34.dp),
        containerColor = MaterialTheme.colorScheme.surface
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
                Text(mainMessage, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primary.copy(alpha = 0.04f))
                                )
                            )
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            stringResource(R.string.score_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(stringResource(R.string.score_value, gameState.score), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = PrimarySoft)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                        Text(stringResource(R.string.best_score_label, gameState.highScore), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                if (onWatchAd != null && !isWon) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        enabled = !isLoading,
                        onClick = onWatchAd,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondarySoft)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(if (adWatched) R.string.game_end_continue else R.string.game_end_watch_ad),
                                    fontWeight = FontWeight.Black
                                )
                            }
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
fun StyledAlertDialogPreview() {
    EmojiGameTheme {
        GameBackground {
            StyledAlertDialog(
                title = stringResource(R.string.game_end_title_victory),
                message = {
                    Text(
                        text = stringResource(R.string.game_end_message_master),
                        fontWeight = FontWeight.Bold
                    )
                },
                confirmButtonText = stringResource(R.string.game_end_play_again),
                onConfirm = {},
                isError = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameEndDialogLostPreview() {
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

@Preview(showBackground = true)
@Composable
fun GameEndDialogVictoryPreview() {
    EmojiGameTheme {
        GameBackground {
            GameEndDialog(
                isWon = true,
                gameState = GameState(score = 2400, highScore = 2400),
                onPlayAgain = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameEndDialogRewardLoadingPreview() {
    EmojiGameTheme {
        GameBackground {
            GameEndDialog(
                isWon = false,
                reason = LossReason.TimeOut,
                gameState = GameState(score = 620, highScore = 1200),
                onPlayAgain = {},
                onWatchAd = {},
                isLoading = true
            )
        }
    }
}
