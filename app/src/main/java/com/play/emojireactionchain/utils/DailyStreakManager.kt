package com.play.emojireactionchain.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.Calendar

class DailyStreakManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun updateAndGetCurrentStreak(): Int {
        val today = getCurrentDayKey()
        val lastOpenedDay = sharedPreferences.getInt(KEY_LAST_OPEN_DAY, -1)
        val currentStreak = sharedPreferences.getInt(KEY_STREAK_COUNT, 0)

        val updatedStreak = when {
            lastOpenedDay == today -> {
                if (currentStreak > 0) currentStreak else 1
            }
            lastOpenedDay != -1 && isPreviousDay(lastOpenedDay, today) -> currentStreak + 1
            else -> 1
        }

        sharedPreferences.edit {
            putInt(KEY_LAST_OPEN_DAY, today)
            putInt(KEY_STREAK_COUNT, updatedStreak)
        }

        return updatedStreak
    }

    private fun getCurrentDayKey(): Int {
        val calendar = Calendar.getInstance()
        return dayKeyFromCalendar(calendar)
    }

    private fun isPreviousDay(previousDayKey: Int, currentDayKey: Int): Boolean {
        val previousCalendar = calendarFromDayKey(previousDayKey)
        previousCalendar.add(Calendar.DAY_OF_YEAR, 1)
        return dayKeyFromCalendar(previousCalendar) == currentDayKey
    }

    private fun dayKeyFromCalendar(calendar: Calendar): Int {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return (year * 10_000) + (month * 100) + day
    }

    private fun calendarFromDayKey(dayKey: Int): Calendar {
        val year = dayKey / 10_000
        val month = (dayKey / 100) % 100
        val day = dayKey % 100

        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    companion object {
        private const val PREFS_NAME = "daily_streak_prefs"
        private const val KEY_LAST_OPEN_DAY = "last_open_day"
        private const val KEY_STREAK_COUNT = "streak_count"
    }
}

