package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.BaseGameViewModel

class MixUpQuestionGenerator : QuestionGenerator {

    // Data class for predefined chains, now including difficulty and length
    data class PredefinedChain(
        val chain: List<String>,
        val correctAnswerIndex: Int,
        val difficulty: Int, // 1 = Easy, 2 = Medium, 3 = Hard
        val length: Int      // Explicitly store the length
    )

    // List of predefined chains
    private val predefinedChains = listOf(
        // Easy (Level 1), Length 4
        PredefinedChain(listOf("🐶", "🚶", "🦴", "🏠"), 3, 1, 4),  // Dog, walk, bone, house
        PredefinedChain(listOf("⏰", "😴", "😠", "🚶"), 3, 1, 4),  // Alarm, sleepy, angry, work
        PredefinedChain(listOf("🌧️", "🚶", "😊", "☔"), 3, 1, 4),  // Rain, walk, happy, umbrella
        PredefinedChain(listOf("🍕", "😋", "🥤", "🍽️"), 3, 1, 4),  // Pizza, yummy, drink, eating
        PredefinedChain(listOf("🎉", "🎂", "😊", "🥳"), 3, 1, 4),
        PredefinedChain(listOf("👶", "🍼", "😴", "😭"), 3, 1, 4),
        PredefinedChain(listOf("🪥", "🦷", "🚰", "😁"), 3, 1, 4),
        PredefinedChain(listOf("📺", "🛋️", "🍿", "😊"), 3, 1, 4),

        // Medium (Level 2), Length 4
        PredefinedChain(listOf("🥚", "🍳", "🥓", "🍽️"), 3, 2, 4), // Egg, fried egg, bacon, plate
        PredefinedChain(listOf("🧼", "🖐️", "💧", "😊"), 3, 2, 4),  // Soap, hand, water, happy
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

        // Hard (Level 3), Length 4
        PredefinedChain(listOf("🔥", "🪵", "😊", "🏕️"), 3, 3, 4),    //fire, wood,  happy , camping
        PredefinedChain(listOf("🧽", "🍽️", "💧", "✨"), 3, 3, 4), // Sponge, plate, water, sparkling clean
        PredefinedChain(listOf("📚", "✏️", "🎓", "😴"), 3, 3, 4),  // Book, pencil, graduation, sleep(tired)
        PredefinedChain(listOf("🧑", "💻", "💼", "🏢"), 3, 3, 4),  // Man, computer, briefcase, office
        PredefinedChain(listOf("👩", "🍳", "😋", "🍽️"), 3, 3, 4),  //Woman, cooking, yummy , eating
        PredefinedChain(listOf("👧", "📚", "🎓", "📝"), 3, 3, 4),  // Girl, book, graduation, writing
        PredefinedChain(listOf("⏰", "🚗", "😠", "🏢"), 3, 3, 4),  // Alarm, car, angry, office
        PredefinedChain(listOf("🔬", "🧪", "👨‍🔬", "🥼"), 3, 3, 4), // Microscope, test tube, scientist, lab coat
        PredefinedChain(listOf("🏢", "👔", "🧑‍💼", "💼"), 3, 3, 4),   // office, tie, businessman, Briefcase.
        PredefinedChain(listOf("🚶", "⛺", "🥶", "🏔️"), 3, 3, 4), // hiking, tent, cold, mountain
        PredefinedChain(listOf("🌊", "⚓", "🏝️", "🚢"), 3, 3, 4),  // ocean, anchor, island, ship
        PredefinedChain(listOf("⚽", "🥅", "🏆", "🏃"), 3, 3, 4),      // Soccer ball, goal, trophy, running
        PredefinedChain(listOf("🏀", "⛹️", "👏", "🥅"), 3, 3, 4),  // Basketball, player, applause, net
        PredefinedChain(listOf("🏊", "🎽", "🥇", "🌊"), 3, 3, 4),    // Swimming, swimming clothes, Gold medal, water
        PredefinedChain(listOf("🚴", "🚵", "⛑️", "⛰️"), 3, 3, 4),   // Bicycle, Mountain biking, helmet, mountain
        PredefinedChain(listOf("🧘", "🕉️", "🧎", "😌"), 3, 3, 4), // Yoga, om symbol, kneeling, relaxed.
        PredefinedChain(listOf("🍇", "🧀", "🥂", "🍷"), 3, 3, 4),    // Grapes, cheese, clinking glasses, wine
        PredefinedChain(listOf("🥦", "🥕", "💪", "🥗"), 3, 3, 4),   // Broccoli, carrot, strong arm, salad
        PredefinedChain(listOf("🕯️", "🎉", "🎁", "🎂"), 3, 3, 4),  // Candles, party, presents, cake
        PredefinedChain(listOf("🍪", "😋", "😊", "🥛"), 3, 3, 4),   // Cookie, delicious, smiley, milk
        PredefinedChain(listOf("🚗", "💥", "⛽", "😓"), 3, 3, 4),  // Car, crash, gas, sweat
        PredefinedChain(listOf("💡", "😊", "🧠", "🤔"), 3, 3, 4), //lightbulb, happy, brain, thinking - Idea
        PredefinedChain(listOf("❤️", "💐", "💌", "🥰"), 3, 3, 4), // heart, flowers, love letter, in love

        // Examples with different lengths (and difficulties)
        PredefinedChain(listOf("😴", "⏰", "😠"), 2, 1, 3),   // Sleepy, alarm, angry (shorter, easy)
        PredefinedChain(listOf("🍕", "😋", "🍽️"), 2, 2, 3),   // Pizza, yummy, plate (shorter, medium)
        PredefinedChain(listOf("🇺🇸", "🗽", "✈️"), 2, 1, 3), // USA, Statue of Liberty; plane
        PredefinedChain(listOf("🇫🇷", "🗼", "✈️"), 2, 1, 3), // France, Eiffel Tower; plane
        PredefinedChain(listOf("🇯🇵", "🍣", "✈️"), 2, 1, 3), // Japan, sushi;  plane
        PredefinedChain(listOf("🇮🇹", "🍕", "✈️"), 2, 1, 3), // Italy, Pizza;  plane
        PredefinedChain(listOf("📚", "📝", "🎓"), 2, 2, 3),   //  book, writing, graduation
        PredefinedChain(listOf("👦", "⚽", "🎉"), 2, 2, 3),   //Boy, football; celebration
        PredefinedChain(listOf("👩", "🍳", "😋"), 2, 3, 3),    //Woman, cooking; yummy
        PredefinedChain(listOf("🧑", "💻", "💼"), 2, 3, 3),   // Man, computer; briefcase
        PredefinedChain(listOf("🚗", "💥", "😓"), 2, 3, 3), // Car, crash; sweat
        PredefinedChain(listOf("🎉", "🎂", "🥳"), 2, 1, 3),  // Party, cake; celebration
        PredefinedChain(listOf("☀️", "🍦", "🏖️"), 2, 1, 3), // Sun, ice cream; beach
        PredefinedChain(listOf("🌧️", "😊", "☔"), 2, 1, 3),   // Rain, walking, happy; umbrella

        PredefinedChain(listOf("🐶", "🚶", "🦴", "🏠", "🐾"), 4, 1, 5),// Dog walks home, bone, paw
        PredefinedChain(listOf("⏰", "😴", "😠", "🚶", "🏢"), 4, 2, 5), // Alarm, sleepy, angry, going, office
        PredefinedChain(listOf("🌧️", "🚶", "😊", "☔", "🚶"), 4, 1, 5), // Rain, walking, happy; umbrella, walk
        PredefinedChain(listOf("🍕", "😋", "🥤", "🍽️", "😊"), 4, 2, 5), // Pizza, yummy, drink, eat, happy
        PredefinedChain(listOf("📚", "✏️", "😴", "🎓", "🎉"), 4, 3, 5), // Book, pencil, sleep, graduation, party
        PredefinedChain(listOf("🚗", "💥", "😓", "⛽", "👨‍🔧"), 4, 3, 5), // Car, crash, sweat; gas, mechanic

        PredefinedChain(listOf("👦", "⚽", "🥅", "🎉", "🏆"), 4, 1, 5), // Easy, but longer
        PredefinedChain(listOf("👧", "📚", "📝", "🎓", "👩‍🎓"), 4, 2, 5), // Medium, longer
    )

