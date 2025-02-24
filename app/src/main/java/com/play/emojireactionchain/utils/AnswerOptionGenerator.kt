package com.play.emojireactionchain.utils

import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule

interface AnswerOptionGenerator {
    fun generateOptions(correctAnswerEmoji: String, category: EmojiCategory, rule: GameRule, emojiChain: List<String>, level: Int): List<String>
}