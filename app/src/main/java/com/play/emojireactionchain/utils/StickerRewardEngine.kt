package com.play.emojireactionchain.utils

object StickerCatalog {
    val stickers: List<String> = listOf(
        "🌟", "🎈", "🦄", "🚀", "🐳", "🍭", "🎮", "🧁", "🪁", "🎨",
        "🦋", "🧸", "🍉", "🍓", "🍩", "🍪", "🐼", "🐶", "🐱", "🐸",
        "🐰", "🦁", "🐯", "🐝"
    )
}

class StickerRewardEngine(
    private val stickers: List<String> = StickerCatalog.stickers
) {
    fun nextSticker(unlocked: Set<String>): String? {
        return stickers.firstOrNull { it !in unlocked }
    }
}