    private val emojiCategories: Map<String, String> = BaseGameViewModel.emojiCategories.values
        .flatMap { category -> category.emojis.map { emoji -> emoji to category.name } }
        .toMap()


    override fun generateQuestion(
        availableEmojis: List<String>,
        level: Int
    ): Triple<List<String>, String, List<String>> {

        var attempts = 0
        val maxAttempts = 10

        while (attempts < maxAttempts) {
            // Filter chains by difficulty
            val suitableChains = predefinedChains.filter { it.difficulty <= level }

            if (suitableChains.isEmpty()) {
                attempts++
                continue
            }

            val selectedChain = suitableChains.random()

            // Enforce maximum *question* chain length (excluding the answer)
            val maxQuestionChainLength = 4  // Max 4 emojis in the question
            val selectedChainLength = selectedChain.chain.size

            // Check if removing the answer emoji will result in a chain that is too long
            if (selectedChainLength -1 > maxQuestionChainLength){
                attempts++
                continue
            }


            val questionChain = selectedChain.chain.toMutableList()
            val correctAnswerEmoji = questionChain.removeAt(selectedChain.correctAnswerIndex)

            if (!availableEmojis.contains(correctAnswerEmoji)) {
                attempts++
                continue
            }

            val options = generateOptions(correctAnswerEmoji, questionChain, level, availableEmojis)

            // Now check if we have enough options *and* the correct answer
            if (options.size >= 3 && options.contains(correctAnswerEmoji)) {
                if(questionChain.size == selectedChain.length -1) { //Length check.
                    return Triple(questionChain, correctAnswerEmoji, options)
                } else{
                    attempts++
                    continue
                }
            }
            attempts++
        }

        return Triple(emptyList(), "", emptyList()) // Indicate failure
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

        // All emojis for wider distractor selection
        val allEmojis = availableEmojis.distinct().ifEmpty {
            BaseGameViewModel.emojiCategories.values.flatMap { it.emojis }.distinct()
        }

        val unrelatedDistractors = allEmojis.filterNot {
            it == correctAnswerEmoji || emojiChain.contains(it)
        }

        var numDistractors = when (level) {
            1, 2, 3 -> 2
            4, 5, 6 -> 3
            else -> 3
        }

        var distractorChoices: List<String> =  if (level >= 2) {
            // Find related distractors based on category
            val chainCategories = emojiChain.mapNotNull { emojiCategories[it] }.toSet()
            val relatedDistractors = allEmojis.filter { emoji ->
                emojiCategories[emoji] in chainCategories && emoji != correctAnswerEmoji && !emojiChain.contains(emoji)
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

            distractorChoices = if(level > 3){
                val chainCategories = emojiChain.mapNotNull { emojiCategories[it] }.toSet()
                val relatedDistractors = allEmojis.filter { emoji ->
                    emojiCategories[emoji] in chainCategories && emoji != correctAnswerEmoji && !emojiChain.contains(emoji)
                }

                if (relatedDistractors.isNotEmpty()) {
                    relatedDistractors.shuffled().take(numDistractors)
                } else {
                    unrelatedDistractors.shuffled().take(numDistractors)
                }
            }else{
                unrelatedDistractors.shuffled().take(numDistractors)
            }
            distractorChoices = distractorChoices.take(numDistractors) // Prevent exceeding limit
        }
        choices.addAll(distractorChoices)
        return choices.shuffled()
    }
}