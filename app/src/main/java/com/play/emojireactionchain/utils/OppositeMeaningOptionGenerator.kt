package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule
import com.play.emojireactionchain.viewModel.BaseGameViewModel

class OppositeMeaningOptionGenerator : AnswerOptionGenerator {
    override fun generateOptions(correctAnswerEmoji: String, category: EmojiCategory, rule: GameRule, emojiChain: List<String>): List<String> {
        val emojisInCategory = category.emojis
        val choices = mutableListOf<String>(correctAnswerEmoji)
        val emojiCategories = BaseGameViewModel.emojiCategories // Access emojiCategories

        val emotionDistractors = if (category.name == "Emotions") {
            emojiCategories["Emotions"]?.emojis?.filterNot { emoji ->
                emoji == correctAnswerEmoji ||
                        emojiChain.contains(emoji) ||
                        emoji.startsWith("😊") || emoji.startsWith("😀") || emoji.startsWith("😄") || emoji.startsWith("😁")
            } ?: emptyList()
        } else emptyList() // Should not happen as chain generation falls back

        val distractorChoices = if (category.name == "Emotions") {
            emotionDistractors.shuffled().take(3)
        } else { // Fallback to sequential options if not emotion category for options generation (should not be reached)
            val sameCategoryDistractors = emojisInCategory.filterNot { it == correctAnswerEmoji }.shuffled().take(1)
            val otherCategoryDistractors = emojiCategories.values.flatMap { it.emojis }.distinct()
                .filterNot { it == correctAnswerEmoji || emojisInCategory.contains(it) }
                .shuffled().take(2)
            (sameCategoryDistractors + otherCategoryDistractors)
        }

        choices.addAll(distractorChoices)
        choices.shuffle()
        return choices
    }
}