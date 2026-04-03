package com.play.emojireactionchain.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.time.LocalDate

class DailyStreakManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun updateAndGetCurrentStreak(): Int {
        val today = LocalDate.now()
        val lastOpenedDateString = sharedPreferences.getString(KEY_LAST_OPEN_DATE, null)
        val currentStreak = sharedPreferences.getInt(KEY_STREAK_COUNT, 0)

        val lastOpenedDate = lastOpenedDateString?.let { LocalDate.parse(it) }

        val updatedStreak = when {
            lastOpenedDate == today -> {
                if (currentStreak > 0) currentStreak else 1
            }
            lastOpenedDate != null && lastOpenedDate.plusDays(1) == today -> currentStreak + 1
            else -> 1
        }

        sharedPreferences.edit {
            putString(KEY_LAST_OPEN_DATE, today.toString())
            putInt(KEY_STREAK_COUNT, updatedStreak)
        }

        return updatedStreak
    }

    companion object {
        private const val PREFS_NAME = "daily_streak_prefs"
        private const val KEY_LAST_OPEN_DATE = "last_open_date"
        private const val KEY_STREAK_COUNT = "streak_count"
    }
}
