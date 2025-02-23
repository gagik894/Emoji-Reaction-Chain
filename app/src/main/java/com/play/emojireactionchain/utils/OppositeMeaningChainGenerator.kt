package com.play.emojireactionchain.utils

import com.play.emojireactionchain.model.GeneratedChainData
import com.play.emojireactionchain.viewModel.BaseGameViewModel
import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule

class OppositeMeaningChainGenerator : EmojiChainGenerator {
    override fun generateChain(category: EmojiCategory, rule: GameRule, level: Int): GeneratedChainData {
        if (category.name != "Emotions") return SequentialChainGenerator().generateChain(category, rule, level) // Fallback

        val emojisInCategory = category.emojis
        // Adjust chain length based on level
        val chainLength = when (level) {
            1 -> 2
            2 -> 3
            3 -> 4
            else -> (4..5).random()  // Longer chains for higher levels
        }

        val emojiChain = mutableListOf<String>()
        var lastEmoji: String? = null
        val oppositeEmojiMap = BaseGameViewModel.oppositeEmojiMap

        repeat(chainLength) {
            val nextEmoji: String
            if (lastEmoji == null) {
                nextEmoji = emojisInCategory.random()
            } else {
                // Get the opposite, or if no opposite, a random emoji from the category.
                nextEmoji = oppositeEmojiMap[lastEmoji] ?: emojisInCategory.random()
            }
            emojiChain.add(nextEmoji)
            lastEmoji = nextEmoji
        }

        // The correct answer is the opposite of the *last* emoji in the chain.
        val correctAnswerEmoji = oppositeEmojiMap[lastEmoji] ?: emojisInCategory.random()
        val choices = OppositeMeaningOptionGenerator().generateOptions(correctAnswerEmoji, category, rule, emojiChain)
        return GeneratedChainData(emojiChain.toList(), choices, correctAnswerEmoji)
    }
}