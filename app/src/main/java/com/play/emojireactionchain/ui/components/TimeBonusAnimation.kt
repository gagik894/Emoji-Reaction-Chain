package com.play.emojireactionchain.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.play.emojireactionchain.R
import com.play.emojireactionchain.ui.GameBackground
import com.play.emojireactionchain.ui.theme.EmojiGameTheme
import com.play.emojireactionchain.ui.theme.SecondarySoft

@Composable
fun TimeBonusAnimation(bonusPoints: Int) {
    if (bonusPoints <= 0) return

    val translateYPx = remember { Animatable(0f) }
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val density = LocalDensity.current

    LaunchedEffect(bonusPoints) {
        translateYPx.animateTo(-150f, animationSpec = tween(300))
        scale.animateTo(1.3f, animationSpec = tween(200))
        alpha.animateTo(1f, animationSpec = tween(200))
        translateYPx.animateTo(0f, animationSpec = tween(400))
        scale.animateTo(1f, animationSpec = tween(300))
        alpha.animateTo(0f, animationSpec = tween(300))
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.time_bonus_text, bonusPoints),
            style = MaterialTheme.typography.titleLarge,
            color = SecondarySoft,
            fontSize = 36.sp,
            modifier = Modifier
                .offset(y = with(density) { translateYPx.value.toDp() })
                .graphicsLayer(scaleX = scale.value, scaleY = scale.value, alpha = alpha.value)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimeBonusAnimationPreview() {
    EmojiGameTheme {
        GameBackground {
            TimeBonusAnimation(bonusPoints = 50)
        }
    }
}
