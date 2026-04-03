package com.play.emojireactionchain.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.play.emojireactionchain.model.EmojiCategory
import com.play.emojireactionchain.model.EmojiData
import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.model.GameResult
import com.play.emojireactionchain.model.GameRule
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class RuleCategory(val rule: GameRule, val category: EmojiCategory)

private data class RulePerformance(
    val attempts: Int = 0,
    val correct: Int = 0
)

abstract class BaseGameViewModel(
    protected val soundManager: SoundManager,
    protected val highScoreManager: HighScoreManager,
    protected val random: Random = Random.Default
) : ViewModel() {

    protected open val questionCountPerGame = 30

    protected val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    protected var questionStartTime: Long = 0
    open val maxTimePerQuestionSeconds: Int = 10
    protected val pointsPerSecondBonus: Int = 5

    protected val streakBonusThreshold: Int = 3
    protected val streakBonusPoints: Int = 20
    private var isAnswerInFlight: Boolean = false
    private var previousRule: GameRule? = null
    private var previousCategory: EmojiCategory? = null
    private val rulePerformance = mutableMapOf<GameRule, RulePerformance>()
    
    private var currentRule: GameRule = GameRule.SEQUENTIAL
    private var currentCategory: EmojiCategory = EmojiData.categories.first()
    
    private var currentQuestionIsBonusRound: Boolean = false
    private val bonusRoundInterval = 5
    private var streakMissionTarget = 3
    private var streakMissionReward = 25

    init {
        resetEngagementLayer()
    }

    protected fun resetEngagementLayer() {
        rulePerformance.clear()
        currentQuestionIsBonusRound = false
        _gameState.update { it.copy(streakMissionProgress = 0) }
        updateMission(level = 1)
    }

    protected fun loadHighScore(gameMode: GameMode) {
        val highScore = highScoreManager.getHighScore(gameMode)
        _gameState.update { it.copy(highScore = highScore, gameMode = gameMode) }
    }

    abstract fun startGame()
    
    protected open fun nextQuestion() {
        viewModelScope.launch {
            val nextQuestionNumber = _gameState.value.questionNumber + 1
            currentQuestionIsBonusRound = nextQuestionNumber % bonusRoundInterval == 0

            handleNextQuestionModeSpecific()
            if (_gameState.value.gameResult != GameResult.InProgress) return@launch

            var attempts = 0
            var questionData: Triple<List<String>, String, List<String>>? = null
            while (questionData == null && attempts < 10) {
                attempts++
                val (emojis, correctAnswer, choices) = generateQuestionData(level)
                val repeatsPreviousAnswer =
                    _gameState.value.correctAnswerEmoji.isNotBlank() && _gameState.value.correctAnswerEmoji == correctAnswer

                if (correctAnswer.isNotBlank() && choices.isNotEmpty() && choices.contains(correctAnswer) && !repeatsPreviousAnswer) {
                    questionData = Triple(emojis, correctAnswer, choices)
                }
            }

            questionStartTime = System.currentTimeMillis()

            if (questionData != null) {
                _gameState.update { state ->
                    state.copy(
                        emojiChain = questionData.first,
                        choices = questionData.third.shuffled(),
                        correctAnswerEmoji = questionData.second,
                        isCorrectAnswer = null,
                        questionNumber = nextQuestionNumber,
                        rule = currentRule,
                        gameResult = GameResult.InProgress,
                        isBonusRound = currentQuestionIsBonusRound,
                        streakMissionTarget = streakMissionTarget,
                        currentEngagementBonus = 0,
                        categoryEmoji = currentCategory.iconEmoji
                    )
                }
            } else {
                endGame(GameResult.Lost(LossReason.GenerationFailed), offerContinue = false)
            }
        }
    }

    protected abstract fun handleNextQuestionModeSpecific()

    protected open fun selectRuleAndCategory(level: Int): RuleCategory {
        val availableRules = when {
            level <= 3 -> listOf(GameRule.SEQUENTIAL, GameRule.MIX_UP)
            level <= 6 -> listOf(GameRule.SEQUENTIAL, GameRule.MIX_UP, GameRule.OPPOSITE)
            else -> GameRule.entries
        }

        val candidateRules = availableRules.filter { it != previousRule }.ifEmpty { availableRules }
        val rule = pickWeightedRule(candidateRules)

        val availableCategories = when {
            level <= 2 -> EmojiData.categories.take(4)
            else -> EmojiData.categories
        }
        val candidateCategories = availableCategories.filter { it != previousCategory }.ifEmpty { availableCategories }
        val category = candidateCategories.random(random)

        previousRule = rule
        previousCategory = category
        currentRule = rule
        currentCategory = category

        return RuleCategory(rule, category)
    }

    private fun pickWeightedRule(candidateRules: List<GameRule>): GameRule {
        var totalWeight = 0.0
        val weighted = candidateRules.map { rule ->
            val perf = rulePerformance[rule] ?: RulePerformance()
            val accuracy = if (perf.attempts == 0) 0.5 else perf.correct.toDouble() / perf.attempts
            val novelty = 1.0 / (perf.attempts + 1)
            val pressure = 1.0 - accuracy
            val weight = 0.2 + novelty + pressure
            totalWeight += weight
            rule to weight
        }

        var pick = random.nextDouble() * totalWeight
        for ((rule, weight) in weighted) {
            pick -= weight
            if (pick <= 0.0) return rule
        }
        return candidateRules.random(random)
    }

    protected fun endGame(result: GameResult, offerContinue: Boolean = true) {
        val finalScore = _gameState.value.score
        highScoreManager.updateHighScoreIfNewRecord(finalScore, _gameState.value.gameMode)
        val updatedHighScore = highScoreManager.getHighScore(_gameState.value.gameMode)

        _gameState.update { state ->
            if (result is GameResult.Lost && offerContinue) {
                state.copy(
                    highScore = updatedHighScore,
                    gameResult = GameResult.AdContinueOffered(result)
                )
            } else {
                state.copy(
                    gameResult = result,
                    highScore = updatedHighScore
                )
            }
        }
    }

    open fun resetGame() {
        _gameState.update {
            it.copy(
                isCorrectAnswer = null,
                score = 0,
                questionNumber = 0,
                emojiChain = emptyList(),
                choices = emptyList(),
                correctAnswerEmoji = "",
                lives = 3,
                currentTimeBonus = 0,
                currentStreakBonus = 0,
                currentStreakCount = 0,
                gameResult = GameResult.InProgress,
                categoryEmoji = null
            )
        }
    }

    protected abstract fun generateQuestionData(level: Int): Triple<List<String>, String, List<String>>

    fun handleChoice(chosenEmoji: String) {
        val currentState = _gameState.value
        if (currentState.gameResult != GameResult.InProgress) return
        if (currentState.isCorrectAnswer != null) return
        if (isAnswerInFlight) return

        isAnswerInFlight = true
        viewModelScope.launch {
            try {
                if (chosenEmoji == currentState.correctAnswerEmoji) {
                    handleCorrectChoice()
                    handleEngagementForCorrect()
                } else {
                    handleIncorrectChoice()
                    handleEngagementForIncorrect()
                }
            } finally {
                isAnswerInFlight = false
            }
        }
    }

    protected abstract suspend fun handleCorrectChoice()
    protected abstract suspend fun handleIncorrectChoice()

    private fun handleEngagementForCorrect() {
        recordRuleOutcome(correct = true)

        var engagementBonus = 0
        val nextProgress = _gameState.value.streakMissionProgress + 1
        
        if (nextProgress >= streakMissionTarget) {
            engagementBonus += streakMissionReward
            updateMission(level)
            _gameState.update { it.copy(streakMissionProgress = 0) }
        } else {
            _gameState.update { it.copy(streakMissionProgress = nextProgress) }
        }

        if (currentQuestionIsBonusRound) {
            engagementBonus += (8 + level * 2)
        }

        if (engagementBonus > 0) {
            _gameState.update { state ->
                val newScore = state.score + engagementBonus
                state.copy(
                    score = newScore,
                    currentEngagementBonus = engagementBonus,
                    streakMissionTarget = streakMissionTarget,
                    currentStreakBonus = state.currentStreakBonus + engagementBonus
                )
            }
        } else {
            _gameState.update { state ->
                state.copy(
                    streakMissionTarget = streakMissionTarget,
                    currentEngagementBonus = 0
                )
            }
        }
    }

    private fun handleEngagementForIncorrect() {
        recordRuleOutcome(correct = false)
        _gameState.update { state ->
            state.copy(
                streakMissionProgress = 0,
                currentEngagementBonus = 0
            )
        }
    }

    private fun recordRuleOutcome(correct: Boolean) {
        val current = rulePerformance[currentRule] ?: RulePerformance()
        rulePerformance[currentRule] = current.copy(
            attempts = current.attempts + 1,
            correct = current.correct + if (correct) 1 else 0
        )
    }

    private fun updateMission(level: Int) {
        val target = when {
            level <= 2 -> 2
            level <= 5 -> 3
            else -> 4
        }
        streakMissionTarget = target
        streakMissionReward = 10 + (target * 8)
    }

    protected open fun getQuestionGenerator(rule: GameRule): QuestionGenerator {
        return when (rule) {
            GameRule.SEQUENTIAL -> SequentialQuestionGenerator()
            GameRule.MIX_UP -> MixUpQuestionGenerator()
            GameRule.OPPOSITE -> OppositeQuestionGenerator()
            GameRule.SYNONYM -> SynonymQuestionGenerator()
        }
    }

    abstract fun handleAdReward()

    val level: Int
        get() = (_gameState.value.questionNumber / 5) + 1
}
