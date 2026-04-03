package com.play.emojireactionchain.utils

import com.play.emojireactionchain.model.EmojiData

class MixUpQuestionGenerator : QuestionGenerator {

    data class PredefinedChain(
        val chain: List<String>,
        val correctAnswerIndex: Int,
        val difficulty: Int,
        val length: Int
    )

    private val predefinedChains = listOf(
        PredefinedChain(listOf("🐶", "🚶", "🦴", "🏠"), 3, 1, 4),
        PredefinedChain(listOf("⏰", "😴", "😠", "🚶"), 3, 1, 4),
        PredefinedChain(listOf("🌧️", "🚶", "😊", "☔"), 3, 1, 4),
        PredefinedChain(listOf("🍕", "😋", "🥤", "🍽️"), 3, 1, 4),
        PredefinedChain(listOf("🎉", "🎂", "😊", "🥳"), 3, 1, 4),
        PredefinedChain(listOf("👶", "🍼", "😴", "😭"), 3, 1, 4),
        PredefinedChain(listOf("🪥", "🦷", "🚰", "😁"), 3, 1, 4),
        PredefinedChain(listOf("📺", "🛋️", "🍿", "😊"), 3, 1, 4),
        PredefinedChain(listOf("🥚", "🍳", "🥓", "🍽️"), 3, 2, 4),
        PredefinedChain(listOf("🧼", "🖐️", "💧", "😊"), 3, 2, 4),
        PredefinedChain(listOf("🧺", "👕", "☀️", "🧽"), 3, 2, 4),
        PredefinedChain(listOf("☕", "😊", "🌅", "📰"), 3, 2, 4),
        PredefinedChain(listOf("🌙", "😴", "⭐", "🛌"), 3, 2, 4),
        PredefinedChain(listOf("🚿", "🧼", "🧖", "😊"), 3, 2, 4),
        PredefinedChain(listOf("🪞", "😀", "👀", "💄"), 3, 2, 4),
        PredefinedChain(listOf("🍎", "👩‍🏫", "🏫", "📚"), 3, 2, 4),
        PredefinedChain(listOf("🚌", "👦", "👧", "🏫"), 3, 2, 4),
        PredefinedChain(listOf("📝", "🧑‍🎓", "🎉", "🎓"), 3, 2, 4),
        PredefinedChain(listOf("💻", "⌨️", "🖥️", "🖱️"), 3, 2, 4),
        PredefinedChain(listOf("🌍", "🧳", "🗺️", "✈️"), 3, 2, 4),
        PredefinedChain(listOf("☀️", "😎", "🍹", "🏖️"), 3, 2, 4),
        PredefinedChain(listOf("🔥", "🪵", "😊", "🏕️"), 3, 3, 4),
        PredefinedChain(listOf("🧽", "🍽️", "💧", "✨"), 3, 3, 4),
        PredefinedChain(listOf("📚", "✏️", "🎓", "😴"), 3, 3, 4),
        PredefinedChain(listOf("🧑", "💻", "💼", "🏢"), 3, 3, 4),
        PredefinedChain(listOf("👩", "🍳", "😋", "🍽️"), 3, 3, 4),
        PredefinedChain(listOf("👧", "📚", "🎓", "📝"), 3, 3, 4),
        PredefinedChain(listOf("⏰", "🚗", "😠", "🏢"), 3, 3, 4),
        PredefinedChain(listOf("🔬", "🧪", "👨‍🔬", "🥼"), 3, 3, 4),
        PredefinedChain(listOf("🏢", "👔", "🧑‍💼", "💼"), 3, 3, 4),
        PredefinedChain(listOf("🚶", "⛺", "🥶", "🏔️"), 3, 3, 4),
        PredefinedChain(listOf("🌊", "⚓", "🏝️", "🚢"), 3, 3, 4),
        PredefinedChain(listOf("⚽", "🥅", "🏆", "🏃"), 3, 3, 4),
        PredefinedChain(listOf("🏀", "⛹️", "👏", "🥅"), 3, 3, 4),
        PredefinedChain(listOf("🏊", "🎽", "🥇", "🌊"), 3, 3, 4),
        PredefinedChain(listOf("🚴", "🚵", "⛑️", "⛰️"), 3, 3, 4),
        PredefinedChain(listOf("🧘", "🕉️", "🧎", "😌"), 3, 3, 4),
        PredefinedChain(listOf("🍇", "🧀", "🥂", "🍷"), 3, 3, 4),
        PredefinedChain(listOf("🥦", "🥕", "💪", "🥗"), 3, 3, 4),
        PredefinedChain(listOf("🕯️", "🎉", "🎁", "🎂"), 3, 3, 4),
        PredefinedChain(listOf("🍪", "😋", "😊", "🥛"), 3, 3, 4),
        PredefinedChain(listOf("🚗", "💥", "⛽", "😓"), 3, 3, 4),
        PredefinedChain(listOf("💡", "😊", "🧠", "🤔"), 3, 3, 4),
        PredefinedChain(listOf("❤️", "💐", "💌", "🥰"), 3, 3, 4),
        PredefinedChain(listOf("😴", "⏰", "😠"), 2, 1, 3),
        PredefinedChain(listOf("🍕", "😋", "🍽️"), 2, 2, 3),
        PredefinedChain(listOf("🇺🇸", "🗽", "✈️"), 2, 1, 3),
        PredefinedChain(listOf("🇫🇷", "🗼", "✈️"), 2, 1, 3),
        PredefinedChain(listOf("🇯🇵", "🍣", "✈️"), 2, 1, 3),
        PredefinedChain(listOf("🇮🇹", "🍕", "✈️"), 2, 1, 3),
        PredefinedChain(listOf("📚", "📝", "🎓"), 2, 2, 3),
        PredefinedChain(listOf("👦", "⚽", "🎉"), 2, 2, 3),
        PredefinedChain(listOf("👩", "🍳", "😋"), 2, 3, 3),
        PredefinedChain(listOf("🧑", "💻", "💼"), 2, 3, 3),
        PredefinedChain(listOf("🚗", "💥", "😓"), 2, 3, 3),
        PredefinedChain(listOf("🎉", "🎂", "🥳"), 2, 1, 3),
        PredefinedChain(listOf("☀️", "🍦", "🏖️"), 2, 1, 3),
        PredefinedChain(listOf("🌧️", "😊", "☔"), 2, 1, 3),
        PredefinedChain(listOf("🐶", "🚶", "🦴", "🏠", "🐾"), 4, 1, 5),
        PredefinedChain(listOf("⏰", "😴", "😠", "🚶", "🏢"), 4, 2, 5),
        PredefinedChain(listOf("🌧️", "🚶", "😊", "☔", "🚶"), 4, 1, 5),
        PredefinedChain(listOf("🍕", "😋", "🥤", "🍽️", "😊"), 4, 2, 5),
        PredefinedChain(listOf("📚", "✏️", "😴", "🎓", "🎉"), 4, 3, 5),
        PredefinedChain(listOf("🚗", "💥", "😓", "⛽", "👨‍🔧"), 4, 3, 5),
        PredefinedChain(listOf("👦", "⚽", "🥅", "🎉", "🏆"), 4, 1, 5),
        PredefinedChain(listOf("👧", "📚", "📝", "🎓", "👩‍🎓"), 4, 2, 5)
    )

