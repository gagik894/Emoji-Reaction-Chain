package com.play.emojireactionchain.utils

import com.play.emojireactionchain.model.GeneratedChainData
import com.play.emojireactionchain.viewModel.BaseGameViewModel

class SynonymChainGenerator : EmojiChainGenerator {

    override fun generateChain(availableEmojis: List<String>, level: Int): GeneratedChainData {
        val synonymPairs = BaseGameViewModel.synonymPairs

        // Find valid synonym *groups* within the available emojis
        val validGroups =
            synonymPairs.map { group -> group.filter { availableEmojis.contains(it) } }
                .filter { it.size >= 2 } // Ensure at least 2 synonyms in the group

        if (validGroups.isEmpty()) {
            return GeneratedChainData(emptyList(), emptyList(), "")
        }

        val chainLength = when (level) {
            1 -> 2
            2 -> 3
            3 -> 4
            else -> (3..4).random()
        }

        val emojiChain = mutableListOf<String>()
        // Instead of taking random pairs, we'll select ONE group and build the chain from it.
        val chosenGroup = validGroups.random()
        val shuffledGroup = chosenGroup.shuffled() // Shuffle the selected group

        for (i in 0 until chainLength) {
            emojiChain.add(shuffledGroup[i % shuffledGroup.size]) // Use modulo to wrap around
        }
        //The correct answer will be another emoji of that group which is not in chain.
        val correctAnswerEmoji =
            chosenGroup.filterNot { emojiChain.contains(it) }.randomOrNull() ?: ""

        val choices = mutableListOf<String>()
        if (correctAnswerEmoji.isNotBlank()) {
            choices.add(correctAnswerEmoji)
        }

        return GeneratedChainData(emojiChain, choices, correctAnswerEmoji)
    }
}