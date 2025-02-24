package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.BaseGameViewModel
import com.play.emojireactionchain.viewModel.EmojiCategory

class SequentialQuestionGenerator : QuestionGenerator {

    override fun generateQuestion(
        availableEmojis: List<String>,
        level: Int
    ): Triple<List<String>, String, List<String>> {

        val category = BaseGameViewModel.emojiCategories.values.random()
        val categoryEmojis = category.emojis

        if (categoryEmojis.size < 3) {  // Need at least 3 for a minimal chain
            return Triple(emptyList(), "", emptyList())
        }

        val (chainLength, step) = getChainParameters(level)
        val actualChainLength = chainLength.coerceAtMost(categoryEmojis.size - 1).coerceAtMost(5) // Max 5!

        var attempts = 0
        val maxAttempts = 20
        while (attempts < maxAttempts) {
            attempts++

            val startIndex = (0..categoryEmojis.size - 1).random()
            val emojiChain = mutableListOf<String>()
            var currentIndex = startIndex
            for (i in 0 until actualChainLength) {
                emojiChain.add(categoryEmojis[currentIndex])
                currentIndex = (currentIndex + step).mod(categoryEmojis.size)
                if (emojiChain.distinct().size != emojiChain.size) break // No duplicates
            }

            if (emojiChain.size < actualChainLength.coerceAtLeast(2)) continue // Ensure min length of 2.

            val nextIndex = (currentIndex).mod(categoryEmojis.size)
            val correctAnswerEmoji = categoryEmojis[nextIndex]
            if (emojiChain.contains(correctAnswerEmoji)) continue

            val choices = generateOptions(correctAnswerEmoji, category, emojiChain, level)

            if (correctAnswerEmoji.isNotBlank() && choices.contains(correctAnswerEmoji) && choices.size >= 3) {
                return Triple(emojiChain, correctAnswerEmoji, choices)
            }
        }
        return Triple(emptyList(), "", emptyList())
    }
    private fun getChainParameters(level: Int): Pair<Int, Int> {
        return when (level) {
            1 -> Pair(3, 1)
            2 -> Pair(4, 1)
            3 -> Pair(5, 1)
            4 -> Pair(3, 2)
            5 -> Pair(4, 2)
            6 -> Pair(5, 2)
            7 -> Pair(3, 3)
            8 -> Pair(4, 3)
            else -> Pair(5, (1..3).random()) // Always max length 5
        }
    }
    private fun generateOptions(
        correctAnswerEmoji: String,
        category: EmojiCategory,
        emojiChain: List<String>,
        level: Int
    ): List<String> {
        val choices = mutableListOf<String>()
        choices.add(correctAnswerEmoji)

        var numDistractors = when (level) {
            1, 2, 3 -> 2
            4, 5, 6 -> 3
            else -> 3
        }

        val categoryEmojis = category.emojis
        val otherCategoryEmojis = getEmojisFromOtherCategories(category)

        val sameCategoryDistractors = categoryEmojis
            .filterNot { it == correctAnswerEmoji || emojiChain.contains(it) }
            .shuffled()

        val diffCategoryDistractors = otherCategoryEmojis
            .filterNot { it == correctAnswerEmoji || emojiChain.contains(it) }
            .shuffled()

        val distractors = mutableListOf<String>()

        when {
            level <= 3 -> {
                distractors.addAll(diffCategoryDistractors.take(numDistractors))
                distractors.addAll(sameCategoryDistractors.take(numDistractors - distractors.size))
            }
            level <= 6 -> {
                distractors.addAll(sameCategoryDistractors.take(1))
                distractors.addAll(diffCategoryDistractors.take(numDistractors - 1))
                distractors.addAll(sameCategoryDistractors.take(numDistractors - distractors.size))
            }
            else -> {
                distractors.addAll(sameCategoryDistractors.take(numDistractors))
                distractors.addAll(diffCategoryDistractors.take(numDistractors - distractors.size))
            }
        }

        distractors.take(numDistractors)

        while (distractors.size < numDistractors && numDistractors > 0){
            numDistractors--
            if(level > 6){ //prioritize same category
                distractors.addAll(sameCategoryDistractors.take(numDistractors))
                distractors.addAll(diffCategoryDistractors.take(numDistractors - distractors.size))

            } else{ //prioritize diff category
                distractors.addAll(diffCategoryDistractors.take(numDistractors))
                distractors.addAll(sameCategoryDistractors.take(numDistractors - distractors.size))
            }
        }

        choices.addAll(distractors)
        return choices.shuffled()
    }

    private fun getEmojisFromOtherCategories(category: EmojiCategory): List<String> {
        return BaseGameViewModel.emojiCategories.values
            .filterNot { it.name == category.name }
            .flatMap { it.emojis }
            .distinct()
            .toList()
    }
}