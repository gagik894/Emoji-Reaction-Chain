package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule
import com.play.emojireactionchain.viewModel.GameViewModel
import com.play.emojireactionchain.utils.SequentialOptionGenerator // Import SequentialOptionGenerator for fallback

class SynonymOptionGenerator : AnswerOptionGenerator {
    override fun generateOptions(correctAnswerEmoji: String, category: EmojiCategory, rule: GameRule, emojiChain: List<String>): List<String> {
        val choices = mutableListOf<String>(correctAnswerEmoji)

        val synonymCategories = listOf(
            GameViewModel.emojiCategories["Faces"],
            GameViewModel.emojiCategories["Emotions"]
        ).filterNotNull()
        val possibleDistractors = mutableListOf<String>()

        synonymCategories.forEach { synonymCategory ->
            possibleDistractors.addAll(synonymCategory.emojis.filterNot { it == correctAnswerEmoji || emojiChain.contains(it) })
        }

        val otherCategoriesForDistraction = emojiCategories.values.filterNot { synonymCategories.contains(it) }
        otherCategoriesForDistraction.forEach { nonSynonymCategory ->
            possibleDistractors.addAll(nonSynonymCategory.emojis.shuffled().take(2)) // Take few distractors from other categories
        }


        val distractorChoices = possibleDistractors.distinct().shuffled().take(3) // Take 3 distractors
        choices.addAll(distractorChoices)
        choices.shuffle()
        return choices
    }

    // Access emojiCategories map (companion object)
    companion object {
        private val emojiCategories = GameViewModel.emojiCategories
    }
}