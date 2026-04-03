package com.play.emojireactionchain.viewModel

import com.play.emojireactionchain.model.EmojiData
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.GameRule
import com.play.emojireactionchain.model.LossReason
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.QuestionGenerator
import com.play.emojireactionchain.utils.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update

class NormalGameViewModel(
    soundManager: SoundManager,
    highScoreManager: HighScoreManager
) : BaseGameViewModel(soundManager, highScoreManager) {

    init {
        loadHighScore(GameMode.NORMAL)
    }

    override fun startGame() {
        _gameState.update { state ->
            state.copy(
                score = 0,
                questionNumber = 0,
                currentStreakCount = 0,
                totalQuestions = questionCountPerGame,
                lives = 3,
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                gameMode = GameMode.NORMAL,
                gameResult = GameResult.InProgress
            )
        }
        resetEngagementLayer()
        nextQuestion()
    }

    override fun generateQuestionData(level: Int): Triple<List<String>, String, List<String>> {
        val ruleCategory = selectRuleAndCategory(level)
        val category = ruleCategory.category
        val rule = ruleCategory.rule
        
        val availableEmojis = if (rule == GameRule.MIX_UP) {
            EmojiData.categories.flatMap { it.emojis }.distinct()
        } else {
            category.emojis
        }

        val questionGenerator: QuestionGenerator = getQuestionGenerator(rule)
        return questionGenerator.generateQuestion(availableEmojis, level)
    }

    override fun handleNextQuestionModeSpecific() {
        if (_gameState.value.questionNumber >= questionCountPerGame) {
            endGame(GameResult.Won)
        }
    }

    override suspend fun handleCorrectChoice() {
        val answerTimeMillis = System.currentTimeMillis() - questionStartTime
        val answerTimeSeconds = answerTimeMillis / 1000.0
        val remainingTimeSeconds = (maxTimePerQuestionSeconds - answerTimeSeconds).coerceAtLeast(0.0)

        val timeBonus = (remainingTimeSeconds * pointsPerSecondBonus).toInt()
        
        _gameState.update { state ->
            val newStreak = state.currentStreakCount + 1
            var streakBonus = 0
            if (newStreak >= streakBonusThreshold) {
                streakBonus = streakBonusPoints
            }
            
            val newScore = state.score + timeBonus + streakBonus + 1
            
            state.copy(
                score = newScore,
                isCorrectAnswer = true,
                currentTimeBonus = timeBonus,
                currentStreakBonus = streakBonus,
                currentStreakCount = newStreak
            )
        }

        soundManager.playCorrectSound()
        soundManager.playCorrectHaptic()
        delay(500)
        nextQuestion()
    }

    override suspend fun handleIncorrectChoice() {
        _gameState.update { it.copy(currentStreakCount = 0) }

        val currentLives = _gameState.value.lives
        if (currentLives > 1) {
            _gameState.update { state ->
                state.copy(
                    isCorrectAnswer = false,
                    lives = currentLives - 1,
                    currentTimeBonus = 0,
                    currentStreakBonus = 0
                )
            }
            soundManager.playIncorrectSound()
            delay(150)
            soundManager.playIncorrectHaptic()
            delay(1000)
            nextQuestion()
        } else {
            _gameState.update { state ->
                state.copy(
                    isCorrectAnswer = false,
                    lives = 0,
                )
            }
            soundManager.playIncorrectSound()
            soundManager.playIncorrectHaptic()
            delay(500)
            endGame(GameResult.Lost(LossReason.OutOfLives))
        }
    }

    override fun handleAdReward() {
        _gameState.update {
            it.copy(
                lives = 3,
                gameResult = GameResult.InProgress
            )
        }
        nextQuestion()
    }
}
