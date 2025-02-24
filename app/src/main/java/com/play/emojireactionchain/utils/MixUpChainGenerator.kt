package com.play.emojireactionchain.utils

import com.play.emojireactionchain.model.GeneratedChainData

class MixUpChainGenerator : EmojiChainGenerator {

    // Define relationships between emojis.  This is a simplified example;
    // you'll need to expand this significantly.  Use a Map<String, List<Pair<String, String>>>
    // where:
    //   - Key: The starting emoji (String)
    //   - Value: A list of Pairs.  Each Pair represents a relationship:
    //      - Pair.first: The related emoji (String)
    //      - Pair.second: The relationship type (String, e.g., "Cause", "Sequence")
    private val emojiRelationships: Map<String, List<Pair<String, String>>> = mapOf(
        "🌧️" to listOf("🌈" to "Cause", "☔" to "Use", "⚡" to "Cause"), // Rain
        "🌈" to listOf("☀️" to "After", "😊" to "Effect"), // Rainbow
        "☀️" to listOf("😎" to "Use", "🏖️" to "Location", "🥵" to "Cause"),  // Sun
        "⏰" to listOf("🚶" to "Sequence", "😴" to "Before", "😠" to "Effect"), // Alarm Clock
        "🚶" to listOf("☕" to "Sequence", "🏢" to "Sequence"), // Walking
        "☕" to listOf("😊" to "Effect", "🍩" to "With"), // Coffee
        "🚗" to listOf("⛽" to "Use", "🛣️" to "Location", "💥" to "Cause"), // Car
        "⛽" to listOf("💰" to "Requires", "🚗" to "For"), // Gas
        "🏖️" to listOf("🏐" to "Activity", "☀️" to "Cause", "😎" to "Use"), // Beach
        "🥚" to listOf("🐣" to "Becomes", "🍳" to "Use"), // Egg
        "🐣" to listOf("🐔" to "Becomes"), // Chick
        "🍳" to listOf("🍽️" to "Sequence", "😋" to "Effect"), // Fried Egg
        "😓" to listOf("🥤" to "Solution", "🥵" to "Cause"), // Tired/Sweating
        "🥤" to listOf("😊" to "Effect"), // Drink
        "🍕" to listOf("😋" to "Effect", "🍽️" to "Use"), // Pizza
        "🎉" to listOf("🥳" to "Synonym", "🎂" to "Cause"),
        "📚" to listOf("✏️" to "Use", "🎓" to "Result"),
        "✏️" to listOf("📝" to "Use"),
    )

    override fun generateChain(availableEmojis: List<String>, level: Int): GeneratedChainData {
        val chainLength = when (level) {
            1 -> 3
            2 -> 4
            3 -> 5
            else -> (5..7).random()
        }

        val validStartingEmojis = availableEmojis.filter { emojiRelationships.containsKey(it) }
        if (validStartingEmojis.isEmpty()) {
            return GeneratedChainData(emptyList(), emptyList(), "") // Fallback: No valid chain
        }

        val emojiChain = mutableListOf<String>()
        var currentEmoji = validStartingEmojis.random()
        emojiChain.add(currentEmoji)

        repeat(chainLength - 1) {
            val relatedEmojis = emojiRelationships[currentEmoji]?.map { it.first } ?: emptyList()
            val validNextEmojis =
                relatedEmojis.filter { availableEmojis.contains(it) && !emojiChain.contains(it) } // Available and not already in chain

            if (validNextEmojis.isNotEmpty()) {
                val nextEmoji = validNextEmojis.random()
                emojiChain.add(nextEmoji)
                currentEmoji = nextEmoji
            } else {
                return GeneratedChainData(
                    emptyList(),
                    emptyList(),
                    ""
                ) // Fallback if chain can't continue
            }
        }

        // Find a correct answer (related to the last emoji)
        val lastEmoji = emojiChain.last()
        val correctAnswerOptions = emojiRelationships[lastEmoji]?.map { it.first } ?: emptyList()
        val validCorrectAnswer =
            correctAnswerOptions.filter { availableEmojis.contains(it) && !emojiChain.contains(it) }
                .randomOrNull() ?: ""

        val choices = mutableListOf<String>()
        if (validCorrectAnswer.isNotBlank()) {
            choices.add(validCorrectAnswer)
        }

        return GeneratedChainData(emojiChain, choices, validCorrectAnswer)
    }
}