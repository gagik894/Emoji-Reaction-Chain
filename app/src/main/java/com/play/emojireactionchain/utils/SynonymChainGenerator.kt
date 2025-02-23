package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule
import com.play.emojireactionchain.viewModel.BaseGameViewModel
import com.play.emojireactionchain.model.GeneratedChainData

class SynonymChainGenerator : EmojiChainGenerator {
    override fun generateChain(category: EmojiCategory, rule: GameRule, level:Int): GeneratedChainData {
        // For Synonym Chain, let's primarily use "Faces" and "Emotions" categories
        val synonymCategories = listOfNotNull(
            BaseGameViewModel.emojiCategories["Faces"],
            BaseGameViewModel.emojiCategories["Emotions"]
        )

        if (synonymCategories.isEmpty() || synonymCategories.all { it.emojis.isEmpty() }) {
            return SequentialChainGenerator().generateChain(category, rule, level) // Fallback
        }

        // Select emojis from the synonym categories
        val selectedEmojis = mutableListOf<String>()
        for (synonymCategory in synonymCategories) {
            selectedEmojis.addAll(synonymCategory.emojis)
        }
        val distinctSynonymEmojis = selectedEmojis.distinct()

        if (distinctSynonymEmojis.size < 2) {
            return SequentialChainGenerator().generateChain(category, rule, level) // Fallback
        }
        // Adjust chain length based on level
        val chainLength = when(level){
            1 -> 2
            2-> 3
            3 -> 4
            else -> (3..4).random()
        }
        val emojiChain = distinctSynonymEmojis.shuffled().take(chainLength)
        val correctAnswerEmoji = emojiChain.firstOrNull() ?: "" // Correct answer: first emoji
        val choices = SynonymOptionGenerator().generateOptions(correctAnswerEmoji, category, rule, emojiChain)

        return GeneratedChainData(emojiChain.toList(), choices, correctAnswerEmoji)
    }
}