package com.play.emojireactionchain.viewModel

import androidx.lifecycle.viewModelScope
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.GameState
import com.play.emojireactionchain.model.LossReason
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.QuestionGenerator
import com.play.emojireactionchain.utils.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SurvivalGameViewModel(
    soundManager: SoundManager,
    highScoreManager: HighScoreManager
) : BaseGameViewModel(soundManager, highScoreManager) {

    override val questionCountPerGame = Int.MAX_VALUE // Infinite questions
    private var lives: Int = 3 // Private, internal state

    init {
        loadHighScore(GameMode.SURVIVAL) // Load correct high score
    }

    override fun startGame() {
        viewModelScope.launch {
            currentGameScore = 0
            currentQuestionCount = 0
            currentStreak = 0
            lives = 3 // Reset lives
            loadHighScore(GameMode.SURVIVAL)

            _gameState.value = GameState(
                score = 0,
                highScore = highScoreManager.getHighScore(GameMode.SURVIVAL), // Use getHighScore
                totalQuestions = questionCountPerGame,
                lives = lives, // Use the lives variable
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                currentStreakCount = 0,
                gameMode = GameMode.SURVIVAL, // Set the GameMode, use enum.
                gameResult = GameResult.InProgress
            )
            nextQuestion()
        }
    }
    // --- Difficulty-Based Rule Selection ---
    override fun selectRuleAndCategory(level: Int): RuleCategory {
        val availableEmojis = emojiCategories.values.flatMap { it.emojis }.distinct()
        // 1. Filter Rules Based on Applicability:
        val applicableRules = rules.filter { rule ->
            when (rule.name) {
                "Sequential in Category", "Category Mix-Up" -> true // Always applicable
                "Opposite Meaning" -> availableEmojis.any {
                    oppositeEmojiMap.containsKey(it) || oppositeEmojiMap.containsValue(it)
                }
                "Synonym Chain" -> synonymPairs.any { pair ->
                    pair.any { availableEmojis.contains(it) }
                }
                else -> false // Unknown rule (shouldn't happen)
            }
        }
        // 2. Select a Rule Based on Level (from the applicable rules):
        val rule = when (level) {
            1 -> applicableRules.firstOrNull { it.name == "Sequential in Category" } ?: applicableRules.random() // Prioritize Sequential
            2 -> applicableRules.filterNot { it.name == "Opposite Meaning" }.randomOrNull() ?: applicableRules.random() // Exclude Opposite
            3 -> applicableRules.filterNot { it.name == "Opposite Meaning" }.randomOrNull() ?: applicableRules.random() // Exclude Opposite
            else -> applicableRules.randomOrNull() ?: rules.random() // All applicable rules, fallback to any rule
        }


        // 3. Select a Category Based on the Chosen Rule:
        val category = when (rule.name) {
            "Sequential in Category", "Category Mix-Up" -> emojiCategories.values.random()
            "Opposite Meaning" -> emojiCategories["Emotions"]!! // Force Emotions
            "Synonym Chain" -> {
                val validCategories = listOfNotNull(
                    emojiCategories["Faces"],
                    emojiCategories["Emotions"]
                ).filter { it.emojis.any { emoji -> availableEmojis.contains(emoji) } } // Only categories with available emojis
                validCategories.randomOrNull() ?: emojiCategories.values.random() // Fallback
            }
            else -> emojiCategories.values.random() // Default case
        }

        return RuleCategory(rule, category)
    }
    override fun generateQuestionData(level: Int): Triple<List<String>, String, List<String>> {
        val ruleCategory = selectRuleAndCategory(level)
        val category = ruleCategory.category
        val rule = ruleCategory.rule
        val availableEmojis = category.emojis // Use category emojis!

        val questionGenerator: QuestionGenerator = getQuestionGenerator(rule.name)
        return questionGenerator.generateQuestion(availableEmojis, level)
    }

    // Implement the mode-specific part (no timer to start)
    override fun handleNextQuestionModeSpecific() {
        if (lives <= 0) {
            endGame(GameResult.Lost(LossReason.OutOfLives))
        }
    }
    override suspend fun handleCorrectChoice() {

        System.currentTimeMillis() - questionStartTime
        val timeBonus = 0 // No time bonus
        currentGameScore += timeBonus

        currentStreak++
        var streakBonus = 0
        if (currentStreak >= streakBonusThreshold) {
            streakBonus = streakBonusPoints * currentStreak
            currentGameScore += streakBonus
        }
        currentGameScore++ // Increment score for correct answer


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
        delay(300) // Short delay
        nextQuestion()
    }

    override suspend fun handleIncorrectChoice() {
        currentStreak = 0
        lives-- // Decrease lives
        soundManager.playIncorrectSoundAndHaptic()

        if (lives <= 0) {
            endGame(GameResult.Lost(LossReason.OutOfLives))
        } else {
            _gameState.value = _gameState.value.copy(
                isCorrectAnswer = false,
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                currentStreakCount = currentStreak,
                lives = lives, // Update lives in GameState
                gameResult = GameResult.InProgress // Stay in progress
            )
            delay(500)
            nextQuestion()

        }
    }
    override fun handleAdReward() {
        _gameState.value = _gameState.value.copy(
            lives = 3,
            gameResult = GameResult.InProgress,
            isCorrectAnswer = true
        )
        nextQuestion()
    }
}