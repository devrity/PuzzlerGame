package com.devrity.puzzler.util

object Constants {
    // Grid configuration
    const val MIN_GRID_SIZE = 2
    const val MAX_GRID_SIZE = 9
    const val DEFAULT_GRID_SIZE = 3
    
    // Animation durations (milliseconds)
    const val GRID_SHOW_DELAY = 1000L
    const val GRID_REMOVE_DELAY = 1000L
    const val PIECE_ANIMATION_DURATION = 200L
    const val VICTORY_ANIMATION_DELAY = 1000L
    
    // SharedPreferences keys
    const val PREFS_NAME = "PuzzlerPrefs"
    const val KEY_GRID_SIZE = "grid_size"
    const val KEY_SOUND_ENABLED = "sound_enabled"
    const val KEY_HAPTIC_ENABLED = "haptic_enabled"
    
    // Image configuration
    const val PUZZLE_IMAGES_FOLDER = "puzzles"
    const val TARGET_IMAGE_SIZE = 1024
    
    // Grid display
    const val GRID_LINE_WIDTH = 5f
    const val GRID_LINE_COLOR = 0xFF000000.toInt() // Black
}
