package com.play.emojireactionchain.viewModel

import android.os.CountDownTimer
import androidx.lifecycle.viewModelScope
import com.play.emojireactionchain.model.EmojiData
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.GameRule
import com.play.emojireactionchain.model.LossReason
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.QuestionGenerator
import com.play.emojireactionchain.utils.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BlitzGameViewModel(
    soundManager: SoundManager,
    highScoreManager: HighScoreManager
) : BaseGameViewModel(soundManager, highScoreManager) {

    override val maxTimePerQuestionSeconds: Int = 3
    override val questionCountPerGame: Int = Int.MAX_VALUE
    val maxLives: Int = 3
    private val _remainingQuestionTimeMs = MutableStateFlow(maxTimePerQuestionSeconds * 1000L)
    val remainingQuestionTimeMsFlow = _remainingQuestionTimeMs.asStateFlow()
    private var countDownTimer: CountDownTimer? = null

    init {
        loadHighScore(GameMode.BLITZ)
    }

    override fun startGame() {
        countDownTimer?.cancel()
        _remainingQuestionTimeMs.value = maxTimePerQuestionSeconds * 1000L
        
        _gameState.update { state ->
            state.copy(
                gameMode = GameMode.BLITZ,
                gameResult = GameResult.InProgress,
                score = 0,
                questionNumber = 0,
                lives = maxLives,
                currentStreakCount = 0,
                currentStreakBonus = 0,
                currentTimeBonus = 0,
                isCorrectAnswer = null
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

    private fun startTimer() {
        countDownTimer?.cancel()
        _remainingQuestionTimeMs.value = maxTimePerQuestionSeconds * 1000L

        countDownTimer = object : CountDownTimer(maxTimePerQuestionSeconds * 1000L, 100) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingQuestionTimeMs.value = millisUntilFinished
            }

            override fun onFinish() {
                _remainingQuestionTimeMs.value = 0L
                if (_gameState.value.gameResult == GameResult.InProgress) {
                    viewModelScope.launch {
                        handleIncorrectChoiceInternal(true)
                    }
                }
            }
        }.start()
    }

    override fun handleNextQuestionModeSpecific() {
        startTimer()
    }

    override suspend fun handleCorrectChoice() {
        countDownTimer?.cancel()
        _remainingQuestionTimeMs.value = maxTimePerQuestionSeconds * 1000L

        val answerTimeMillis = System.currentTimeMillis() - questionStartTime
        val timeBonus =
            ((maxTimePerQuestionSeconds - answerTimeMillis / 1000.0) * pointsPerSecondBonus).toInt()
                .coerceAtLeast(0)

        _gameState.update { state ->
            val newStreak = state.currentStreakCount + 1
            var streakBonus = 0
            if (newStreak >= streakBonusThreshold) {
                streakBonus = streakBonusPoints * newStreak
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
        delay(300)
        nextQuestion()
    }

    override suspend fun handleIncorrectChoice() {
        handleIncorrectChoiceInternal(false)
    }

    private suspend fun handleIncorrectChoiceInternal(isTimeout: Boolean) {
        soundManager.playIncorrectSoundAndHaptic()
        countDownTimer?.cancel()
        _remainingQuestionTimeMs.value = maxTimePerQuestionSeconds * 1000L
        
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
            delay(150)
            nextQuestion()
        } else {
            val lossReason = if (isTimeout) LossReason.TimeOut else LossReason.OutOfLives
            _gameState.update { state -> state.copy(isCorrectAnswer = false, lives = 0) }
            endGame(GameResult.Lost(lossReason))
        }
    }

    override fun handleAdReward() {
        _gameState.update {
            it.copy(
                lives = maxLives,
                gameResult = GameResult.InProgress
            )
        }
        countDownTimer?.cancel()
        _remainingQuestionTimeMs.value = maxTimePerQuestionSeconds * 1000L
        nextQuestion()
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
        _remainingQuestionTimeMs.value = 0L
        countDownTimer = null
    }
}
