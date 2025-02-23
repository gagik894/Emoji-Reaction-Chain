package com.play.emojireactionchain.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.model.GameResult
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RuleCategory(val rule: GameRule, val category: EmojiCategory)
data class EmojiCategory(
    val name: String,
    val emojis: List<String>
)

data class GameRule(
    val name: String
)

abstract class BaseGameViewModel(
    protected val soundManager: SoundManager,  // Changed to protected
    protected val highScoreManager: HighScoreManager // Changed to protected
) : ViewModel() {

    // --- Companion Object (Static Data) ---
    companion object {
        val emojiCategories: Map<String, EmojiCategory> = listOf(
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

    // --- Game Rules and Constants ---
    private val _rules = listOf(
        GameRule("Sequential in Category"),
        GameRule("Opposite Meaning"),
        GameRule("Category Mix-Up"),
        GameRule("Synonym Chain")
    )
    protected val rules: List<GameRule> = _rules // Protected, accessible to subclasses
    protected open val questionCountPerGame = 10 // Allow overriding

    // --- Strategy Objects (Now protected) ---
    protected val sequentialChainGenerator = SequentialChainGenerator()
    protected val sequentialOptionGenerator = SequentialOptionGenerator()
    protected val mixUpChainGenerator = MixUpChainGenerator()
    protected val mixUpOptionGenerator = MixUpOptionGenerator()
    protected val oppositeMeaningChainGenerator = OppositeMeaningChainGenerator()
    protected val oppositeMeaningOptionGenerator = OppositeMeaningOptionGenerator()
    protected val synonymChainGenerator = SynonymChainGenerator()
    protected val synonymOptionGenerator = SynonymOptionGenerator()


    // --- Game State (Now protected) ---
    protected val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    protected var currentQuestionCount = 0
    protected var currentGameScore = 0

    var questionStartTime: Long = 0
    protected open val maxTimePerQuestionSeconds: Int = 10  // Allow overriding
    protected val pointsPerSecondBonus: Int = 5

    protected var currentStreak: Int = 0
    protected val streakBonusThreshold: Int = 3
    protected val streakBonusPoints: Int = 20


    // --- Initialization ---
    init {
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }

    protected fun loadHighScore(gameMode: GameMode) { // Add gameMode parameter
        val highScore = highScoreManager.getHighScore(gameMode)
        _gameState.value = _gameState.value.copy(highScore = highScore, gameMode = gameMode) // Also, set gameMode
    }


    // --- Abstract Methods (To be implemented by subclasses) ---
    abstract fun startGame()
    protected abstract fun nextQuestion()

    // --- Common Methods ---
    protected open fun selectRuleAndCategory(): RuleCategory { //add open
        val category = emojiCategories.values.random()
        val rule = rules.random()
        return RuleCategory(rule, category)
    }


    protected open fun generateEmojiChain(category: EmojiCategory, rule: GameRule, level: Int = 1): GeneratedChainData { //add level and open
        if (category.emojis.isEmpty()) {
            return GeneratedChainData(emptyList(), emptyList(), "")
        }

        return when (rule.name) {
            "Sequential in Category" -> sequentialChainGenerator.generateChain(category, rule, level) //pass level
            "Category Mix-Up" -> mixUpChainGenerator.generateChain(category, rule, level) //pass level
            "Opposite Meaning" -> oppositeMeaningChainGenerator.generateChain(category, rule, level) //pass level
            "Synonym Chain" -> synonymChainGenerator.generateChain(category, rule, level) //pass level
            else -> sequentialChainGenerator.generateChain(category, rule, level) // Default //pass level
        }
    }

    protected fun generateAnswerOptions(correctAnswerEmoji: String, category: EmojiCategory, rule: GameRule, emojiChain: List<String>): List<String> {
        return when (rule.name) {
            "Sequential in Category" -> sequentialOptionGenerator.generateOptions(correctAnswerEmoji, category, rule, emojiChain)
            "Category Mix-Up" -> mixUpOptionGenerator.generateOptions(correctAnswerEmoji, category, rule, emojiChain)
            "Opposite Meaning" -> oppositeMeaningOptionGenerator.generateOptions(correctAnswerEmoji, category, rule, emojiChain)
            "Synonym Chain" -> synonymOptionGenerator.generateOptions(correctAnswerEmoji, category, rule, emojiChain)
            else -> sequentialOptionGenerator.generateOptions(correctAnswerEmoji, category, rule, emojiChain)
        }
    }

    // --- Handle Choice (Common logic, but calls mode-specific handling) ---
    fun handleChoice(chosenEmoji: String) {
        viewModelScope.launch {
            if (chosenEmoji == _gameState.value.correctAnswerEmoji) {
                handleCorrectChoice()
            } else {
                handleIncorrectChoice()
            }
        }
    }

    // --- Abstract Choice Handling (Implemented by subclasses) ---
    protected abstract suspend fun handleCorrectChoice()
    protected abstract suspend fun handleIncorrectChoice()


    // --- End Game (Common logic) ---
    protected fun endGame(result: GameResult) {
        val finalScore = currentGameScore
        highScoreManager.updateHighScoreIfNewRecord(finalScore, _gameState.value.gameMode)
        val updatedHighScore = highScoreManager.getHighScore(_gameState.value.gameMode)
        _gameState.value = _gameState.value.copy(gameResult = result, score = finalScore, highScore = updatedHighScore) //keep this
    }

    // --- Reset Game (Common logic) ---
    open fun resetGame() {
        currentGameScore = 0
        currentQuestionCount = 0
        currentStreak = 0

        viewModelScope.launch {
            _gameState.value = _gameState.value.copy(
                isCorrectAnswer = null,
                score = 0,
                questionNumber = 0,
                emojiChain = emptyList(),
                choices = emptyList(),
                correctAnswerEmoji = "",
                lives = 3, //reset to one for timed mode and keep 3 for normal mode
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                currentStreakCount = 0,
                gameResult = GameResult.InProgress // Reset to InProgress
            )
        }
    }
//    protected abstract fun generateQuestionData(): Triple<List<String>, String, List<String>>

}