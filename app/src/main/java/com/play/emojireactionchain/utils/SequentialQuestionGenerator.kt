package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.BaseGameViewModel

class SequentialQuestionGenerator : QuestionGenerator {

    override fun generateQuestion(
        availableEmojis: List<String>,
        level: Int
    ): Triple<List<String>, String, List<String>> {

        val categoryEmojis = if (availableEmojis.size >= 3) {
            availableEmojis.distinct()
        } else {
            BaseGameViewModel.emojiCategories.values.random().emojis
        }

        if (categoryEmojis.size < 3) {  // Need at least 3 for a minimal chain
            return Triple(emptyList(), "", emptyList())
        }

        val (chainLength, step) = getChainParameters(level)
        val actualChainLength = chainLength.coerceAtMost(categoryEmojis.size - 1).coerceAtMost(5) // Max 5!
        val effectiveStep = resolveStep(categoryEmojis.size, step, actualChainLength) ?: return Triple(emptyList(), "", emptyList())

        var attempts = 0
        val maxAttempts = 20
        while (attempts < maxAttempts) {
            attempts++

            val startIndex = (0..<categoryEmojis.size).random()
            val emojiChain = mutableListOf<String>()
            var currentIndex = startIndex
            while (emojiChain.size < actualChainLength) {
                emojiChain.add(categoryEmojis[currentIndex])
                currentIndex = (currentIndex + effectiveStep).mod(categoryEmojis.size)
                if (emojiChain.distinct().size != emojiChain.size) break // No duplicates
            }

            if (emojiChain.size < actualChainLength.coerceAtLeast(2)) continue // Ensure min length of 2.

            val nextIndex = (currentIndex).mod(categoryEmojis.size)
            val correctAnswerEmoji = categoryEmojis[nextIndex]
            if (emojiChain.contains(correctAnswerEmoji)) continue

            val choices = generateOptions(correctAnswerEmoji, emojiChain, level, categoryEmojis)

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

    private fun resolveStep(size: Int, preferredStep: Int, chainLength: Int): Int? {
        val candidates = listOf(preferredStep, 1, 2, 3).distinct()
        return candidates.firstOrNull { step ->
            val normalized = step.mod(size).let { if (it == 0) 1 else it }
            val cycleLength = size / gcd(size, normalized)
            cycleLength > chainLength
        }
    }

    private fun gcd(a: Int, b: Int): Int {
        var x = a
        var y = b
        while (y != 0) {
            val temp = x % y
            x = y
            y = temp
        }
        return x
    }

    private fun generateOptions(
        correctAnswerEmoji: String,
        emojiChain: List<String>,
        level: Int,
        categoryEmojis: List<String>
    ): List<String> {
        val targetDistractors = when (level) {
            1, 2, 3 -> 2
            4, 5, 6 -> 3
            else -> 3
        }

        val allEmojis = BaseGameViewModel.emojiCategories.values.flatMap { it.emojis }.distinct()

        val sameCategoryDistractors = categoryEmojis
            .filterNot { it == correctAnswerEmoji || emojiChain.contains(it) }
            .shuffled()

        val diffCategoryDistractors = allEmojis
            .filterNot { it == correctAnswerEmoji || emojiChain.contains(it) }
            .shuffled()

        val prioritized = when {
            level <= 3 -> {
                diffCategoryDistractors + sameCategoryDistractors
            }
            level <= 6 -> {
                sameCategoryDistractors.take(1) + diffCategoryDistractors + sameCategoryDistractors.drop(1)
            }
            else -> {
                sameCategoryDistractors + diffCategoryDistractors
            }
        }

        val distractors = prioritized
            .distinct()
            .filterNot { it == correctAnswerEmoji || emojiChain.contains(it) }
            .take(targetDistractors)

        val fallback = (sameCategoryDistractors + diffCategoryDistractors)
            .distinct()
            .filterNot { it == correctAnswerEmoji || emojiChain.contains(it) || distractors.contains(it) }

        val finalDistractors = (distractors + fallback)
            .take(targetDistractors)

        return (listOf(correctAnswerEmoji) + finalDistractors).shuffled()
    }
}