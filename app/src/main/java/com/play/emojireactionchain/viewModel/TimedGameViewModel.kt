package com.play.emojireactionchain.viewModel

import androidx.lifecycle.viewModelScope
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.LossReason
import com.play.emojireactionchain.model.GameState
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimedGameViewModel(
    soundManager: SoundManager,
    highScoreManager: HighScoreManager
) : BaseGameViewModel(soundManager, highScoreManager) {

    private val totalGameTimeSeconds: Int = 10 // Total game time
    private val timeBonusPerCorrectAnswer: Int = 2 // Seconds added for correct answer
    private var timerJob: Job = Job()
    //var remainingGameTime: Long = 0 // Store remaining time in milliseconds -- OLD
    private val _remainingGameTimeFlow = MutableStateFlow(0L) //in miliseconds
    val remainingGameTimeFlow = _remainingGameTimeFlow.asStateFlow()
    override val questionCountPerGame = Int.MAX_VALUE // Effectively infinite.

    init {
        loadHighScore(GameMode.TIMED)
    }

    override fun startGame() {
        viewModelScope.launch {
            currentGameScore = 0
            currentQuestionCount = 0
            currentStreak = 0
            //remainingGameTime = totalGameTimeSeconds * 1000L // Initialize game time -- OLD
            _remainingGameTimeFlow.value = totalGameTimeSeconds * 1000L

            _gameState.value = GameState(
                score = 0,
                highScore = highScoreManager.getHighScore(GameMode.TIMED),
                totalQuestions = questionCountPerGame, // Still use this for consistency
                lives = 1, // Keep lives=1, even though it's not directly used
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                currentStreakCount = 0,
                gameMode = GameMode.TIMED,
                gameResult = GameResult.InProgress
            )
            nextQuestion()
            startTotalGameTimer() // Start the overall game timer
        }
    }

    override fun nextQuestion() {
        viewModelScope.launch {
            currentQuestionCount++
            val ruleCategory = selectRuleAndCategory()
            val generatedChainData = generateEmojiChain(ruleCategory.category, ruleCategory.rule)

            questionStartTime = System.currentTimeMillis() // Still track question start time

            _gameState.value = _gameState.value.copy(
                emojiChain = generatedChainData.emojiChain,
                choices = generatedChainData.choices,
                correctAnswerEmoji = generatedChainData.correctAnswerEmoji,
                isCorrectAnswer = null,
                questionNumber = currentQuestionCount,
                rule = ruleCategory.rule.name,
                gameResult = GameResult.InProgress
            )
        }
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
}