package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule
import com.play.emojireactionchain.viewModel.BaseGameViewModel

class OppositeMeaningOptionGenerator : AnswerOptionGenerator {
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

        val oppositeEmojiMap = BaseGameViewModel.oppositeEmojiMap
        val allEmojis = BaseGameViewModel.emojiCategories.values.flatMap { it.emojis }.distinct()

        val validDistractors = allEmojis.filterNot { emoji ->
            emoji == correctAnswerEmoji ||
                    emojiChain.contains(emoji) ||
                    oppositeEmojiMap[emoji] == correctAnswerEmoji ||
                    oppositeEmojiMap.containsValue(emoji) && oppositeEmojiMap.entries.firstOrNull{it.value == emoji}?.key == correctAnswerEmoji
        }


        // Prioritize opposites of *other* emojis in the chain (if available) for higher levels.
        val chainOpposites = emojiChain.mapNotNull { oppositeEmojiMap[it] }.filterNot { it == correctAnswerEmoji || choices.contains(it)}

        val distractorChoices = when (level) {
            1, 2 -> validDistractors.shuffled().take(3)
            3 -> {
                // Try to include *one* chain opposite, if possible
                val chosenChainOpposite = chainOpposites.shuffled().take(1)
                val otherDistractors = validDistractors.shuffled().take(2)
                (chosenChainOpposite + otherDistractors).distinct() // Ensure unique
            }
            else -> {
                // Level 4+:  Try to use *mostly* chain opposites.
                val chosenChainOpposites = chainOpposites.shuffled().take(3)
                val otherDistractors = validDistractors.shuffled().take(3 - chosenChainOpposites.size)
                (chosenChainOpposites + otherDistractors).distinct().take(3)
            }
        }

        choices.addAll(distractorChoices)
        return choices.shuffled()
    }
}