package com.play.emojireactionchain.utils

import com.play.emojireactionchain.model.GeneratedChainData
import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule

class SequentialChainGenerator : EmojiChainGenerator {
    override fun generateChain(
        category: EmojiCategory, rule: GameRule, level: Int
    ): GeneratedChainData {
        val emojisInCategory = category.emojis
        if (emojisInCategory.isEmpty()) return GeneratedChainData(emptyList(), emptyList(), "")

        val chainLength = when (level) {
            1 -> 3
            2 -> 4
            3 -> 5
            else -> (6..8).random() // For higher levels, use a random length between 6 and 8
        }

        val emojiChain = mutableListOf<String>()
        var lastEmojiIndex = -1

        repeat(chainLength) {
            var nextEmojiIndex = (lastEmojiIndex + 1) % emojisInCategory.size
            if (nextEmojiIndex < 0) nextEmojiIndex = 0 // Should not be needed, but good for safety
            emojiChain.add(emojisInCategory[nextEmojiIndex])
            lastEmojiIndex = nextEmojiIndex
        }

        val correctAnswerEmoji = emojisInCategory[(lastEmojiIndex + 1) % emojisInCategory.size]
        val choices = SequentialOptionGenerator().generateOptions(
            correctAnswerEmoji, category, rule, emojiChain
        ) // Use SequentialOptionGenerator
        return GeneratedChainData(emojiChain.toList(), choices, correctAnswerEmoji)
    }
}