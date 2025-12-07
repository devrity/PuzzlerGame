package com.devrity.brainnotpuzzler.model

import android.graphics.Bitmap
import com.devrity.brainnotpuzzler.util.Constants
import kotlin.math.abs

class PuzzleBoard {
    private val gridSize = Constants.GRID_SIZE
    private var pieces: Array<PuzzlePiece?> = arrayOfNulls(gridSize * gridSize)
    private var emptyPosition: Int = -1
    private val totalPieces = gridSize * gridSize

    fun initBoard(pieceBitmaps: List<Bitmap>) {
        if (pieceBitmaps.size != totalPieces - 1) {
            throw IllegalArgumentException("Expected ${totalPieces - 1} pieces, got ${pieceBitmaps.size}")
        }

        for (i in 0 until totalPieces - 1) {
            pieces[i] = PuzzlePiece(
                id = i,
                correctPosition = i,
                currentPosition = i,
                bitmap = pieceBitmaps[i],
                isEmptySpace = false
            )
        }

        emptyPosition = totalPieces - 1
        pieces[emptyPosition] = PuzzlePiece(
            id = totalPieces - 1,
            correctPosition = totalPieces - 1,
            currentPosition = emptyPosition,
            bitmap = null,
            isEmptySpace = true
        )
    }

    fun getPieces(): Array<PuzzlePiece?> = pieces.copyOf()

    fun getEmptyPosition(): Int = emptyPosition

    fun canMove(position: Int): Boolean {
        if (position < 0 || position >= totalPieces) return false
        if (position == emptyPosition) return false
        return isAdjacent(position, emptyPosition)
    }

    fun movePiece(position: Int): Boolean {
        if (!canMove(position)) return false

        val movingPiece = pieces[position] ?: return false
        val emptyPiece = pieces[emptyPosition] ?: return false

        if (!emptyPiece.isEmptySpace) return false

        val updatedMovingPiece = movingPiece.copy(currentPosition = emptyPosition)
        val updatedEmptyPiece = emptyPiece.copy(currentPosition = position)

        pieces[emptyPosition] = updatedMovingPiece
        pieces[position] = updatedEmptyPiece

        emptyPosition = position

        return true
    }

    private fun isAdjacent(pos1: Int, pos2: Int): Boolean {
        val row1 = pos1 / gridSize
        val col1 = pos1 % gridSize
        val row2 = pos2 / gridSize
        val col2 = pos2 % gridSize

        if (row1 == row2 && abs(col1 - col2) == 1) return true
        if (col1 == col2 && abs(row1 - row2) == 1) return true

        return false
    }

    fun getValidMoves(): List<Int> {
        val validMoves = mutableListOf<Int>()
        for (i in pieces.indices) {
            if (canMove(i)) {
                validMoves.add(i)
            }
        }
        return validMoves
    }

    fun shuffle(moves: Int = 100) {
        var lastMove = -1
        repeat(moves) {
            val validMoves = getValidMoves().filter { it != lastMove }
            if (validMoves.isNotEmpty()) {
                val randomMove = validMoves.random()
                movePiece(randomMove)
                lastMove = emptyPosition
            }
        }
    }

    fun isSolved(): Boolean {
        if (emptyPosition != totalPieces - 1) return false

        for (i in 0 until totalPieces - 1) {
            val piece = pieces[i] ?: return false
            if (!piece.isInCorrectPosition()) return false
        }
        return true
    }
}
