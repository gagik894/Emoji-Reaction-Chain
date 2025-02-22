package com.play.emojireactionchain.utils

import com.play.emojireactionchain.model.GeneratedChainData
import com.play.emojireactionchain.viewModel.BaseGameViewModel
import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule

class MixUpChainGenerator : EmojiChainGenerator {
    override fun generateChain(category: EmojiCategory, rule: GameRule): GeneratedChainData {
        val selectedCategories = emojiCategories.values.shuffled().take(2)
        val category1Emojis = selectedCategories[0].emojis
        val category2Emojis = selectedCategories[1].emojis

        if (category1Emojis.isEmpty() || category2Emojis.isEmpty()) {
            return SequentialChainGenerator().generateChain(category, rule) // Fallback to sequential if categories are empty
        }

        val chainLength = (2..3).random()
        val emojiChain = (category1Emojis.shuffled().take(1) + category2Emojis.shuffled().take(1)).shuffled()
        val correctAnswerEmoji = (category1Emojis + category2Emojis).distinct().randomOrNull() ?: ""
        val choices = MixUpOptionGenerator().generateOptions(correctAnswerEmoji, category, rule, emojiChain) // Use MixUpOptionGenerator
        return GeneratedChainData(emojiChain.toList(), choices, correctAnswerEmoji)
    }

    // Access emojiCategories map (companion object)
    companion object {
        private val emojiCategories = BaseGameViewModel.emojiCategories
    }
}