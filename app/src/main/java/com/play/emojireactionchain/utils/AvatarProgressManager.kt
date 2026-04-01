package com.play.emojireactionchain.utils

data class AvatarProgress(
    val title: String,
    val emoji: String,
    val subtitle: String
)

class AvatarProgressManager {
    fun getAvatarProgress(stickerCount: Int): AvatarProgress {
        return when {
            stickerCount >= 16 -> AvatarProgress("Legend Hero", "🦄", "Level up: legendary friend")
            stickerCount >= 10 -> AvatarProgress("Star Captain", "🦁", "Level up: brave explorer")
            stickerCount >= 6 -> AvatarProgress("Spark Friend", "🦊", "Level up: playful helper")
            stickerCount >= 3 -> AvatarProgress("Play Pal", "🐶", "Level up: cheerful buddy")
            else -> AvatarProgress("Tiny Buddy", "🐣", "Level up: just starting out")
        }
    }
}

