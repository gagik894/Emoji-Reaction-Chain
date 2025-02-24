package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule
import com.play.emojireactionchain.viewModel.BaseGameViewModel.Companion.emojiCategories

class SequentialOptionGenerator : AnswerOptionGenerator {
    override fun generateOptions(
        correctAnswerEmoji: String,
        category: EmojiCategory,
        rule: GameRule,
        emojiChain: List<String>,
        level: Int
    ): List<String> {
        val emojisInCategory = category.emojis
        val choices = mutableListOf<String>()
        if (correctAnswerEmoji.isBlank()) return choices // No correct answer, no options

        choices.add(correctAnswerEmoji)

        val lastEmojiInChain = emojiChain.lastOrNull() ?: return choices
        val lastIndex = emojisInCategory.indexOf(lastEmojiInChain)
        if (lastIndex == -1) return choices

        // --- Level-Based Distractor Selection ---
        val distractors = mutableListOf<String>()

        if (level == 1) {
            // Level 1: All distractors from DIFFERENT categories
            distractors.addAll(
                category.emojisFromOtherCategories(emojiCategories.values)
                    .filterNot { it == correctAnswerEmoji || emojiChain.contains(it) }
                    .shuffled()
                    .take(3)
            )
        } else {
            // Level 2+: Prioritize nearby emojis, then same category, then others
            val nearbyDistractors = mutableListOf<String>()
            var attempts = 0
            while (nearbyDistractors.size < 3 && attempts < 20) { // Limit attempts
                attempts++
                val offset = (1..5).random() * (if (kotlin.random.Random.nextBoolean()) 1 else -1)
                val potentialIndex = (lastIndex + offset).mod(emojisInCategory.size)
                val potentialDistractor = emojisInCategory[potentialIndex]
                if (potentialDistractor != correctAnswerEmoji && !emojiChain.contains(potentialDistractor) && !nearbyDistractors.contains(potentialDistractor)) {
                    nearbyDistractors.add(potentialDistractor)
                }
            }
            distractors.addAll(nearbyDistractors)

            // Fill remaining slots with same-category, then other-category emojis
            val sameCategoryRemaining = emojisInCategory.filterNot {
                it == correctAnswerEmoji || emojiChain.contains(it) || distractors.contains(it)
            }.shuffled()
            distractors.addAll(sameCategoryRemaining.take(3 - distractors.size))

            val otherCategoryRemaining = category.emojisFromOtherCategories(emojiCategories.values)
                .filterNot { it == correctAnswerEmoji || emojiChain.contains(it) || distractors.contains(it) }
                .shuffled()
            distractors.addAll(otherCategoryRemaining.take(3 - distractors.size))
        }

        choices.addAll(distractors.take(3)) // Ensure exactly 3 distractors
        return choices.shuffled()
    }

    private fun EmojiCategory.emojisFromOtherCategories(allCategories: Collection<EmojiCategory>): List<String> {
        return allCategories.filterNot { it == this }.flatMap { it.emojis }.distinct()
    }
}