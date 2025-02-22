package com.play.emojireactionchain.viewModel

import androidx.lifecycle.viewModelScope
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.LossReason
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BlitzGameViewModel(
    soundManager: SoundManager,
    highScoreManager: HighScoreManager
) : BaseGameViewModel(soundManager, highScoreManager) {

    public override val maxTimePerQuestionSeconds: Int = 3
    private var timerJob: Job = Job() // Single, persistent Job, initialized immediately
    override val questionCountPerGame: Int = Int.MAX_VALUE

    override fun startGame(gameMode: GameMode) {
        viewModelScope.launch {
            nextQuestion()
        }
    }
    override fun nextQuestion() {
        viewModelScope.launch {
            // No need to cancel here anymore: timerJob is managed in startTimer
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
                rule = ruleCategory.rule.name,
                gameResult = GameResult.InProgress
            )

            startTimer() // Start timer *after* UI update
        }
    }

    private fun startTimer() {
        // *** KEY CHANGES HERE ***
        timerJob.cancel() // Cancel any previous job, but don't wait
        timerJob = viewModelScope.launch { // Immediately assign the new Job
            delay(maxTimePerQuestionSeconds * 1000L)
            if (_gameState.value.gameResult == GameResult.InProgress) {
                handleIncorrectChoice(true)
            }else{
                timerJob.cancel()
            }
        }
    }


    override suspend fun handleCorrectChoice() {
        timerJob.cancel() // Use simple cancel, no join needed

        val answerTimeMillis = System.currentTimeMillis() - questionStartTime
        val timeBonus = ((maxTimePerQuestionSeconds - answerTimeMillis / 1000.0) * pointsPerSecondBonus).toInt().coerceAtLeast(0)
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
        delay(300)
        nextQuestion()
    }
    override suspend fun handleIncorrectChoice() {
        handleIncorrectChoice(false) // Non-timeout incorrect choice
    }

    private suspend fun handleIncorrectChoice(isTimeout: Boolean) {
        timerJob.cancel()  // Use simple cancel
        currentStreak = 0

        val lossReason = if (isTimeout) LossReason.TimeOut else LossReason.OutOfLives
        endGame(GameResult.Lost(lossReason))
        soundManager.playIncorrectSoundAndHaptic()
    }
}