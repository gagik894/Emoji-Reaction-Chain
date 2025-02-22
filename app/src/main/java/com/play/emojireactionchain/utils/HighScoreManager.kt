package com.play.emojireactionchain.utils // Or your preferred package name

import android.content.Context
import android.content.SharedPreferences

class HighScoreManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("EmojiReactionChain_HighScores", Context.MODE_PRIVATE) // Unique SharedPreferences file

    private val highScoreKey = "high_score" // Key to store high score

    fun getHighScore(): Int {
        return sharedPreferences.getInt(highScoreKey, 0) // Default to 0 if no high score saved
    }

    fun saveHighScore(newHighScore: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(highScoreKey, newHighScore)
        editor.apply() // Use apply() for background saving
    }

    fun isNewHighScore(currentScore: Int): Boolean {
        return currentScore > getHighScore()
    }

    fun updateHighScoreIfNewRecord(currentScore: Int) {
        if (isNewHighScore(currentScore)) {
            saveHighScore(currentScore)
        }
    }
}