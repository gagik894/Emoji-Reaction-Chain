package com.play.emojireactionchain.utils

interface QuestionGenerator {
    fun generateQuestion(availableEmojis: List<String>, level: Int): Triple<List<String>, String, List<String>>
}
