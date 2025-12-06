package com.devrity.puzzler.manager

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.devrity.puzzler.util.Constants

/**
 * Manages haptic feedback (vibration).
 */
class HapticManager(private val context: Context) {
    
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
     * Set haptic feedback enabled/disabled.
     */
    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_HAPTIC_ENABLED, enabled).apply()
    }
    
    /**
     * Light tap feedback for piece moves.
     */
    fun playPieceMoveHaptic() {
        if (!isHapticEnabled() || vibrator == null) return
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        20, // 20 milliseconds
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Pattern: buzz-pause-buzz (celebration feel)
                val pattern = longArrayOf(0, 100, 50, 100)
                vibrator.vibrate(
                    VibrationEffect.createWaveform(pattern, -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 100, 50, 100)
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
