package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.BaseGameViewModel
import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule

class MixUpOptionGenerator : AnswerOptionGenerator {
    override fun generateOptions(correctAnswerEmoji: String, category: EmojiCategory, rule: GameRule, emojiChain: List<String>): List<String> {
        val choices = mutableListOf<String>(correctAnswerEmoji)

        val chainCategories = mutableSetOf<String>()
        emojiChain.forEach { emojiInChain ->
            emojiCategories.forEach { _, emojiCat ->
                if (emojiCat.emojis.contains(emojiInChain)) {
                    chainCategories.add(emojiCat.name)
                }
            }
        }
        val possibleDistractors = mutableListOf<String>()
        chainCategories.forEach { catName ->
            possibleDistractors.addAll(emojiCategories[catName]?.emojis?.filterNot { it == correctAnswerEmoji || emojiChain.contains(it) } ?: emptyList())
        }
        val distractorChoices = possibleDistractors.distinct().shuffled().take(3)
        choices.addAll(distractorChoices)
        choices.shuffle()
        return choices
    }

    // Access emojiCategories map (companion object)
    companion object {
        private val emojiCategories = BaseGameViewModel.emojiCategories
    }
}