package com.devrity.brainnotpuzzler.manager

import android.content.Context
import android.content.SharedPreferences
import com.devrity.brainnotpuzzler.util.Constants
import com.google.gson.Gson
import java.io.InputStreamReader

class ProgressionManager(private val context: Context) {

    private var puzzleProgression: PuzzleProgression? = null
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    init {
        loadProgression()
    }

    private fun loadProgression() {
        try {
            val inputStream = context.assets.open("gallery_progress.json")
            val reader = InputStreamReader(inputStream)
            puzzleProgression = Gson().fromJson(reader, PuzzleProgression::class.java)
            reader.close()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPuzzleStates(): List<PuzzleState> {
        val puzzleStates = mutableListOf<PuzzleState>()
        puzzleProgression?.puzzles?.forEach { node ->
            val isUnlocked = isPuzzleUnlocked(node.id)
            puzzleStates.add(PuzzleState(node, isUnlocked))
        }
        return puzzleStates
    }

    fun unlockNextPuzzle(currentPuzzleId: String) {
        val currentPuzzle = puzzleProgression?.puzzles?.find { it.id == currentPuzzleId }
        currentPuzzle?.unlocks?.let {
            setPuzzleUnlocked(it, true)
        }
    }

    private fun isPuzzleUnlocked(puzzleId: String): Boolean {
        // The first puzzle is always unlocked
        if (puzzleId == puzzleProgression?.puzzles?.first()?.id) {
            return true
        }
        return prefs.getBoolean("unlocked_$puzzleId", false)
    }

    private fun setPuzzleUnlocked(puzzleId: String, isUnlocked: Boolean) {
        prefs.edit().putBoolean("unlocked_$puzzleId", isUnlocked).apply()
    }
}
