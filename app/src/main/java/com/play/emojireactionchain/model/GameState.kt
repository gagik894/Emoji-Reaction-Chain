package com.play.emojireactionchain.model

sealed class GameResult {
    object InProgress : GameResult()
    object Won : GameResult()
    data class Lost(val reason: LossReason) : GameResult()
    data class AdContinueOffered(val underlyingResult: GameResult) : GameResult() // New state
}

enum class LossReason {
    OutOfLives,
    TimeOut,  // Useful for timed modes
    // Add other reasons as needed, e.g., IncorrectDecoding
}

data class GameState(
    val score: Int = 0,
    val highScore: Int = 0,
    val totalQuestions: Int = 5,
    val questionNumber: Int = 0,
    val emojiChain: List<String> = emptyList(),
    val choices: List<String> = emptyList(),
    val correctAnswerEmoji: String = "",
    val isCorrectAnswer: Boolean? = null,
    val gameResult: GameResult = GameResult.InProgress, // Use the sealed class
    val lives: Int = 3, //keep it
    val rule: String? = null,
    val currentTimeBonus: Int = 0,
    val currentStreakBonus: Int = 0,
    val currentStreakCount: Int = 0,
    val gameMode: GameMode = GameMode.NORMAL
)