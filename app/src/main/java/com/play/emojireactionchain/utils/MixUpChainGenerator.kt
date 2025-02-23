package com.play.emojireactionchain.utils

import com.play.emojireactionchain.model.GeneratedChainData
import com.play.emojireactionchain.viewModel.BaseGameViewModel
import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule

class MixUpChainGenerator : EmojiChainGenerator {
    override fun generateChain(
        category: EmojiCategory,
        rule: GameRule,
        level: Int
    ): GeneratedChainData {
        val selectedCategories =
            BaseGameViewModel.emojiCategories.values.shuffled().take(2) // Always use 2 categories
        val category1Emojis = selectedCategories.getOrNull(0)?.emojis ?: emptyList() // Use getOrNull and safe calls
        val category2Emojis = selectedCategories.getOrNull(1)?.emojis ?: emptyList()

        if (category1Emojis.isEmpty() || category2Emojis.isEmpty()) {
            return SequentialChainGenerator().generateChain(category, rule, level) // Fallback
        }

        // Adjust chain length based on level
        val chainLength = when (level) {
            1 -> 2
            2 -> 3
            3 -> 4
            else -> (4..5).random() // Longer chains for higher levels
        }

        // Ensure we don't try to take more emojis than available in categories
        val numFromCat1 = (1..<chainLength).random().coerceAtMost(category1Emojis.size)
        val numFromCat2 = (chainLength - numFromCat1).coerceAtMost(category2Emojis.size)


        val emojiChain =
            (category1Emojis.shuffled().take(numFromCat1) + category2Emojis.shuffled().take(numFromCat2)).shuffled() //take elements
        val correctAnswerEmoji = (category1Emojis + category2Emojis).distinct()
            .randomOrNull() ?: ""  //correct answer can be any of emojis
        val choices =
            MixUpOptionGenerator().generateOptions(correctAnswerEmoji, category, rule, emojiChain)
        return GeneratedChainData(emojiChain.toList(), choices, correctAnswerEmoji)
    }

    // Access emojiCategories map (companion object)
    companion object {
        private val emojiCategories = BaseGameViewModel.emojiCategories
    }
}