package com.play.emojireactionchain.viewModel

import androidx.lifecycle.viewModelScope
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.GameState
import com.play.emojireactionchain.model.LossReason
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SurvivalGameViewModel(
    soundManager: SoundManager,
    highScoreManager: HighScoreManager
) : BaseGameViewModel(soundManager, highScoreManager) {

    override val questionCountPerGame = Int.MAX_VALUE // Infinite questions
    var questionLevel: Int = 1  // Make questionLevel mutable
    private var lives: Int = 3 // Add lives

    init {
        loadHighScore(GameMode.SURVIVAL)
    }

    override fun startGame() {
        viewModelScope.launch {
            currentGameScore = 0
            currentQuestionCount = 0
            currentStreak = 0
            questionLevel = 1
            lives = 3 // Reset lives

            _gameState.value = GameState(
                score = 0,
                highScore = highScoreManager.getHighScore(GameMode.SURVIVAL),
                totalQuestions = questionCountPerGame,
                lives = lives, // Use the lives variable
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                currentStreakCount = 0,
                gameMode = GameMode.SURVIVAL,
                gameResult = GameResult.InProgress
            )
            nextQuestion()
        }
    }

    override fun nextQuestion() {
        viewModelScope.launch {
            currentQuestionCount++

            // Increase difficulty based on score (example)
            questionLevel = when {
                currentGameScore > 500 -> 4
                currentGameScore > 250 -> 3
                currentGameScore > 100 -> 2
                else -> 1
            }

            val (emojis, correctAnswer, choices) = generateQuestionData()  // Use generateQuestionData

            questionStartTime = System.currentTimeMillis()

            _gameState.value = _gameState.value.copy(
                emojiChain = emojis, // Use values from generateQuestionData
                choices = choices.shuffled(), // Shuffle choices here
                correctAnswerEmoji = correctAnswer, // Use values from generateQuestionData
                isCorrectAnswer = null,
                questionNumber = currentQuestionCount,
                rule = null, // Or set a relevant rule if you want to display it
                gameResult = GameResult.InProgress,
                lives = lives
            )
        }
    }

    private fun generateQuestionData(): Triple<List<String>, String, List<String>> {
        val ruleCategory = selectRuleAndCategory()
        val generatedChainData = generateEmojiChain(ruleCategory.category, ruleCategory.rule, questionLevel)
        val options = generateAnswerOptions(generatedChainData.correctAnswerEmoji,ruleCategory.category, ruleCategory.rule, generatedChainData.emojiChain)

        return Triple(generatedChainData.emojiChain, generatedChainData.correctAnswerEmoji, options)
    }

    override suspend fun handleCorrectChoice() {

        val answerTimeMillis = System.currentTimeMillis() - questionStartTime
        val timeBonus = 0 // No time bonus in this version
        currentGameScore += timeBonus

        currentStreak++
        var streakBonus = 0
        if (currentStreak >= streakBonusThreshold) {
            streakBonus = streakBonusPoints * currentStreak
            currentGameScore += streakBonus
        }
        currentGameScore++

        _gameState.value = _gameState.value.copy(
            score = currentGameScore,
            isCorrectAnswer = true,
            currentTimeBonus = timeBonus,
            currentStreakBonus = streakBonus,
            currentStreakCount = currentStreak,
            gameResult = GameResult.InProgress
        )

        soundManager.playCorrectSound()
        soundManager.playCorrectHaptic()
        delay(300) // Short delay
        nextQuestion()
    }

    override suspend fun handleIncorrectChoice() {
        currentStreak = 0
        lives-- // Decrease lives

        if (lives <= 0) {
            endGame(GameResult.Lost(LossReason.OutOfLives))
        } else {
            _gameState.value = _gameState.value.copy(
                isCorrectAnswer = false,
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                currentStreakCount = currentStreak,
                lives = lives, // Update lives in GameState
                gameResult = GameResult.InProgress // Stay in progress
            )
            soundManager.playIncorrectSound()
            soundManager.playIncorrectHaptic()
            delay(500)
            nextQuestion()

        }
    }
}