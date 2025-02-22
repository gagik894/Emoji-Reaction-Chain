package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule
import com.play.emojireactionchain.viewModel.BaseGameViewModel.Companion.emojiCategories

class SequentialOptionGenerator : AnswerOptionGenerator {
    override fun generateOptions(correctAnswerEmoji: String, category: EmojiCategory, rule: GameRule, emojiChain: List<String>): List<String> {
        val emojisInCategory = category.emojis
        val choices = mutableListOf<String>(correctAnswerEmoji)

        val sameCategoryDistractors = emojisInCategory.filterNot { it == correctAnswerEmoji }.shuffled().take(1)
        val otherCategoryDistractors = category.emojisFromOtherCategories(emojiCategories.values).filterNot { it == correctAnswerEmoji || emojisInCategory.contains(it) }.shuffled().take(2)


        choices.addAll(sameCategoryDistractors + otherCategoryDistractors)
        choices.shuffle()
        return choices
    }

    // Extension function to get emojis from other categories (helper function)
    private fun EmojiCategory.emojisFromOtherCategories(allCategories: Collection<EmojiCategory>): List<String> {
        return allCategories.filterNot { it == this }.flatMap { it.emojis }.distinct().toList()
    }
}