package com.play.emojireactionchain.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.play.emojireactionchain.R

class SoundManager(context: Context) {

    private var correctSoundPlayer: MediaPlayer? = null
    private var incorrectSoundPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null // Add Vibrator instance

    init {
        correctSoundPlayer = MediaPlayer.create(context, R.raw.correct_answer)
        incorrectSoundPlayer = MediaPlayer.create(context, R.raw.incorrect_answer)
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator // Initialize Vibrator
    }

    fun playCorrectSound() {
        correctSoundPlayer?.start()
    }

    fun playIncorrectSound() {
        incorrectSoundPlayer?.start()
    }
    fun playIncorrectSoundAndHaptic() {
        playIncorrectSound()
        Thread.sleep(150)
        playIncorrectHaptic()
    }

    fun playCorrectHaptic() { // Function for correct answer haptic
        vibrator?.let { vibrator ->
            if (vibrator.hasVibrator()) { // Check if vibrator is available
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)) // Short, default amplitude vibration
                } else {
                    // Deprecated but necessary for older versions
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50) // Short vibration for older versions
                }
            }
        }
    }

    fun playIncorrectHaptic() { // Function for incorrect answer haptic
        vibrator?.let { vibrator ->
            if (vibrator.hasVibrator()) { // Check if vibrator is available
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 80, 80), -1)) // Waveform vibration - pause, vibrate, pause, longer vibrate, no repeat (-1)
                } else {
                    // Deprecated but necessary for older versions
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(250) // Longer vibration for older versions
                }
            }
        }
    }


    fun release() {
        correctSoundPlayer?.release()
        incorrectSoundPlayer?.release()
        vibrator = null // Nullify Vibrator reference (optional release for Vibrator)
    }
}