package com.play.emojireactionchain.utils

import com.play.emojireactionchain.model.GeneratedChainData
import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule

class SequentialChainGenerator : EmojiChainGenerator {
    override fun generateChain(category: EmojiCategory, rule: GameRule): GeneratedChainData {
        val emojisInCategory = category.emojis
        if (emojisInCategory.isEmpty()) return GeneratedChainData(emptyList(), emptyList(), "")

        val chainLength = (3..5).random()
        val emojiChain = mutableListOf<String>()
        var lastEmojiIndex = -1

        repeat(chainLength) {
            var nextEmojiIndex = (lastEmojiIndex + 1) % emojisInCategory.size
            if (nextEmojiIndex < 0) nextEmojiIndex = 0
            emojiChain.add(emojisInCategory[nextEmojiIndex])
            lastEmojiIndex = nextEmojiIndex
        }
        val correctAnswerEmoji = emojisInCategory[(lastEmojiIndex + 1) % emojisInCategory.size]
        val choices = SequentialOptionGenerator().generateOptions(correctAnswerEmoji, category, rule, emojiChain) // Use SequentialOptionGenerator
        return GeneratedChainData(emojiChain.toList(), choices, correctAnswerEmoji)
    }
}