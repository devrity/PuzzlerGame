package com.devrity.brainnotpuzzler.model

import android.graphics.Bitmap
import com.devrity.brainnotpuzzler.util.Constants
import kotlin.math.abs

class PuzzleBoard {
    private val gridSize = Constants.GRID_SIZE
    private var pieces: Array<PuzzlePiece?> = arrayOfNulls(gridSize * gridSize)
    private var emptyPosition: Int = -1
    private val totalPieces = gridSize * gridSize

    fun initBoard(pieceBitmaps: List<Bitmap>, emptyPieceId: Int) {
        if (pieceBitmaps.size != totalPieces) {
            throw IllegalArgumentException("Expected $totalPieces pieces, got ${pieceBitmaps.size}")
        }

        for (i in 0 until totalPieces) {
            val isThisTheEmptyPiece = (i == emptyPieceId)
            pieces[i] = PuzzlePiece(
                id = i,
                correctPosition = i,
                currentPosition = i,
                bitmap = if (isThisTheEmptyPiece) null else pieceBitmaps[i],
                isEmptySpace = isThisTheEmptyPiece
            )
        }
    }

    fun setBoardState(order: List<String>) {
        val originalPieces = this.pieces.associateBy { it?.correctPosition }
        val newPieces: Array<PuzzlePiece?> = arrayOfNulls(totalPieces)

        for (i in 0 until totalPieces) {
            val pieceIdentifier = order[i]
            val pieceToMove: PuzzlePiece?

            if (pieceIdentifier == "E") {
                pieceToMove = originalPieces.values.first { it?.isEmptySpace == true }
            } else {
                val correctPositionId = pieceIdentifier.toInt()
                pieceToMove = originalPieces[correctPositionId]
            }

            if (pieceToMove != null) {
                newPieces[i] = pieceToMove.copy(currentPosition = i)
                if (pieceToMove.isEmptySpace) {
                    emptyPosition = i
                }
            }
        }
        this.pieces = newPieces
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

    fun shuffle(emptyPieceId: Int) {
        // Find the designated empty piece and prepare it for shuffling.
        val emptyPiece = pieces.find { it?.correctPosition == emptyPieceId }
        if (emptyPiece != null) {
            val index = pieces.indexOf(emptyPiece)
            pieces[index] = emptyPiece.copy(isEmptySpace = true, bitmap = null)
            emptyPosition = emptyPiece.currentPosition
        }

        var lastMove = -1
        repeat(100) {
            val validMoves = getValidMoves().filter { it != lastMove }
            if (validMoves.isNotEmpty()) {
                val randomMove = validMoves.random()
                movePiece(randomMove)
                lastMove = emptyPosition
            }
        }
    }

    fun isSolved(): Boolean {
        for (piece in pieces) {
            if (piece?.isInCorrectPosition() == false) {
                return false
            }
        }
        return true
    }

    fun getCurrentPieceOrder(): ArrayList<Int> {
        return ArrayList(pieces.map { it?.id ?: -1 })
    }

    fun restoreBoardState(order: List<Int>) {
        val currentPieces = this.pieces.associateBy { it?.id }
        val newPieces: Array<PuzzlePiece?> = arrayOfNulls(order.size)
        for (i in order.indices) {
            val pieceId = order[i]
            val piece = currentPieces[pieceId]
            newPieces[i] = piece?.copy(currentPosition = i)
            if (piece?.isEmptySpace == true) {
                emptyPosition = i
            }
        }
        this.pieces = newPieces
    }
}
