package com.play.emojireactionchain.utils

import com.play.emojireactionchain.model.GeneratedChainData

interface EmojiChainGenerator {
    fun generateChain(availableEmojis: List<String>, level: Int): GeneratedChainData
}