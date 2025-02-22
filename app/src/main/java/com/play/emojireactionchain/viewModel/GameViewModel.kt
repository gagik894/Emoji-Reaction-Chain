package com.play.emojireactionchain.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.play.emojireactionchain.model.GameMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.play.emojireactionchain.model.GameState
import com.play.emojireactionchain.model.GeneratedChainData
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.MixUpChainGenerator
import com.play.emojireactionchain.utils.MixUpOptionGenerator
import com.play.emojireactionchain.utils.OppositeMeaningChainGenerator
import com.play.emojireactionchain.utils.OppositeMeaningOptionGenerator
import com.play.emojireactionchain.utils.SequentialChainGenerator
import com.play.emojireactionchain.utils.SequentialOptionGenerator
import com.play.emojireactionchain.utils.SoundManager
import com.play.emojireactionchain.utils.SynonymChainGenerator
import com.play.emojireactionchain.utils.SynonymOptionGenerator

data class EmojiCategory( // Keep data classes at the top
    val name: String,
    val emojis: List<String>
)

data class GameRule(
    val name: String
)

private data class RuleCategory(val rule: GameRule, val category: EmojiCategory)

class GameViewModel(
    private val soundManager: SoundManager,
    private val highScoreManager: HighScoreManager
) : ViewModel() {
    private var currentGameMode = GameMode.NORMAL
    // Emoji Data and Rules (Keep as they are)
    companion object { // Make emojiCategories accessible to Strategies via Companion Object
        val emojiCategories: Map<String, EmojiCategory> = listOf( // Initialize directly here
            EmojiCategory("Fruits", listOf("🍎", "🍌", "🍇", "🍓", "🍉", "🥝")),
            EmojiCategory("Animals", listOf("🐶", "🐱", "🐻", "🐼", "🐸", "🐒")),
            EmojiCategory("Faces", listOf("😀", "😊", "😂", "😎", "😍", "🤯")),
            EmojiCategory("Emotions", listOf("😀", "😢", "😊", "😠", "😂", "😥")),
            EmojiCategory("Vehicles", listOf("🚗", "🚕", "🚌", "🚑", "🚓", "🚒")),
            EmojiCategory("Clothing", listOf("👕", "👚", "👗", "👖", "👔", "🧣")),
            EmojiCategory("Sports", listOf("⚽️", "🏀", "🏈", "⚾️", "🎾", "🏐")),
            EmojiCategory("Food (Beyond Fruits)", listOf("🍰", "🎂", "🥨", "🥪", "🌮", "🍜")),
            EmojiCategory("Drinks", listOf("☕", "🍵", "🍶", "🍺", "🍷", "🍹")),
            EmojiCategory("Travel/Places", listOf("⛰️", "🏖️", "🏕️", "🗽", "🗼", "🕌")),
            EmojiCategory("Time/Date/Weather", listOf("⏰", "🗓️", "☀️", "🌧️", "❄️", "🌈")),
            EmojiCategory("Household Objects", listOf("🛋️", "🛏️", "🚪", "🪑", "💡", "🧸")),
            EmojiCategory("Technology", listOf("📱", "💻", "⌨️", "🖱️", "🎧", "📺")),
            EmojiCategory("Tools/Instruments", listOf("🔨", "🔧", "🧰", "🧪", "🔬", "🔭")),
            EmojiCategory("Music", listOf("🎵", "🎶", "🎤", "🎧", "🎼", "🎹")),
            EmojiCategory("Office/School Supplies", listOf("📚", "📓", "📐", "📏", "🖇️", "✏️"))
        ).associateBy { it.name }
        val oppositeEmojiMap = mapOf(
            "😀" to "😢", "😢" to "😀", "😊" to "😠", "😠" to "😊", "😂" to "😥", "😥" to "😂"
        )
    }


    private val _rules = listOf( // Make _rules private
        GameRule("Sequential in Category"),
        GameRule("Opposite Meaning"),
        GameRule("Category Mix-Up"),
        GameRule("Synonym Chain") // New Rule: Synonym Chain
    )
    private val rules: List<GameRule> = _rules
    private val questionCountPerGame = 10

    // Strategy Objects
    private val sequentialChainGenerator = SequentialChainGenerator()
    private val sequentialOptionGenerator = SequentialOptionGenerator()
    private val mixUpChainGenerator = MixUpChainGenerator()
    private val mixUpOptionGenerator = MixUpOptionGenerator()
    private val oppositeMeaningChainGenerator = OppositeMeaningChainGenerator()
    private val oppositeMeaningOptionGenerator = OppositeMeaningOptionGenerator()
    private val synonymChainGenerator = SynonymChainGenerator()
    private val synonymOptionGenerator = SynonymOptionGenerator()

    // Game State (Keep as they are)
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var currentQuestionCount = 0
    private var currentGameScore = 0

    private var questionStartTime: Long = 0
    private val maxTimePerQuestionSeconds: Int = 10
    private val pointsPerSecondBonus: Int = 5

    private var currentStreak: Int = 0 // To track the current streak of correct answers
    private val streakBonusThreshold: Int = 3 // Streak length required to start getting bonus
    private val streakBonusPoints: Int = 20 // Base bonus points awarded for each question in a streak (beyond threshold)

    init {
        loadHighScore()
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }

    private fun loadHighScore() {
        val highScore = highScoreManager.getHighScore()
        _gameState.value = _gameState.value.copy(highScore = highScore)
    }

    fun startGame(gameMode: GameMode = GameMode.NORMAL) {
        viewModelScope.launch {
            currentGameScore = 0
            currentQuestionCount = 0
            currentStreak = 0

            _gameState.value = GameState(
                score = 0,
                highScore = highScoreManager.getHighScore(),
                totalQuestions = questionCountPerGame,
                lives = 3,
                currentTimeBonus = 0,       // Initialize currentTimeBonus to 0 at start
                currentStreakBonus = 0,     // Initialize currentStreakBonus to 0 at start
                currentStreakCount = 0       // Initialize currentStreakCount to 0 at start (already done, but good to be explicit)
            )
            nextQuestion()
        }
    }

    private fun nextQuestion() {
        viewModelScope.launch {
            if (currentQuestionCount < questionCountPerGame) {
                currentQuestionCount++
                val ruleCategory = selectRuleAndCategory()
                val generatedChainData = generateEmojiChain(ruleCategory.category, ruleCategory.rule)

                questionStartTime = System.currentTimeMillis() // Record the start time here!

                _gameState.value = _gameState.value.copy(
                    emojiChain = generatedChainData.emojiChain,
                    choices = generatedChainData.choices,
                    correctAnswerEmoji = generatedChainData.correctAnswerEmoji,
                    isCorrectAnswer = null,
                    questionNumber = currentQuestionCount,
                    rule = ruleCategory.rule.name
                )
            } else {
                _gameState.value = _gameState.value.copy(isGameOver = true, isCorrectAnswer = true)
                endGame()
                println("Game Won! - All questions completed")
            }
        }
    }

    private fun endGame() {
        val finalScore = currentGameScore
        highScoreManager.updateHighScoreIfNewRecord(finalScore)
        val updatedHighScore = highScoreManager.getHighScore()
        _gameState.value = _gameState.value.copy(isGameOver = true, score = finalScore, highScore = updatedHighScore)
    }


    private fun selectRuleAndCategory(): RuleCategory {
        val category = emojiCategories.values.random()
        val rule = rules.random()
        return RuleCategory(rule, category)
    }


    private fun generateEmojiChain(category: EmojiCategory, rule: GameRule): GeneratedChainData {
        if (category.emojis.isEmpty()) {
            return GeneratedChainData(emptyList(), emptyList(), "")
        }

        return when (rule.name) {
            "Sequential in Category" -> sequentialChainGenerator.generateChain(category, rule)
            "Category Mix-Up" -> mixUpChainGenerator.generateChain(category, rule)
            "Opposite Meaning" -> oppositeMeaningChainGenerator.generateChain(category, rule)
            "Synonym Chain" -> synonymChainGenerator.generateChain(category, rule) // New Case: Synonym Chain
            else -> sequentialChainGenerator.generateChain(category, rule) // Default case
        }
    }


    private fun generateAnswerOptions(correctAnswerEmoji: String, category: EmojiCategory, rule: GameRule, emojiChain: List<String>): List<String> {
        return when (rule.name) {
            "Sequential in Category" -> sequentialOptionGenerator.generateOptions(correctAnswerEmoji, category, rule, emojiChain)
            "Category Mix-Up" -> mixUpOptionGenerator.generateOptions(correctAnswerEmoji, category, rule, emojiChain)
            "Opposite Meaning" -> oppositeMeaningOptionGenerator.generateOptions(correctAnswerEmoji, category, rule, emojiChain)
            "Synonym Chain" -> synonymOptionGenerator.generateOptions(correctAnswerEmoji, category, rule, emojiChain) // New Case: Synonym Chain
            else -> sequentialOptionGenerator.generateOptions(correctAnswerEmoji, category, rule, emojiChain) // Default case
        }
    }


    fun handleChoice(chosenEmoji: String) {
        viewModelScope.launch {
            if (chosenEmoji == _gameState.value.correctAnswerEmoji) {
                // Correct Answer
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
                println("Correct! Score: $currentGameScore, Time Bonus: $timeBonus, Streak Bonus: $streakBonus, Current Streak: $currentStreak")

            } else {
                // Incorrect Answer
                currentStreak = 0 // Reset the streak count on incorrect answer!

                val currentLives = _gameState.value.lives
                if (currentLives > 1) {
                    _gameState.value = _gameState.value.copy(
                        isCorrectAnswer = false,
                        lives = currentLives - 1,
                        currentTimeBonus = 0,       // Reset currentTimeBonus in GameState to 0
                        currentStreakBonus = 0,     // Reset currentStreakBonus in GameState to 0
                        currentStreakCount = currentStreak // Update currentStreakCount (will be 0) in GameState
                    )
                    soundManager.playIncorrectSound()
                    delay(150)
                    soundManager.playIncorrectHaptic()
                    delay(1000)
                    nextQuestion()
                    println("Incorrect! Lives Remaining: ${currentLives - 1}, Streak Reset!") // Print streak reset info
                } else {
                    _gameState.value = _gameState.value.copy(
                        isCorrectAnswer = false,
                        lives = 0,
                        isGameOver = true
                    )
                    soundManager.playIncorrectSound()
                    soundManager.playIncorrectHaptic()
                    delay(500)
                    endGame()
                    println("Game Over! Final Score: $currentGameScore, Streak Reset!") // Print streak reset info at game over
                }
            }
        }
    }

    fun resetGame() {
        currentGameScore = 0
        currentQuestionCount = 0
        currentStreak = 0

        viewModelScope.launch {
            _gameState.value = _gameState.value.copy(
                isGameOver = false,
                isCorrectAnswer = null,
                score = 0,
                questionNumber = 0,
                emojiChain = emptyList(),
                choices = emptyList(),
                correctAnswerEmoji = "",
                lives = 3,
                currentTimeBonus = 0,       // Initialize currentTimeBonus to 0 on reset
                currentStreakBonus = 0,     // Initialize currentStreakBonus to 0 on reset
                currentStreakCount = 0       // Initialize currentStreakCount to 0 on reset (already done, but good to be explicit)
            )
            startGame()
        }
    }
}