package com.play.emojireactionchain.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.play.emojireactionchain.R
import com.play.emojireactionchain.ui.GameBackground
import com.play.emojireactionchain.ui.Routes
import com.play.emojireactionchain.ui.theme.EmojiGameTheme
import com.play.emojireactionchain.ui.theme.PrimarySoft
import com.play.emojireactionchain.ui.theme.SecondarySoft
import com.play.emojireactionchain.ui.theme.TertiarySoft
import com.play.emojireactionchain.ui.theme.WarningOrange

sealed class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val activeColor: Color
) {
    object Home : BottomNavItem(Routes.START, R.string.bottom_nav_play, Icons.Rounded.Gamepad, PrimarySoft)
    object Treasures : BottomNavItem(Routes.COLLECTION, R.string.bottom_nav_treasures, Icons.Rounded.AutoAwesome, SecondarySoft)
    object Shop : BottomNavItem(Routes.SHOP, R.string.bottom_nav_shop, Icons.Rounded.Storefront, TertiarySoft)
    object Rank : BottomNavItem(Routes.RANK, R.string.bottom_nav_rank, Icons.Rounded.EmojiEvents, WarningOrange)
}

@Composable
fun PlayfulBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom bar on these top-level routes
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    if (showBottomBar) {
        PlayfulBottomBarContent(
            currentRoute = currentRoute,
            onItemClick = { item ->
                if (currentRoute != item.route) {
                    navController.navigate(item.route) {
                        popUpTo(Routes.START) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )
    }
}

private val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Treasures,
    BottomNavItem.Shop,
    BottomNavItem.Rank
)

@Composable
private fun PlayfulBottomBarContent(
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit
) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = (navBarPadding + 12.dp).coerceAtLeast(16.dp)),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route
                PlayfulBottomNavItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
private fun PlayfulBottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val label = stringResource(item.labelRes)
    val activeColor = item.activeColor

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "item_scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        label = "icon_color"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            
            if (isSelected) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = activeColor,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                // Active dot indicator
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .background(activeColor, CircleShape)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayfulBottomBarPreview() {
    EmojiGameTheme {
        GameBackground {
            Box(contentAlignment = Alignment.BottomCenter) {
                PlayfulBottomBarContent(
                    currentRoute = Routes.START,
                    onItemClick = {}
                )
            }
        }
    }
}
