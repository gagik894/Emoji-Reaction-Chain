package com.play.emojireactionchain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.play.emojireactionchain.R
import com.play.emojireactionchain.ui.GameBackground
import com.play.emojireactionchain.ui.theme.EmojiGameTheme

@Composable
fun GameHeader(showBack: Boolean = true, onBack: () -> Unit = {}) {
    if (!showBack) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(46.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.game_header_back_description),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun BottomBannerAd(modifier: Modifier = Modifier) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val isPreview = LocalInspectionMode.current

    if (!isLandscape) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isPreview) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.banner_ad_preview_placeholder),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 96.dp, vertical = 14.dp)
                    )
                }
            } else {
                com.play.emojireactionchain.ui.BannerAd(adUnitId = "ca-app-pub-2523891738770793/9481725035")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameHeaderPreview() {
    EmojiGameTheme {
        GameBackground {
            androidx.compose.foundation.layout.Column {
                GameHeader()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomBannerAdPreview() {
    EmojiGameTheme {
        GameBackground {
            BottomBannerAd()
        }
    }
}
