package com.devrity.puzzler.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.ToneGenerator
import android.media.AudioManager
import android.content.SharedPreferences
import com.devrity.puzzler.util.Constants

/**
 * Manages game sound effects.
 */
class SoundManager(private val context: Context) {
    
    private var soundPool: SoundPool? = null
    private var pieceMoveSound: Int = 0
    private var victorySound: Int = 0
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    init {
        initSoundPool()
    }
    
    /**
     * Initialize the SoundPool for playing sounds.
     */
    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()
        
        // For now, we'll use system sounds
        // In the future, load custom sound files from res/raw/
        // pieceMoveSound = soundPool?.load(context, R.raw.piece_move, 1) ?: 0
        // victorySound = soundPool?.load(context, R.raw.victory, 1) ?: 0
    }
    
    /**
     * Check if sound is enabled in settings.
     */
    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(Constants.KEY_SOUND_ENABLED, true)
    }
    
    /**
     * Set sound enabled/disabled.
     */
    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_SOUND_ENABLED, enabled).apply()
    }
    
    /**
     * Play piece move sound (soft click/pop).
     */
    fun playPieceMoveSound() {
        if (!isSoundEnabled()) return
        
        // Using system click sound as placeholder
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.3f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Play victory sound (cheerful jingle).
     */
    fun playVictorySound() {
        if (!isSoundEnabled()) return
        
        // Using system sounds as placeholder
        // In production, this will be a custom cheerful jingle
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 50)
            // Play a short pleasant melody
            Thread {
                try {
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                    Thread.sleep(150)
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 200)
                    Thread.sleep(200)
                    toneGen.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Release sound resources.
     */
    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
