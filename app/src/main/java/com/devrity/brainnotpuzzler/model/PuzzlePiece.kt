package com.devrity.brainnotpuzzler.model

import android.graphics.Bitmap

/**
 * Represents a single piece of the puzzle.
 *
 * @param id Unique identifier for the piece (0 to gridSize*gridSize - 1)
 * @param correctPosition The position where this piece should be in the solved puzzle
 * @param currentPosition The current position of this piece on the board
 * @param bitmap The image bitmap for this piece (null for the empty space)
 * @param isEmptySpace True if this is the empty space that allows movement
 */
data class PuzzlePiece(
    val id: Int,
    val correctPosition: Int,
    var currentPosition: Int,
    val bitmap: Bitmap?,
    val isEmptySpace: Boolean = false
) {
    /**
     * Check if this piece is in its correct position
     */
    fun isInCorrectPosition(): Boolean {
        return currentPosition == correctPosition
    }
}
