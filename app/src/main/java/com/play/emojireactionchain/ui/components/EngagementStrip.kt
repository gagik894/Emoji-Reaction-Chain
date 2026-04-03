package com.play.emojireactionchain.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.play.emojireactionchain.R
import com.play.emojireactionchain.ui.GameBackground
import com.play.emojireactionchain.ui.theme.EmojiGameTheme
import com.play.emojireactionchain.ui.theme.PrimarySoft
import com.play.emojireactionchain.ui.theme.SecondarySoft

@Composable
fun EngagementStrip(
    isBonusRound: Boolean,
    missionProgress: Int,
    missionTarget: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        if (isBonusRound) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SecondarySoft.copy(alpha = 0.18f)
            ) {
                Text(
                    text = stringResource(R.string.engagement_bonus_round),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = SecondarySoft,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PrimarySoft.copy(alpha = 0.12f)
        ) {
            Text(
                text = stringResource(R.string.engagement_streak_mission, missionProgress, missionTarget),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = PrimarySoft,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EngagementStripPreview() {
    EmojiGameTheme {
        GameBackground {
            EngagementStrip(isBonusRound = true, missionProgress = 1, missionTarget = 3)
        }
    }
}
