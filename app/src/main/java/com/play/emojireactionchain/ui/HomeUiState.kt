package com.play.emojireactionchain.ui

import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.utils.AchievementBadge

data class HomeUiState(
    val dailyStreak: Int = 1,
    val modeHighScores: Map<GameMode, Int> = emptyMap(),
    val stickerCount: Int = 0,
    val unlockedStickers: Set<String> = emptySet(),
    val dailyStickerEmoji: String? = null,
    val avatarLevelEmoji: String = "",
    val avatarLevelTitle: String = "",
    val avatarLevelSubtitle: String = "",
    val unlockedBadges: List<AchievementBadge> = emptyList(),
    val isLoading: Boolean = true
)
