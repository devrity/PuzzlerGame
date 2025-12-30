package com.devrity.brainnotpuzzler.model

import android.graphics.Bitmap
import kotlin.math.abs

class PuzzleBoard(private val gridSize: Int) {
    private var pieces: Array<PuzzlePiece?>
    var moveCount: Int = 0
    private val totalPieces = gridSize * gridSize

    init {
        pieces = arrayOfNulls(totalPieces)
    }

    fun setupBoard(pieceBitmaps: List<Bitmap>, initState: List<String>?, savedState: List<String>?) {
        // 1. Create a "perfect" board first
        for (i in 0 until totalPieces) {
            pieces[i] = PuzzlePiece(
                id = i, correctPosition = i, currentPosition = i,
                bitmap = pieceBitmaps.getOrNull(i), isEmptySpace = false
            )
        }

        // 2. Determine the true state based on priority
        val stateToLoad = savedState ?: initState

        if (stateToLoad != null) {
            // Load from initState or savedState
            val allPossibleIds = (0 until totalPieces).toMutableSet()
            val visibleIds = stateToLoad.filter { it != "E" }.map { it.split("_")[0].toInt() }
            allPossibleIds.removeAll(visibleIds)
            val emptyIds = allPossibleIds.toMutableList()

            val originalPieces = this.pieces.associateBy { it?.correctPosition }
            val newPieces: Array<PuzzlePiece?> = arrayOfNulls(totalPieces)

            for (i in stateToLoad.indices) {
                val identifier = stateToLoad[i]
                if (identifier == "E") {
                    val emptyId = emptyIds.firstOrNull()
                    if (emptyId != null) {
                        val piece = originalPieces[emptyId]
                        newPieces[i] = piece?.copy(currentPosition = i, isEmptySpace = true, bitmap = null)
                        emptyIds.remove(emptyId)
                    }
                } else {
                    val parts = identifier.split("_")
                    val correctId = parts[0].toInt()
                    val isLocked = parts.size > 1 && parts[1] == "L"
                    val piece = originalPieces[correctId]
                    newPieces[i] = piece?.copy(currentPosition = i, isLocked = isLocked)
                }
            }
            this.pieces = newPieces
        } else {
            // No predefined state, just shuffle
            val defaultEmptyId = totalPieces - 1
            val emptyPiece = pieces.find { it?.correctPosition == defaultEmptyId }
            emptyPiece?.isEmptySpace = true
            emptyPiece?.bitmap = null
            shuffle()
        }
    }

    fun getPieces(): Array<PuzzlePiece?> = pieces.copyOf()
    fun getGridSize(): Int = gridSize

    private fun getEmptyPositions(): List<Int> {
        return pieces.mapIndexedNotNull { index, piece -> if (piece?.isEmptySpace == true) index else null }
    }

    private fun isAdjacent(pos1: Int, pos2: Int): Boolean {
        val row1 = pos1 / gridSize
        val col1 = pos1 % gridSize
        val row2 = pos2 / gridSize
        val col2 = pos2 % gridSize
        return (row1 == row2 && abs(col1 - col2) == 1) || (col1 == col2 && abs(row1 - row2) == 1)
    }

    fun movePiece(position: Int): Boolean {
        val emptyNeighbors = getEmptyPositions().filter { isAdjacent(position, it) }
        if (emptyNeighbors.size == 1) {
            return movePiece(position, emptyNeighbors.first())
        }
        return false
    }

    fun movePiece(from: Int, to: Int): Boolean {
        val fromPiece = pieces.getOrNull(from)
        val toPiece = pieces.getOrNull(to)
        
        // FIX #1: Add adjacency check
        if (fromPiece == null || toPiece == null || 
            fromPiece.isEmptySpace || 
            fromPiece.isLocked || 
            !toPiece.isEmptySpace ||
            !isAdjacent(from, to)) {  // <-- ADDED: Check if positions are adjacent
            return false
        }

        pieces[to] = fromPiece.copy(currentPosition = to)
        pieces[from] = toPiece.copy(currentPosition = from)
        moveCount++
        return true
    }

    private fun shuffle() {
        var lastMovedFrom = -1
        repeat(20 * totalPieces) {
            val movablePieces = pieces.mapIndexedNotNull { index, piece ->
                if (piece?.isEmptySpace == false && canMove(index) && index != lastMovedFrom) index else null
            }
            if (movablePieces.isNotEmpty()) {
                val randomPiecePosition = movablePieces.random()
                lastMovedFrom = randomPiecePosition
                movePiece(randomPiecePosition)
            }
        }
        moveCount = 0
    }

    private fun canMove(position: Int): Boolean {
        val piece = pieces.getOrNull(position)
        return piece != null && !piece.isEmptySpace && !piece.isLocked && getEmptyPositions().any { isAdjacent(position, it) }
    }

    fun isSolved(): Boolean {
        return pieces.all { it?.isInCorrectPosition() ?: true }
    }

    fun getCurrentStateForSave(): ArrayList<String> {
        return ArrayList(pieces.map { piece ->
            when {
                piece == null -> ""
                piece.isEmptySpace -> "E"
                piece.isLocked -> "${piece.correctPosition}_L"
                else -> "${piece.correctPosition}"
            }
        })
    }
}