    private val emojiToCategoryMap: Map<String, String> = EmojiData.categories
        .flatMap { category -> category.emojis.map { emoji -> emoji to category.name } }
        .associate { it.first to it.second }


    override fun generateQuestion(
        availableEmojis: List<String>,
        level: Int
    ): Triple<List<String>, String, List<String>> {

        var attempts = 0
        val maxAttempts = 10

        while (attempts < maxAttempts) {
            val suitableChains = predefinedChains.filter { it.difficulty <= level }

            if (suitableChains.isEmpty()) {
                attempts++
                continue
            }

            val selectedChain = suitableChains.random()
            val maxQuestionChainLength = 4
            val selectedChainLength = selectedChain.chain.size

            if (selectedChainLength - 1 > maxQuestionChainLength) {
                attempts++
                continue
            }

            val questionChain = selectedChain.chain.toMutableList()
            val correctAnswerEmoji = questionChain.removeAt(selectedChain.correctAnswerIndex)

            if (questionChain.contains(correctAnswerEmoji)) {
                attempts++
                continue
            }

            if (!availableEmojis.contains(correctAnswerEmoji)) {
                attempts++
                continue
            }

            val options = generateOptions(correctAnswerEmoji, questionChain, level, availableEmojis)

            if (options.size >= 3 && options.contains(correctAnswerEmoji)) {
                if (questionChain.size == selectedChain.length - 1) {
                    return Triple(questionChain, correctAnswerEmoji, options)
                } else {
                    attempts++
                    continue
                }
            }
            attempts++
        }

        return Triple(emptyList(), "", emptyList())
    }

    private fun generateOptions(
        correctAnswerEmoji: String,
        emojiChain: List<String>,
        level: Int,
        availableEmojis: List<String>
    ): List<String> {

        val choices = mutableListOf<String>()
        if (correctAnswerEmoji.isBlank()) return choices
        choices.add(correctAnswerEmoji)

        val allEmojis = availableEmojis.distinct().ifEmpty {
            EmojiData.categories.flatMap { it.emojis }.distinct()
        }

        val unrelatedDistractors = allEmojis.filterNot {
            it == correctAnswerEmoji || emojiChain.contains(it)
        }

        var numDistractors = when (level) {
            1, 2, 3 -> 2
            4, 5, 6 -> 3
            else -> 3
        }

        var distractorChoices: List<String> = if (level >= 2) {
            val chainCategories = emojiChain.mapNotNull { emojiToCategoryMap[it] }.toSet()
            val relatedDistractors = allEmojis.filter { emoji ->
                emojiToCategoryMap[emoji] in chainCategories && emoji != correctAnswerEmoji && !emojiChain.contains(emoji)
            }

            if (relatedDistractors.isNotEmpty()) {
                relatedDistractors.shuffled().take(numDistractors)
            } else {
                unrelatedDistractors.shuffled().take(numDistractors)
            }
        } else {
            unrelatedDistractors.shuffled().take(numDistractors)
        }

        while (distractorChoices.size < numDistractors) {
            numDistractors--

            distractorChoices = if (level > 3) {
                val chainCategories = emojiChain.mapNotNull { emojiToCategoryMap[it] }.toSet()
                val relatedDistractors = allEmojis.filter { emoji ->
                    emojiToCategoryMap[emoji] in chainCategories && emoji != correctAnswerEmoji && !emojiChain.contains(emoji)
                }

                if (relatedDistractors.isNotEmpty()) {
                    relatedDistractors.shuffled().take(numDistractors)
                } else {
                    unrelatedDistractors.shuffled().take(numDistractors)
                }
            } else {
                unrelatedDistractors.shuffled().take(numDistractors)
            }
            distractorChoices = distractorChoices.take(numDistractors)
        }
        choices.addAll(distractorChoices)
        return choices.shuffled()
    }
}
