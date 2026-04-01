package com.play.emojireactionchain.viewModel

import androidx.lifecycle.viewModelScope
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.LossReason
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.QuestionGenerator
import com.play.emojireactionchain.utils.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NormalGameViewModel(
    soundManager: SoundManager,
    highScoreManager: HighScoreManager
) : BaseGameViewModel(soundManager, highScoreManager) {

    init {
        loadHighScore(GameMode.NORMAL) // Load at init
    }

    override fun startGame() {
        viewModelScope.launch {
            currentGameScore = 0
            currentQuestionCount = 0
            currentStreak = 0
            // No need to call loadHighScore here, it's done in init
            _gameState.value = _gameState.value.copy(
                totalQuestions = questionCountPerGame, // Use base class property
                lives = 3,
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                currentStreakCount = 0,
                gameMode = GameMode.NORMAL // Set GameMode directly
            )
            nextQuestion()
        }
    }

    override fun generateQuestionData(level: Int): Triple<List<String>, String, List<String>> {
        val ruleCategory = selectRuleAndCategory(level)
        val category = ruleCategory.category
        val rule = ruleCategory.rule
        val availableEmojis = if (rule.name == "Category Mix-Up") {
            emojiCategories.values.flatMap { it.emojis }.distinct()
        } else {
            category.emojis
        }

        val questionGenerator: QuestionGenerator = getQuestionGenerator(rule.name)
        return questionGenerator.generateQuestion(availableEmojis, level)
    }
    override fun handleNextQuestionModeSpecific() {
        if (currentQuestionCount > questionCountPerGame) {
            endGame(GameResult.Won)
        }
    }
    override suspend fun handleCorrectChoice() {
        val answerTimeMillis = System.currentTimeMillis() - questionStartTime
        val answerTimeSeconds = answerTimeMillis / 1000.0
        val remainingTimeSeconds = (maxTimePerQuestionSeconds - answerTimeSeconds).coerceAtLeast(0.0)

        val timeBonus = (remainingTimeSeconds * pointsPerSecondBonus).toInt()
        currentGameScore += timeBonus

        currentStreak++
        var streakBonus = 0
        if (currentStreak >= streakBonusThreshold) {
            streakBonus = streakBonusPoints
            currentGameScore += streakBonus
        }
        currentGameScore++ // Add base score

        _gameState.value = _gameState.value.copy(
            score = currentGameScore,
            isCorrectAnswer = true,
            currentTimeBonus = timeBonus,
            currentStreakBonus = streakBonus,
            currentStreakCount = currentStreak
        )

        soundManager.playCorrectSound()
        soundManager.playCorrectHaptic()
        delay(500)
        nextQuestion()
    }


    override suspend fun handleIncorrectChoice() {
        currentStreak = 0 // Reset streak

        val currentLives = _gameState.value.lives
        if (currentLives > 1) {
            _gameState.value = _gameState.value.copy(
                isCorrectAnswer = false,
                lives = currentLives - 1,
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                currentStreakCount = currentStreak
            )
            soundManager.playIncorrectSound()
            delay(150)
            soundManager.playIncorrectHaptic()
            delay(1000)
            nextQuestion()
        } else {
            _gameState.value = _gameState.value.copy(
                isCorrectAnswer = false,
                lives = 0,
            )
            soundManager.playIncorrectSound()
            soundManager.playIncorrectHaptic()
            delay(500)
            endGame(GameResult.Lost(LossReason.OutOfLives))
        }
    }
    override fun handleAdReward() {
        _gameState.value = _gameState.value.copy(
            lives = 3,
            gameResult = GameResult.InProgress
        )
        nextQuestion()
    }
}