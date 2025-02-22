package com.play.emojireactionchain.utils

import com.play.emojireactionchain.model.GeneratedChainData
import com.play.emojireactionchain.viewModel.EmojiCategory
import com.play.emojireactionchain.viewModel.GameRule

interface EmojiChainGenerator {
    fun generateChain(category: EmojiCategory, rule: GameRule): GeneratedChainData
}