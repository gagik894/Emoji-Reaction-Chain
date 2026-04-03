package com.play.emojireactionchain.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.play.emojireactionchain.R
import com.play.emojireactionchain.ui.GameBackground
import com.play.emojireactionchain.ui.theme.EmojiGameTheme
import com.play.emojireactionchain.ui.theme.PrimarySoft
import com.play.emojireactionchain.ui.theme.TextMain
import com.play.emojireactionchain.ui.theme.TextSecondary

@Composable
fun StyledActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimarySoft)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PreGameContent(
    gameModeNameRes: Int,
    gameDescriptionRes: Int,
    highScore: Int,
    onStartGame: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(gameModeNameRes),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = TextMain
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(gameDescriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        if (highScore > 0) {
            Text(
                text = stringResource(R.string.pregame_best_score, highScore),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = PrimarySoft
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        StyledActionButton(text = stringResource(R.string.pregame_start_playing), onClick = onStartGame)
    }
}

@Preview(showBackground = true)
@Composable
fun PreGameContentPreview() {
    EmojiGameTheme {
        GameBackground {
            PreGameContent(
                gameModeNameRes = R.string.mode_normal_name,
                gameDescriptionRes = R.string.pregame_normal_description,
                highScore = 500,
                onStartGame = {}
            )
        }
    }
}
