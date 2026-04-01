package com.play.emojireactionchain.utils

import com.play.emojireactionchain.model.GameMode

data class AchievementBadge(
    val title: String,
    val emoji: String,
    val description: String
)

class AchievementBadgeManager {
    fun getUnlockedBadges(
        dailyStreak: Int,
        bestScores: Map<GameMode, Int>,
        stickerCount: Int
    ): List<AchievementBadge> {
        val badges = mutableListOf<AchievementBadge>()

        if (stickerCount >= 1) {
            badges += AchievementBadge("First Sticker", "🎁", "Collected your first sticker")
        }
        if (stickerCount >= 5) {
            badges += AchievementBadge("Sticker Collector", "📒", "Collected 5 stickers")
        }
        if (stickerCount >= 10) {
            badges += AchievementBadge("Sticker Master", "🌟", "Collected 10 stickers")
        }
        if (dailyStreak >= 3) {
            badges += AchievementBadge("Streak Starter", "🔥", "Played 3 days in a row")
        }
        if (dailyStreak >= 7) {
            badges += AchievementBadge("Daily Champ", "🏆", "Played 7 days in a row")
        }
        if ((bestScores.values.maxOrNull() ?: 0) >= 1000) {
            badges += AchievementBadge("Score Star", "⭐", "Reached a score of 1000")
        }
        if (bestScores.values.count { it >= 500 } >= 2) {
            badges += AchievementBadge("Mode Mixer", "🎮", "Scored well in 2 modes")
        }

        return badges
    }
}

