package com.play.emojireactionchain.utils

import com.play.emojireactionchain.model.GeneratedChainData
import com.play.emojireactionchain.viewModel.BaseGameViewModel

class OppositeMeaningChainGenerator : EmojiChainGenerator {
    override fun generateChain(availableEmojis: List<String>, level: Int): GeneratedChainData {
        val oppositeEmojiMap = BaseGameViewModel.oppositeEmojiMap
        // Use only emojis that *have* opposites
        val validEmojis = availableEmojis.filter { oppositeEmojiMap.containsKey(it) || oppositeEmojiMap.containsValue(it) }

        if (validEmojis.size < 2) {
            return  GeneratedChainData(emptyList(), emptyList(), "") // Return empty data
        }
        val chainLength = when (level) {
            1 -> 2
            2 -> 3
            3 -> 4
            else -> (4..5).random()  // Longer chains for higher levels
        }


        val emojiChain = mutableListOf<String>()
        var lastEmoji: String? = null

        repeat(chainLength) {
            val nextEmoji: String
            if (lastEmoji == null) {
                nextEmoji = validEmojis.random()
            }
            else {
                // Get the opposite. If there's no opposite for the *last* emoji,
                // this logic will now pick a *new* valid emoji that has an opposite.
                nextEmoji = oppositeEmojiMap[lastEmoji] ?: validEmojis.random()
            }
            emojiChain.add(nextEmoji)
            lastEmoji = nextEmoji
        }

        val correctAnswerEmoji = oppositeEmojiMap[lastEmoji] ?: "" // Get opposite
        val choices = mutableListOf<String>()
        if(correctAnswerEmoji.isNotBlank()){
            choices.add(correctAnswerEmoji)
        }
        return GeneratedChainData(emojiChain, choices, correctAnswerEmoji)
    }
}