package com.play.emojireactionchain.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.GameState
import com.play.emojireactionchain.model.LossReason
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.MixUpQuestionGenerator
import com.play.emojireactionchain.utils.OppositeQuestionGenerator
import com.play.emojireactionchain.utils.QuestionGenerator
import com.play.emojireactionchain.utils.SequentialQuestionGenerator
import com.play.emojireactionchain.utils.SoundManager
import com.play.emojireactionchain.utils.SynonymQuestionGenerator
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
    protected val soundManager: SoundManager,
    protected val highScoreManager: HighScoreManager
) : ViewModel() {

    // --- Companion Object (Static Data) ---
    companion object {
        val emojiCategories: Map<String, EmojiCategory> = listOf(
            EmojiCategory("Fruits", listOf("🍎", "🍌", "🍇", "🍓", "🍉", "🥝", "🍍", "🥭", "🍑", "🍒", "🍈", "🥥")),
            EmojiCategory("Animals", listOf("🐶", "🐱", "🐻", "🐼", "🐸", "🐒", "🦁", "🐯", "🦊", "🦝", "🐷", "🐮")),
            EmojiCategory("Faces", listOf("😀", "😊", "😂", "😎", "😍", "🤯", "🤨", "🤔", "🤩", "🥳", "😳", "🥺")),
            EmojiCategory("Emotions", listOf("😀", "😢", "😊", "😠", "😂", "😥", "😨", "😰", "😱", "🥵", "🥶", "😳")),
            EmojiCategory("Vehicles", listOf("🚗", "🚕", "🚌", "🚑", "🚓", "🚒", "✈️", "🚀", "🚢", "⛵️", "🚁", "🚲")),
            EmojiCategory("Clothing", listOf("👕", "👚", "👗", "👖", "👔", "🧣", "🧤", "🧦", "🧢", "👒", "🎩", "👟")),
            EmojiCategory("Sports", listOf("⚽️", "🏀", "🏈", "⚾️", "🎾", "🏐", "🏓", "🏸", "🏒", "🥍", "🏏", "⛳️")),
            EmojiCategory("Food (Beyond Fruits)", listOf("🍰", "🎂", "🥨", "🥪", "🌮", "🍜", "🍕", "🍔", "🍟", "🍦", "🍩", "🍪")),
            EmojiCategory("Drinks", listOf("☕", "🍵", "🍶", "🍺", "🍷", "🍹", "🥛", "🧃", "🥤", "🧉", "🧊", "🫗")), // Added more
            EmojiCategory("Travel/Places", listOf("⛰️", "🏖️", "🏕️", "🗽", "🗼", "🕌", "⛩️", "🏞️", "🏟️", "🏛️", "🏘️", "🏙️")), // Added more
            EmojiCategory("Time/Date/Weather", listOf("⏰", "🗓️", "☀️", "🌧️", "❄️", "🌈", "🌪️", "⚡️", "☔️", "🌬️", "📅", "⏱️")),
            EmojiCategory("Household Objects", listOf("🛋️", "🛏️", "🚪", "🪑", "💡", "🧸", "🪞", "🧽", "🪣", "🔑", "🖼️", "🚽")), // Added more
            EmojiCategory("Technology", listOf("📱", "💻", "⌨️", "🖱️", "🎧", "📺", "⌚️", "📷", "📹", "🕹️", "💾", "💽")),
            EmojiCategory("Tools/Instruments", listOf("🔨", "🔧", "🧰", "🧪", "🔬", "🔭", "🪛", "🪚", "🪓", "🪤", "🧲", "🔦")), // Added more
            EmojiCategory("Music", listOf("🎵", "🎶", "🎤", "🎧", "🎼", "🎹", "🎸", "🎻", "🎺", "🥁", "🎷", "📻")),
            EmojiCategory("Office/School Supplies", listOf("📚", "📓", "📐", "📏", "🖇️", "✏️", "📝", "📁", "📂", "📅", "📊", "📈"))
        ).associateBy { it.name }

        val oppositeEmojiMap = mapOf(
            "😀" to "😢", "😢" to "😀", "😊" to "😠", "😠" to "😊", "😂" to "😥", "😥" to "😂",
            "☀️" to "🌧️", "🌧️" to "☀️", "🔥" to "🧊", "🧊" to "🔥", "⬆️" to "⬇️", "⬇️" to "⬆️",
            "❤️" to "💔", "💔" to "❤️", "✅" to "❌", "❌" to "✅"
        )

        val synonymPairs = listOf(
            listOf("😀", "😊", "😄", "😁", "😆", "😅"), // Happy faces
            listOf("😢", "😥", "😓", "😔", "😟", "🙁"), // Sad faces
            listOf("😠", "😡", "😤", "🤬"),       // Angry faces
            listOf("😨", "😱", "😰"),          // Scared faces
            listOf("😴", "😪", "💤"),       // Sleepy emojis
            listOf("🚗", "🚕", "🚙", "🚓"),    // Cars
            listOf("🏠", "🏡", "🏘️", "🏢"),   // Buildings
            listOf("☀️", "🌤️", "⛅️", "🔆"), // Sunny weather
            listOf("🌧️", "☔️", "⛈️"),       // Rainy weather
        )
    }
    private val _rules = listOf(
        GameRule("Sequential in Category"),
        GameRule("Category Mix-Up"),
        GameRule("Opposite Meaning"),
        GameRule("Synonym Chain")
    )

    protected val rules: List<GameRule> = _rules // Protected, accessible to subclasses
    protected open val questionCountPerGame = 30 // Allow overriding

    // --- Game State (Now protected) ---
    protected val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    protected var currentQuestionCount = 0
    protected var currentGameScore = 0

    var questionStartTime: Long = 0
    open val maxTimePerQuestionSeconds: Int = 10  // Allow overriding
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
    protected open fun nextQuestion() { // Make it open for potential overrides, but provide a default implementation

        viewModelScope.launch {
            currentQuestionCount++
            println(currentQuestionCount)
            // --- Leveling Logic ---
            println(level)
            var attempts = 0 // prevent infinit loop
            var questionData: Triple<List<String>, String, List<String>>? = null
            while (questionData == null && attempts < 10){
                attempts++
                val (emojis, correctAnswer, choices) = generateQuestionData(level)
                if(correctAnswer.isNotBlank() && choices.isNotEmpty() && choices.contains(correctAnswer)){
                    questionData = Triple(emojis, correctAnswer, choices)
                }
            }

            questionStartTime = System.currentTimeMillis()

            if(questionData != null){
                _gameState.value = _gameState.value.copy(
                    emojiChain = questionData.first,
                    choices = questionData.third.shuffled(), // Shuffle choices here!
                    correctAnswerEmoji = questionData.second,
                    isCorrectAnswer = null,
                    questionNumber = currentQuestionCount,
                    rule = null, // Common behavior: don't display rule
                    gameResult = GameResult.InProgress,
                    lives = _gameState.value.lives //keep current value
                )
            } else {
                // Handle the case where no valid question could be generated
                // This is VERY important to prevent crashes/infinite loops
                endGame(GameResult.Lost(LossReason.OutOfLives)) // Or some other appropriate action
            }
            handleNextQuestionModeSpecific()
        }
    }

    // NEW: Abstract function for mode-specific logic within nextQuestion
    protected abstract fun handleNextQuestionModeSpecific()

    // --- Common Methods ---
    protected open fun selectRuleAndCategory(level: Int): RuleCategory {
        // Level-based rule selection (more sophisticated)
        val availableRules = when {
            level <= 3 -> listOf("Sequential in Category", "Category Mix-Up") // Easier rules
            level <= 6 -> listOf("Sequential in Category", "Category Mix-Up", "Opposite Meaning")
            else -> rules.map { it.name } // All rules available at higher levels
        }

        val ruleName = availableRules.random()

        // Level-based category selection (optional, but recommended)
        val availableCategories = when {
            level <= 2 -> listOf("Fruits", "Animals", "Faces", "Emotions") // Basic categories
            level <= 5 -> emojiCategories.keys.toList()  // All categories
            else -> emojiCategories.keys.toList() // All categories
        }
        val categoryName = availableCategories.random()
        val category = emojiCategories[categoryName] ?: emojiCategories.values.random() // Fallback

        return RuleCategory(GameRule(ruleName), category) // Return a RuleCategory object
    }


    // --- Abstract Choice Handling (Implemented by subclasses) ---
    protected abstract suspend fun handleCorrectChoice()
    protected abstract suspend fun handleIncorrectChoice()

    // --- End Game (Common logic) ---
    protected fun endGame(result: GameResult, offerContinue: Boolean = true) {
        val finalScore = currentGameScore
        highScoreManager.updateHighScoreIfNewRecord(finalScore, _gameState.value.gameMode)
        val updatedHighScore = highScoreManager.getHighScore(_gameState.value.gameMode)

        if (result is GameResult.Lost && offerContinue) {
            _gameState.value = _gameState.value.copy(
                score = finalScore,
                highScore = updatedHighScore,
                gameResult = GameResult.AdContinueOffered(result)
            )
        } else {
            _gameState.value = _gameState.value.copy(
                gameResult = result,
                score = finalScore,
                highScore = updatedHighScore
            )
        }
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

    protected abstract fun generateQuestionData(level: Int): Triple<List<String>, String, List<String>>

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

    protected open fun getQuestionGenerator(ruleName: String): QuestionGenerator {
        return when (ruleName) {
            "Sequential in Category" -> SequentialQuestionGenerator()
            "Category Mix-Up" -> MixUpQuestionGenerator()
            "Opposite Meaning" -> OppositeQuestionGenerator()
            "Synonym Chain" -> SynonymQuestionGenerator()
            else -> SequentialQuestionGenerator() // Default
        }
    }

    abstract fun handleAdReward()

    // --- Level Calculation (New) ---
     val level: Int
        get() = (currentQuestionCount / 5) + 1  // Increase level every 5 questions
}