package com.play.emojireactionchain.viewModel

import android.os.CountDownTimer
import androidx.lifecycle.viewModelScope
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.LossReason
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.QuestionGenerator
import com.play.emojireactionchain.utils.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BlitzGameViewModel(
    soundManager: SoundManager,
    highScoreManager: HighScoreManager
) : BaseGameViewModel(soundManager, highScoreManager) {

    override val maxTimePerQuestionSeconds: Int = 3
    private var timerJob: Job = Job()
    override val questionCountPerGame: Int = Int.MAX_VALUE
    val maxLives: Int = 3
    private var countDownTimer: CountDownTimer? = null

    init {
        loadHighScore(GameMode.BLITZ) // Load at init
    }

    override fun startGame() { // Remove gameMode parameter
        viewModelScope.launch {
            // No need to call loadHighScore here, it's done in init

            _gameState.value = _gameState.value.copy( //add game mode
                gameMode = GameMode.BLITZ,
                gameResult = GameResult.InProgress,
                score = 0,
                lives = maxLives,
                currentStreakCount = 0,
                currentStreakBonus = 0,
                currentTimeBonus = 0,
                isCorrectAnswer = false
            )
            nextQuestion()
        }
    }

    override fun generateQuestionData(level: Int): Triple<List<String>, String, List<String>> {
        val ruleCategory = selectRuleAndCategory(level)
        val category = ruleCategory.category
        val rule = ruleCategory.rule
        val availableEmojis = category.emojis

        // Use the base class method to get the question generator
        val questionGenerator: QuestionGenerator = getQuestionGenerator(rule.name)


        return questionGenerator.generateQuestion(availableEmojis, level)
    }


    private fun startTimer() {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(maxTimePerQuestionSeconds * 1000L, 100) {
            override fun onTick(millisUntilFinished: Long) {
                // Optional: Update a time state value if needed for UI
            }

            override fun onFinish() {
                if (_gameState.value.gameResult == GameResult.InProgress) {
                    viewModelScope.launch {
                        handleIncorrectChoice(true)
                    }
                }
            }
        }.start()
    }

    override fun handleNextQuestionModeSpecific() {
        startTimer() // Start timer in Blitz mode
    }

    // ... (rest of BlitzGameViewModel, no other changes needed) ...
    override suspend fun handleCorrectChoice() {
        timerJob.cancel() // Use simple cancel, no join needed

        val answerTimeMillis = System.currentTimeMillis() - questionStartTime
        val timeBonus =
            ((maxTimePerQuestionSeconds - answerTimeMillis / 1000.0) * pointsPerSecondBonus).toInt()
                .coerceAtLeast(0)
        currentGameScore += timeBonus

        currentStreak++
        var streakBonus = 0
        if (currentStreak >= streakBonusThreshold) {
            streakBonus = streakBonusPoints * currentStreak // Increase bonus with streak!
            currentGameScore += streakBonus
        }
        currentGameScore++
        _gameState.value = _gameState.value.copy(
            score = currentGameScore,
            isCorrectAnswer = true,
            currentTimeBonus = timeBonus,
            currentStreakBonus = streakBonus,
            currentStreakCount = currentStreak,
            gameResult = GameResult.InProgress // Keep playing
        )

        soundManager.playCorrectSound()
        soundManager.playCorrectHaptic()
        delay(300) // Shorter delay for Blitz mode!
        nextQuestion()
    }

    override suspend fun handleIncorrectChoice() {
        handleIncorrectChoice(false) // Non-timeout incorrect choice
    }

    suspend fun handleIncorrectChoice(isTimeout: Boolean) {
        soundManager.playIncorrectSoundAndHaptic()
        timerJob.cancel()
        currentStreak = 0

        println(_gameState.value.lives > 1)
        // Just remove one life regardless of whether it's timeout or wrong answer
        if (_gameState.value.lives > 1) {
            println("Next question")
            _gameState.value = _gameState.value.copy(
                isCorrectAnswer = false,
                lives = _gameState.value.lives - 1,
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                currentStreakCount = currentStreak
            )
            delay(150)
            println("Next question")
            nextQuestion() // This will start a new timer through handleNextQuestionModeSpecific()
        } else {
            // If this was the last life, end the game
            val lossReason = if (isTimeout) LossReason.TimeOut else LossReason.OutOfLives
            endGame(GameResult.Lost(lossReason))
        }
    }

    override fun handleAdReward() {
        _gameState.value = _gameState.value.copy(
            lives = maxLives,
            gameResult = GameResult.InProgress
        )
        timerJob.cancel()
        nextQuestion()
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
        countDownTimer = null
    }
}