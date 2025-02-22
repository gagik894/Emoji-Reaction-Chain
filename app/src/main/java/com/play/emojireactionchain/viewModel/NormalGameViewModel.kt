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

class NormalGameViewModel(
    soundManager: SoundManager,
    highScoreManager: HighScoreManager
) : BaseGameViewModel(soundManager, highScoreManager) {

    override fun startGame(gameMode: GameMode) {
        viewModelScope.launch {
            currentGameScore = 0
            currentQuestionCount = 0
            currentStreak = 0

            _gameState.value = GameState(
                score = 0,
                highScore = highScoreManager.getHighScore(),
                totalQuestions = questionCountPerGame,
                lives = 3,
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                currentStreakCount = 0
            )
            nextQuestion()
        }
    }

    override fun nextQuestion() {
        viewModelScope.launch {
            if (currentQuestionCount < questionCountPerGame) {
                currentQuestionCount++
                val ruleCategory = selectRuleAndCategory()
                val generatedChainData = generateEmojiChain(ruleCategory.category, ruleCategory.rule)

                questionStartTime = System.currentTimeMillis()

                _gameState.value = _gameState.value.copy(
                    emojiChain = generatedChainData.emojiChain,
                    choices = generatedChainData.choices,
                    correctAnswerEmoji = generatedChainData.correctAnswerEmoji,
                    isCorrectAnswer = null,
                    questionNumber = currentQuestionCount,
                    rule = ruleCategory.rule.name
                )
            } else {
                endGame(GameResult.Won)
                println("Game Won! - All questions completed")
            }
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
        println("Correct! Score: $currentGameScore, Time Bonus: $timeBonus, Streak Bonus: $streakBonus, Current Streak: $currentStreak")//keep this for debugging
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
            soundManager.playIncorrectSoundAndHaptic()
            delay(1000)
            nextQuestion()
            println("Incorrect! Lives Remaining: ${currentLives - 1}, Streak Reset!") // Print streak reset info
        } else {
            _gameState.value = _gameState.value.copy(
                isCorrectAnswer = false,
                lives = 0,
            )
            soundManager.playIncorrectSoundAndHaptic()
            delay(500)
            endGame(GameResult.Lost(LossReason.OutOfLives))
            println("Game Over! Final Score: $currentGameScore, Streak Reset!") // Print streak reset info at game over
        }
    }
}