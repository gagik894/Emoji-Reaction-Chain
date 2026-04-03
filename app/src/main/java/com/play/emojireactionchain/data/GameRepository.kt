package com.play.emojireactionchain.data

import com.play.emojireactionchain.model.GameMode
import com.play.emojireactionchain.utils.DailyStreakManager
import com.play.emojireactionchain.utils.HighScoreManager
import com.play.emojireactionchain.utils.StickerBookManager
import com.play.emojireactionchain.utils.StickerReward

interface GameRepository {
    fun getHighScores(): Map<GameMode, Int>
    fun updateHighScore(mode: GameMode, score: Int)
    fun getDailyStreak(): Int
    fun getUnlockedStickers(): Set<String>
    fun awardDailySticker(): StickerReward?
    fun getStickerCount(): Int
}

class GameRepositoryImpl(
    private val highScoreManager: HighScoreManager,
    private val dailyStreakManager: DailyStreakManager,
    private val stickerBookManager: StickerBookManager
) : GameRepository {

    override fun getHighScores(): Map<GameMode, Int> = highScoreManager.getAllHighScores()

    override fun updateHighScore(mode: GameMode, score: Int) {
        highScoreManager.updateHighScoreIfNewRecord(score, mode)
    }

    override fun getDailyStreak(): Int = dailyStreakManager.updateAndGetCurrentStreak()

    override fun getUnlockedStickers(): Set<String> = stickerBookManager.getUnlockedStickers()

    override fun awardDailySticker(): StickerReward? = stickerBookManager.awardDailyStickerIfNeeded()

    override fun getStickerCount(): Int = stickerBookManager.getStickerCount()
}
