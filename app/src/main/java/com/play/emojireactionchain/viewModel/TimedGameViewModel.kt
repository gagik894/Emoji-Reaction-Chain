package com.play.emojireactionchain.viewModel

import androidx.lifecycle.viewModelScope
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.model.GameResult
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
import kotlinx.coroutines.launch

class TimedGameViewModel(
    soundManager: SoundManager,
    highScoreManager: HighScoreManager
) : BaseGameViewModel(soundManager, highScoreManager) {

    private val totalGameTimeSeconds: Int = 60
    private val timeBonusPerCorrectAnswer: Int = 2
    private var timerJob: Job = Job()
    private val _remainingGameTimeFlow = MutableStateFlow(0L)
    val remainingGameTimeFlow = _remainingGameTimeFlow.asStateFlow()
    override val questionCountPerGame = Int.MAX_VALUE

    init {
        loadHighScore(GameMode.TIMED) // Load at init
    }

    override fun startGame() { // Remove gameMode parameter
        viewModelScope.launch {
            currentGameScore = 0
            currentQuestionCount = 0
            currentStreak = 0
            _remainingGameTimeFlow.value = totalGameTimeSeconds * 1000L
            // No need to call loadHighScore here

            _gameState.value = GameState(
                score = 0,
                totalQuestions = questionCountPerGame,
                lives = 3, // Infinite lives
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                currentStreakCount = 0,
                gameMode = GameMode.TIMED, // Set directly
                gameResult = GameResult.InProgress
            )
            nextQuestion()
            startTotalGameTimer()
        }
    }
    // In NormalGameViewModel, TimedGameViewModel, and BlitzGameViewModel:
    override fun generateQuestionData(level: Int): Triple<List<String>, String, List<String>> {
        val ruleCategory = selectRuleAndCategory(level)
        val category = ruleCategory.category
        val rule = ruleCategory.rule
        val availableEmojis = category.emojis

        val questionGenerator: QuestionGenerator = getQuestionGenerator(rule.name)
        return questionGenerator.generateQuestion(availableEmojis, level)
    }
    private fun startTotalGameTimer() {
        timerJob.cancel() // Ensure any previous timer is cancelled
        timerJob = viewModelScope.launch {
            while (_remainingGameTimeFlow.value > 0) {
                delay(100) // Update every 100ms for smoother countdown
                _remainingGameTimeFlow.value -= 100
            }
            endGame(GameResult.Lost(LossReason.TimeOut)) // Game Over - Time's Up
        }
    }

    // Implement the mode-specific part (starting the timer)
    override fun handleNextQuestionModeSpecific() {
        //startTotalGameTimer() // Start timer in Timed mode //REMOVED
        //No check here it is infinite
    }
    override suspend fun handleCorrectChoice() {

        currentStreak++
        var streakBonus = 0
        if (currentStreak >= streakBonusThreshold) {
            streakBonus = streakBonusPoints * currentStreak
            currentGameScore += streakBonus
        }
        currentGameScore++

        _remainingGameTimeFlow.value += timeBonusPerCorrectAnswer * 1000L

        _gameState.value = _gameState.value.copy(
            score = currentGameScore,
            isCorrectAnswer = true,
            currentTimeBonus = timeBonusPerCorrectAnswer,
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
        // No timer to cancel here either
        currentStreak = 0

        // Don't end the game immediately, just reduce time
        _gameState.value = _gameState.value.copy(
            isCorrectAnswer = false,
            currentTimeBonus = 0,
            currentStreakBonus = 0,
            currentStreakCount = currentStreak,
            gameResult = GameResult.InProgress // Stay in progress
        )
        soundManager.playIncorrectSoundAndHaptic()
        delay(500)
        nextQuestion()
    }
    override fun resetGame() {
        currentGameScore = 0
        currentQuestionCount = 0
        currentStreak = 0

        viewModelScope.launch {
            timerJob.cancelAndJoin() //cancel timer job
            _remainingGameTimeFlow.value = 0 //set remaining time to zero
            _gameState.value = _gameState.value.copy(
                isCorrectAnswer = null,
                score = 0,
                questionNumber = 0,
                emojiChain = emptyList(),
                choices = emptyList(),
                correctAnswerEmoji = "",
                lives = 1, //reset to one for timed mode and keep 3 for normal mode
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                currentStreakCount = 0,
                gameResult = GameResult.InProgress // Reset to InProgress
            )
            // Don't call startGame here - let the UI trigger the specific mode start.
        }
    }
    override fun handleAdReward() {
        // Reset timer to half of total time
        _remainingGameTimeFlow.value = (totalGameTimeSeconds / 2) * 1000L

        _gameState.value = _gameState.value.copy(
            lives = 1,
            gameResult = GameResult.InProgress
        )

        // Restart the timer
        startTotalGameTimer()

        nextQuestion()
    }
}