package com.play.emojireactionchain.model // <-- Package is now model

data class GeneratedChainData(
    val emojiChain: List<String>,
    val choices: List<String>,
    val correctAnswerEmoji: String
)