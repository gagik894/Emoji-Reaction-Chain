package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule
import com.play.emojireactionchain.viewModel.BaseGameViewModel
import com.play.emojireactionchain.model.GeneratedChainData

class SynonymChainGenerator : EmojiChainGenerator {
    override fun generateChain(category: EmojiCategory, rule: GameRule): GeneratedChainData {
        // For Synonym Chain, let's primarily use "Faces" and "Emotions" categories
        val synonymCategories = listOf(
            BaseGameViewModel.emojiCategories["Faces"],
            BaseGameViewModel.emojiCategories["Emotions"]
        ).filterNotNull() // Filter out null categories in case they are not found

        if (synonymCategories.isEmpty() || synonymCategories.all { it.emojis.isEmpty() }) {
            return SequentialChainGenerator().generateChain(category, rule) // Fallback if no suitable synonym categories or empty
        }

        // Select emojis from the synonym categories
        val selectedEmojis = mutableListOf<String>()
        for (synonymCategory in synonymCategories) {
            selectedEmojis.addAll(synonymCategory.emojis)
        }
        val distinctSynonymEmojis = selectedEmojis.distinct()

        if (distinctSynonymEmojis.size < 2) {
            return SequentialChainGenerator().generateChain(category, rule) // Fallback if not enough unique synonym emojis
        }

        val chainLength = (2..3).random() // Keep chain length similar to others
        val emojiChain = distinctSynonymEmojis.shuffled().take(chainLength)
        val correctAnswerEmoji = emojiChain.firstOrNull() ?: "" // Correct answer can be the first in the chain for simplicity
        val choices = SynonymOptionGenerator().generateOptions(correctAnswerEmoji, category, rule, emojiChain) // Use SynonymOptionGenerator

        return GeneratedChainData(emojiChain.toList(), choices, correctAnswerEmoji)
    }

    // Access emojiCategories map (companion object)
    companion object {
        private val emojiCategories = BaseGameViewModel.emojiCategories
    }
}