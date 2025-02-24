package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.BaseGameViewModel

class OppositeQuestionGenerator : QuestionGenerator {
    override fun generateQuestion(
        availableEmojis: List<String>,
        level: Int
    ): Triple<List<String>, String, List<String>> {
        val oppositeEmojiMap = BaseGameViewModel.oppositeEmojiMap
        val validEmojis = availableEmojis.filter {
            oppositeEmojiMap.containsKey(it) || oppositeEmojiMap.containsValue(it)
        }

        if (validEmojis.size < 2) {
            return Triple(emptyList(), "", emptyList())
        }

        // Chain length capped at 5, and at least 2.
        val chainLength = when (level) {
            1 -> 2
            2, 3 -> 3
            4, 5 -> 4
            else -> 5
        }.coerceAtMost(5)


        var attempts = 0
        val maxAttempts = 20
        while (attempts < maxAttempts) {
            attempts++
            val emojiChain = mutableListOf<String>()
            var lastEmoji: String? = null

            repeat(chainLength) {
                val nextEmoji: String
                if (lastEmoji == null) {
                    // Start with a random valid emoji
                    nextEmoji = validEmojis.random()
                } else {
                    // Try to get the opposite.
                    nextEmoji = oppositeEmojiMap[lastEmoji] ?: validEmojis.random()
                }
                emojiChain.add(nextEmoji)
                lastEmoji = nextEmoji
            }

            val correctAnswerEmoji = oppositeEmojiMap[lastEmoji] ?: ""
            //If correct answer exists in chain, retry generation.
            if(emojiChain.contains(correctAnswerEmoji)) continue

            val choices = generateOptions(correctAnswerEmoji, availableEmojis, emojiChain, level)

            if (correctAnswerEmoji.isNotBlank() && choices.contains(correctAnswerEmoji) && choices.size >= 3) {
                return Triple(emojiChain, correctAnswerEmoji, choices)
            }
        }
        return Triple(emptyList(), "", emptyList()) // Failed to generate
    }

    private fun generateOptions(
        correctAnswerEmoji: String,
        availableEmojis: List<String>,
        emojiChain: List<String>,
        level: Int
    ): List<String> {
        val choices = mutableListOf<String>()
        if (correctAnswerEmoji.isBlank()) return choices
        choices.add(correctAnswerEmoji)

        val oppositeEmojiMap = BaseGameViewModel.oppositeEmojiMap
        val allEmojis = BaseGameViewModel.emojiCategories.values.flatMap { it.emojis }.distinct()

        // Distractors:  Not the correct answer, not in the chain, and *not* the opposite of the correct answer.
        val validDistractors = allEmojis.filterNot { emoji ->
            emoji == correctAnswerEmoji ||
                    emojiChain.contains(emoji) ||
                    oppositeEmojiMap[emoji] == correctAnswerEmoji || // Prevent showing both opposites as options
                    (oppositeEmojiMap[correctAnswerEmoji] != null && oppositeEmojiMap[correctAnswerEmoji] == emoji)
        }


        // Prioritize opposites of *other* emojis in the chain (if available) for higher levels.
        val chainOpposites = emojiChain.mapNotNull { oppositeEmojiMap[it] }.filterNot { it == correctAnswerEmoji || choices.contains(it) }

        var numDistractors = when (level) {
            1, 2 -> 2
            3, 4 -> 3
            else -> 3
        }

        var distractorChoices: List<String> = when (level) { // Assign to distractorChoices
            1, 2 -> validDistractors.shuffled().take(numDistractors)
            3, 4 -> {
                // Try to include *one* chain opposite, if possible
                val chosenChainOpposite = chainOpposites.shuffled().take(1)
                val otherDistractors = validDistractors.shuffled().take(numDistractors - chosenChainOpposite.size)
                (chosenChainOpposite + otherDistractors).distinct().take(numDistractors)
            }
            else -> {
                // Level 5+:  Try to use *mostly* chain opposites.
                val chosenChainOpposites = chainOpposites.shuffled().take(numDistractors)
                val otherDistractors = validDistractors.shuffled().take(numDistractors - chosenChainOpposites.size)
                (chosenChainOpposites + otherDistractors).distinct().take(numDistractors)
            }
        }
        //Reduce number of distractors if not enough available
        while (distractorChoices.size < numDistractors && numDistractors > 0) {
            numDistractors--
            distractorChoices = if (level > 4) {
                //Level 5+ prioritize using opposites of emojis.
                val chosenChainOpposites = chainOpposites.shuffled().take(numDistractors)
                val otherDistractors = validDistractors.shuffled().take(numDistractors - chosenChainOpposites.size)
                (chosenChainOpposites + otherDistractors).distinct().take(numDistractors) // Reassign, don't addAll
            } else {
                validDistractors.shuffled().take(numDistractors) // Reassign, don't addAll
            }
            distractorChoices = distractorChoices.take(numDistractors) // Crucial to prevent IndexOutOfBounds
        }

        choices.addAll(distractorChoices)
        return choices.shuffled()
    }
}