package com.play.emojireactionchain.utils

import com.play.emojireactionchain.model.GeneratedChainData
import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule
import com.play.emojireactionchain.viewModel.GameViewModel

class OppositeMeaningChainGenerator : EmojiChainGenerator {
    override fun generateChain(category: EmojiCategory, rule: GameRule): GeneratedChainData {
        if (category.name != "Emotions") return SequentialChainGenerator().generateChain(category, rule) // Fallback if not Emotions category

        val emojisInCategory = category.emojis
        val chainLength = (3..5).random()
        val emojiChain = mutableListOf<String>()
        var lastEmoji: String? = null
        val oppositeEmojiMap = GameViewModel.oppositeEmojiMap // Access oppositeEmojiMap

        repeat(chainLength) {
            val nextEmoji: String
            if (lastEmoji == null) {
                nextEmoji = emojisInCategory.random()
            } else {
                nextEmoji = oppositeEmojiMap[lastEmoji] ?: emojisInCategory.random()
            }
            emojiChain.add(nextEmoji)
            lastEmoji = nextEmoji
        }
        val correctAnswerEmoji = oppositeEmojiMap[lastEmoji] ?: emojisInCategory.random()
        val choices = OppositeMeaningOptionGenerator().generateOptions(correctAnswerEmoji, category, rule, emojiChain) // Use OppositeMeaningOptionGenerator
        return GeneratedChainData(emojiChain.toList(), choices, correctAnswerEmoji)
    }
}