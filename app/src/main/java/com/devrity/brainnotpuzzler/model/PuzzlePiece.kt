package com.devrity.brainnotpuzzler.model

import android.graphics.Bitmap

// Self-contained data class for a puzzle piece
data class PuzzlePiece(
    val id: Int,
    val correctPosition: Int,
    var currentPosition: Int,
    var bitmap: Bitmap?,
    var isEmptySpace: Boolean,
    var isLocked: Boolean = false // New property for locking
) {
    fun isInCorrectPosition(): Boolean = currentPosition == correctPosition
}
