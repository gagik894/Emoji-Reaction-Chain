package com.play.emojireactionchain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.play.emojireactionchain.data.GameRepository
import com.play.emojireactionchain.utils.AchievementBadgeManager
import com.play.emojireactionchain.utils.AvatarProgressManager
import com.play.emojireactionchain.utils.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: GameRepository,
    private val avatarProgressManager: AvatarProgressManager,
    private val achievementBadgeManager: AchievementBadgeManager,
    private val soundManager: SoundManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshHomeData()
    }

    fun refreshHomeData() {
        viewModelScope.launch {
            val streak = repository.getDailyStreak()
            val highScores = repository.getHighScores()
            val unlockedStickers = repository.getUnlockedStickers()
            val stickerCount = unlockedStickers.size
            
            val dailyReward = repository.awardDailySticker()
            
            if (dailyReward != null) {
                soundManager.playCorrectSound()
                soundManager.playCorrectHaptic()
            }

            val avatarProgress = avatarProgressManager.getAvatarProgress(stickerCount)
            val unlockedBadges = achievementBadgeManager.getUnlockedBadges(streak, highScores, stickerCount)

            _uiState.update { 
                it.copy(
                    dailyStreak = streak,
                    modeHighScores = highScores,
                    unlockedStickers = unlockedStickers,
                    stickerCount = stickerCount,
                    dailyStickerEmoji = dailyReward?.sticker,
                    avatarLevelEmoji = avatarProgress.emoji,
                    avatarLevelTitle = avatarProgress.title,
                    avatarLevelSubtitle = avatarProgress.subtitle,
                    unlockedBadges = unlockedBadges,
                    isLoading = false
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
