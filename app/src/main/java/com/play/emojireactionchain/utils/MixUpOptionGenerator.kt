package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.BaseGameViewModel.Companion.emojiCategories
import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule

// MixUpOptionGenerator.kt
class MixUpOptionGenerator : AnswerOptionGenerator {
    override fun generateOptions(
        correctAnswerEmoji: String,
        category: EmojiCategory,
        rule: GameRule,
        emojiChain: List<String>,
        level: Int
    ): List<String> {
        val choices = mutableListOf<String>()
        if(correctAnswerEmoji.isBlank()) return choices
        choices.add(correctAnswerEmoji)

        val allEmojis = emojiCategories.values.flatMap { it.emojis }.distinct()
        val availableDistractors =
            allEmojis.filterNot { it == correctAnswerEmoji || emojiChain.contains(it) }

        // Get categories of emojis *in* the chain
        val chainCategories = emojiChain.mapNotNull { emojiInChain ->
            emojiCategories.entries.firstOrNull { it.value.emojis.contains(emojiInChain) }?.key
        }

        val distractorChoices = when (level) {
            1 -> availableDistractors.shuffled().take(3) // Level 1: Completely random
            2, 3 -> {
                // Level 2 & 3: At least one distractor from a chain category, if possible
                val relatedDistractors = availableDistractors.filter { distractor ->
                    chainCategories.any { categoryName ->
                        emojiCategories[categoryName]?.emojis?.contains(distractor) == true
                    }
                }.shuffled().take(1) // Try to get one related

                val unrelatedDistractors =
                    availableDistractors.filterNot { it in relatedDistractors }.shuffled()
                        .take(2)
                relatedDistractors + unrelatedDistractors
            }
            else -> {
                // Level 4+:  Prioritize emojis from the categories *present* in the chain.
                val relatedDistractors = availableDistractors.filter { distractor ->
                    chainCategories.any { categoryName ->
                        emojiCategories[categoryName]?.emojis?.contains(distractor) == true
                    }
                }.shuffled().take(3)

                if (relatedDistractors.size < 3) {
                    (relatedDistractors + availableDistractors.shuffled()
                        .take(3 - relatedDistractors.size)).distinct()
                } else {
                    relatedDistractors
                }
            }
        }.shuffled()

        choices.addAll(distractorChoices)
        return choices.shuffled()
    }
}