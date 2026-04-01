package com.play.emojireactionchain.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.Calendar

data class StickerReward(
    val sticker: String,
    val totalUnlocked: Int,
    val isNew: Boolean
)

class StickerBookManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val rewardEngine = StickerRewardEngine()

    fun awardDailyStickerIfNeeded(): StickerReward? {
        val today = getCurrentDayKey()
        val lastRewardDay = sharedPreferences.getInt(KEY_LAST_REWARD_DAY, -1)
        if (lastRewardDay == today) return null

        val unlocked = getUnlockedStickers().toMutableSet()
        val nextSticker = rewardEngine.nextSticker(unlocked) ?: return null

        unlocked.add(nextSticker)
        sharedPreferences.edit {
            putInt(KEY_LAST_REWARD_DAY, today)
            putString(KEY_LAST_STICKER, nextSticker)
            putStringSet(KEY_UNLOCKED_STICKERS, unlocked)
        }

        return StickerReward(
            sticker = nextSticker,
            totalUnlocked = unlocked.size,
            isNew = true
        )
    }

    fun getStickerCount(): Int = getUnlockedStickers().size

    fun getLatestSticker(): String? = sharedPreferences.getString(KEY_LAST_STICKER, null)

    fun getUnlockedStickers(): Set<String> {
        return sharedPreferences.getStringSet(KEY_UNLOCKED_STICKERS, emptySet())?.toSet().orEmpty()
    }

    private fun getCurrentDayKey(): Int {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return (year * 10_000) + (month * 100) + day
    }

    companion object {
        private const val PREFS_NAME = "sticker_book_prefs"
        private const val KEY_LAST_REWARD_DAY = "last_reward_day"
        private const val KEY_LAST_STICKER = "last_sticker"
        private const val KEY_UNLOCKED_STICKERS = "unlocked_stickers"
    }
}

