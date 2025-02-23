package com.play.emojireactionchain.utils // Or your preferred package name

import android.content.Context
import android.content.SharedPreferences
import com.play.emojireactionchain.model.GameMode

class HighScoreManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("high_scores", Context.MODE_PRIVATE)

    fun getHighScore(gameMode: GameMode): Int {
        // Use the GameMode enum value as part of the key
        return sharedPreferences.getInt(getHighScoreKey(gameMode), 0)
    }

    fun updateHighScoreIfNewRecord(newScore: Int, gameMode: GameMode) {
        val currentHighScore = getHighScore(gameMode)
        if (newScore > currentHighScore) {
            sharedPreferences.edit().putInt(getHighScoreKey(gameMode), newScore).apply()
        }
    }

    // Helper function to create a unique key for each GameMode
    private fun getHighScoreKey(gameMode: GameMode): String {
        return "high_score_${gameMode.name}" // e.g., "high_score_NORMAL", "high_score_TIMED"
    }
    //for testing only
    fun clearAllHighScores() {
        sharedPreferences.edit().clear().apply()
    }
}