package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.BaseGameViewModel

class SynonymQuestionGenerator : QuestionGenerator {

    override fun generateQuestion(
        availableEmojis: List<String>,
        level: Int
    ): Triple<List<String>, String, List<String>> {
        val synonymPairs = BaseGameViewModel.synonymPairs

        // Find valid synonym *groups* within the available emojis
        val validGroups = synonymPairs.map { group -> group.filter { availableEmojis.contains(it) } }
            .filter { it.size >= 3 } // Require at least 3 for chain + answer

        if (validGroups.isEmpty()) {
            return Triple(emptyList(), "", emptyList()) // Fallback
        }

        // Chain length capped at 5
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
            val chosenGroup = validGroups.random()
            val shuffledGroup = chosenGroup.shuffled() // Shuffle the selected group

            for (i in 0 until chainLength) {
                emojiChain.add(shuffledGroup[i % shuffledGroup.size]) // Wrap around
            }

            val correctAnswerEmoji = chosenGroup.filterNot { emojiChain.contains(it) }.randomOrNull() ?: ""

            if(correctAnswerEmoji.isBlank()) continue // If no valid correct answer, retry

            val options = generateOptions(correctAnswerEmoji, availableEmojis, emojiChain, level, chosenGroup)

            if (options.contains(correctAnswerEmoji) && options.size >= 3) {
                return Triple(emojiChain, correctAnswerEmoji, options)
            }
        }
        return Triple(emptyList(), "", emptyList()) // Indicate failure after attempts

    }

    private fun generateOptions(
        correctAnswerEmoji: String,
        availableEmojis: List<String>,
        emojiChain: List<String>,
        level: Int,
        synonymGroup: List<String>
    ): List<String> {
        val choices = mutableListOf<String>()
        if (correctAnswerEmoji.isBlank()) return choices
        choices.add(correctAnswerEmoji)

        // All emojis for wider distractor selection
        val allEmojis = BaseGameViewModel.emojiCategories.values.flatMap { it.emojis }.distinct()


        // Distractors that are NOT synonyms and are not in the chain
        val validDistractors = allEmojis.filterNot { emoji ->
            emoji == correctAnswerEmoji ||
                    emojiChain.contains(emoji) ||
                    synonymGroup.contains(emoji)
        }

        var numDistractors = when (level) {
            1, 2 -> 2
            3, 4 -> 3
            else -> 3
        }

        var distractorChoices: List<String> = when (level) {
            1, 2 -> {
                // Levels 1 & 2:  Mostly random distractors.
                validDistractors.shuffled().take(numDistractors)
            }
            3, 4 -> {
                // Level 3, 4: One synonym (if available), others random.
                val synonymDistractor = synonymGroup.filterNot { it == correctAnswerEmoji || emojiChain.contains(it) }.shuffled().take(1)
                val otherDistractors = validDistractors.shuffled().take(numDistractors - synonymDistractor.size)
                (synonymDistractor + otherDistractors).distinct().take(numDistractors)
            }
            else -> {
                // Level 5+: As many synonyms as possible.
                val synonymDistractors = synonymGroup.filterNot { it == correctAnswerEmoji || emojiChain.contains(it) }.shuffled().take(numDistractors)
                val otherDistractors = validDistractors.shuffled().take(numDistractors - synonymDistractors.size)
                (synonymDistractors + otherDistractors).distinct().take(numDistractors)
            }
        }

        while(distractorChoices.size < numDistractors && numDistractors > 0){
            numDistractors--
            distractorChoices = if(level > 4){
                //Level 5+ prioritize synonyms
                val synonymDistractors = synonymGroup.filterNot { it == correctAnswerEmoji || emojiChain.contains(it) }.shuffled().take(numDistractors)
                val otherDistractors = validDistractors.shuffled().take(numDistractors - synonymDistractors.size)
                (synonymDistractors + otherDistractors).distinct().take(numDistractors)
            }else{
                validDistractors.shuffled().take(numDistractors)
            }
            distractorChoices = distractorChoices.take(numDistractors) // Prevent exceeding limit
        }

        choices.addAll(distractorChoices)
        return choices.shuffled()
    }
}