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
            badges += AchievementBadge("Sticker Sprout", "🎁", "Collected your very first sticker")
        }
        if (stickerCount >= 5) {
            badges += AchievementBadge("Sticker Explorer", "📒", "Collected 5 stickers and counting")
        }
        if (stickerCount >= 10) {
            badges += AchievementBadge("Sticker Star", "🌟", "Collected 10 stickers and shining bright")
        }
        if (dailyStreak >= 3) {
            badges += AchievementBadge("Cuddle Streak", "🔥", "Played 3 days in a row")
        }
        if (dailyStreak >= 7) {
            badges += AchievementBadge("Daily Hero", "🏆", "Played 7 days in a row")
        }
        if ((bestScores.values.maxOrNull() ?: 0) >= 1000) {
            badges += AchievementBadge("Score Spark", "⭐", "Reached a super sparkly score of 1000")
        }
        if (bestScores.values.count { it >= 500 } >= 2) {
            badges += AchievementBadge("Mode Explorer", "🎮", "Scored well in 2 modes")
        }

        return badges
    }
}

