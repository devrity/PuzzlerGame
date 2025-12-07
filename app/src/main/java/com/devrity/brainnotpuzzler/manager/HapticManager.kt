package com.devrity.brainnotpuzzler.manager

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.devrity.brainnotpuzzler.util.Constants

/**
 * Manages haptic feedback (vibration).
 */
class HapticManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Check if haptic feedback is enabled in settings.
     */
    fun isHapticEnabled(): Boolean {
        return prefs.getBoolean(Constants.KEY_HAPTIC_ENABLED, true)
    }

    /**
     * Light tap feedback for piece moves.
     */
    fun playPieceMoveHaptic() {
        if (!isHapticEnabled() || vibrator == null) return

        try {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    20, // 20 milliseconds
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Celebration pattern for victory.
     */
    fun playVictoryHaptic() {
        if (!isHapticEnabled() || vibrator == null) return

        try {
            // Pattern: buzz-pause-buzz (celebration feel)
            val pattern = longArrayOf(0, 100, 50, 100)
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, -1)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
