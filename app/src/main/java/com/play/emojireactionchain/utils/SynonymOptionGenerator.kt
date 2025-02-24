package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule
import com.play.emojireactionchain.viewModel.BaseGameViewModel

class SynonymOptionGenerator : AnswerOptionGenerator {
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
        val synonymPairs = BaseGameViewModel.synonymPairs
        val allEmojis = BaseGameViewModel.emojiCategories.values.flatMap { it.emojis }.distinct()

        // Find synonyms of the correct answer, *excluding* emojis in the chain.
        val correctSynonyms = synonymPairs.firstOrNull { it.contains(correctAnswerEmoji) } ?: listOf()
        val validSynonyms = correctSynonyms.filterNot { it == correctAnswerEmoji || emojiChain.contains(it) }


        // Distractors that are NOT synonyms and are not in the chain
        val validDistractors = allEmojis.filterNot { emoji ->
            emoji == correctAnswerEmoji ||
                    emojiChain.contains(emoji) ||
                    correctSynonyms.contains(emoji)
        }

        val distractorChoices = when (level) {
            1, 2 -> {
                // Levels 1 & 2:  Mostly random distractors.
                validDistractors.shuffled().take(3)
            }
            3 -> {
                // Level 3: One synonym (if available), two random.
                val synonymDistractor = validSynonyms.shuffled().take(1)
                val otherDistractors = validDistractors.shuffled().take(2)
                (synonymDistractor + otherDistractors).distinct() // Ensure uniqueness
            }
            else -> {
                // Level 4+: As many synonyms as possible.
                val synonymDistractors = validSynonyms.shuffled().take(3)
                val remainingCount = 3 - synonymDistractors.size
                val otherDistractors = validDistractors.shuffled().take(remainingCount)
                (synonymDistractors + otherDistractors).distinct().take(3)
            }
        }
        choices.addAll(distractorChoices)
        return choices.shuffled()
    }
}