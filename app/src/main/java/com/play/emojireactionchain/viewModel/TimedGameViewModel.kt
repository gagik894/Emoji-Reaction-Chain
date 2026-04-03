package com.play.emojireactionchain.viewModel

import androidx.lifecycle.viewModelScope
import com.play.emojireactionchain.model.EmojiData
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.GameRule
import com.play.emojireactionchain.model.GameState
import com.play.emojireactionchain.model.LossReason
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.QuestionGenerator
import com.play.emojireactionchain.utils.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TimedGameViewModel(
    soundManager: SoundManager,
    highScoreManager: HighScoreManager
) : BaseGameViewModel(soundManager, highScoreManager) {

    private val totalGameTimeSeconds: Int = 60
    private val timeBonusPerCorrectAnswer: Int = 2
    private val timePenaltyPerIncorrectAnswer: Int = 2
    private var timerJob: Job? = null
    private val _remainingGameTimeFlow = MutableStateFlow(0L)
    val remainingGameTimeFlow = _remainingGameTimeFlow.asStateFlow()
    override val questionCountPerGame = Int.MAX_VALUE

    init {
        loadHighScore(GameMode.TIMED)
    }

    override fun startGame() {
        _remainingGameTimeFlow.value = totalGameTimeSeconds * 1000L
        
        _gameState.update {
            GameState(
                score = 0,
                totalQuestions = questionCountPerGame,
                lives = 1,
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                currentStreakCount = 0,
                gameMode = GameMode.TIMED,
                gameResult = GameResult.InProgress
            )
        }
        resetEngagementLayer()
        nextQuestion()
        startTotalGameTimer()
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

    private fun startTotalGameTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingGameTimeFlow.value > 0 && _gameState.value.gameResult == GameResult.InProgress) {
                delay(100)
                _remainingGameTimeFlow.update { (it - 100).coerceAtLeast(0L) }
            }
            if (_gameState.value.gameResult == GameResult.InProgress) {
                endGame(GameResult.Lost(LossReason.TimeOut))
            }
        }
    }

    override fun handleNextQuestionModeSpecific() {
        // Infinite mode logic
    }

    override suspend fun handleCorrectChoice() {
        _remainingGameTimeFlow.update { it + timeBonusPerCorrectAnswer * 1000L }

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
                currentTimeBonus = timeBonusPerCorrectAnswer,
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
        _remainingGameTimeFlow.update { (it - (timePenaltyPerIncorrectAnswer * 1000L)).coerceAtLeast(0L) }

        _gameState.update { state ->
            state.copy(
                isCorrectAnswer = false,
                currentTimeBonus = -timePenaltyPerIncorrectAnswer,
                currentStreakBonus = 0,
                currentStreakCount = 0
            )
        }

        if (_remainingGameTimeFlow.value <= 0L) {
            endGame(GameResult.Lost(LossReason.TimeOut))
            return
        }

        soundManager.playIncorrectSoundAndHaptic()
        delay(500)
        nextQuestion()
    }

    override fun resetGame() {
        viewModelScope.launch {
            timerJob?.cancelAndJoin()
            _remainingGameTimeFlow.value = 0
            super.resetGame()
        }
    }

    override fun handleAdReward() {
        _remainingGameTimeFlow.value = (totalGameTimeSeconds / 2) * 1000L
        _gameState.update {
            it.copy(
                lives = 1,
                gameResult = GameResult.InProgress
            )
        }
        startTotalGameTimer()
        nextQuestion()
    }
}
