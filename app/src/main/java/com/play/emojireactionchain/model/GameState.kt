package com.play.emojireactionchain.model

data class GameState(
    val score: Int = 0,
    val highScore: Int = 0,
    val totalQuestions: Int = 5,
    val questionNumber: Int = 0,
    val emojiChain: List<String> = emptyList(),
    val choices: List<String> = emptyList(),
    val correctAnswerEmoji: String = "",
    val isCorrectAnswer: Boolean? = null, // Null means question is not yet answered, true/false after answer
    val isGameOver: Boolean = false,
    val lives: Int = 3,
    val rule: String = "", // Rule for the current question (e.g., "Sequential")
    val currentTimeBonus: Int = 0, // Time bonus awarded on the last correct answer
    val currentStreakBonus: Int = 0, // Streak bonus awarded on the last correct answer
    val currentStreakCount: Int = 0 // Current streak of correct answers
)