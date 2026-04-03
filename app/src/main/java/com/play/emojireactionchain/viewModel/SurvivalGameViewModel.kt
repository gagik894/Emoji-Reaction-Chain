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

class SurvivalGameViewModel(
    soundManager: SoundManager,
    highScoreManager: HighScoreManager
) : BaseGameViewModel(soundManager, highScoreManager) {

    override val questionCountPerGame = Int.MAX_VALUE

    init {
        loadHighScore(GameMode.SURVIVAL)
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
                gameMode = GameMode.SURVIVAL,
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
        if (_gameState.value.lives <= 0) {
            endGame(GameResult.Lost(LossReason.OutOfLives))
        }
    }

    override suspend fun handleCorrectChoice() {
        _gameState.update { state ->
            val newStreak = state.currentStreakCount + 1
            var streakBonus = 0
            if (newStreak >= streakBonusThreshold) {
                streakBonus = streakBonusPoints * newStreak
            }
            val newScore = state.score + streakBonus + 1
            
            state.copy(
                score = newScore,
                isCorrectAnswer = true,
                currentTimeBonus = 0,
                currentStreakBonus = streakBonus,
                currentStreakCount = newStreak
            )
        }

        soundManager.playCorrectSound()
        soundManager.playCorrectHaptic()
        delay(300)
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
            soundManager.playIncorrectSoundAndHaptic()
            delay(500)
            nextQuestion()
        } else {
            _gameState.update { state ->
                state.copy(
                    isCorrectAnswer = false,
                    lives = 0,
                )
            }
            soundManager.playIncorrectSoundAndHaptic()
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
