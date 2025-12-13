package com.devrity.brainnotpuzzler.model

import android.graphics.Bitmap
import kotlin.math.abs

class PuzzleBoard(private val gridSize: Int) {
    private var pieces: Array<PuzzlePiece?>
    private var emptyPosition: Int = -1
    private val totalPieces = gridSize * gridSize

    init {
        pieces = arrayOfNulls(totalPieces)
    }

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
            if (isThisTheEmptyPiece) {
                emptyPosition = i // Set initial empty position
            }
        }
    }

    fun setBoardState(order: List<String>) {
        if (order.size != totalPieces) {
            throw IllegalArgumentException("Initial state must have $totalPieces elements.")
        }
        
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
    
    fun getGridSize(): Int = gridSize

    private fun isAdjacent(pos1: Int, pos2: Int): Boolean {
        val row1 = pos1 / gridSize
        val col1 = pos1 % gridSize
        val row2 = pos2 / gridSize
        val col2 = pos2 % gridSize

        if (row1 == row2 && abs(col1 - col2) == 1) return true
        if (col1 == col2 && abs(row1 - row2) == 1) return true

        return false
    }

    fun canMove(position: Int): Boolean {
        if (position < 0 || position >= totalPieces) return false
        if (position == emptyPosition) return false
        return isAdjacent(position, emptyPosition)
    }
    
    private fun getValidMoves(): List<Int> {
        val validMoves = mutableListOf<Int>()
        val (emptyRow, emptyCol) = Pair(emptyPosition / gridSize, emptyPosition % gridSize)

        if (emptyRow > 0) validMoves.add(emptyPosition - gridSize) // Up
        if (emptyRow < gridSize - 1) validMoves.add(emptyPosition + gridSize) // Down
        if (emptyCol > 0) validMoves.add(emptyPosition - 1) // Left
        if (emptyCol < gridSize - 1) validMoves.add(emptyPosition + 1) // Right

        return validMoves
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
    
    fun shuffle() {
        var lastMove = -1
        // Scale shuffles based on grid size for better randomness
        repeat(20 * totalPieces) {
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
        // Store -1 for the empty piece to correctly restore it.
        return ArrayList(pieces.map { if(it?.isEmptySpace == true) -1 else it?.id ?: -1 })
    }

    fun restoreBoardState(order: List<Int>) {
        val currentPieces = this.pieces.associateBy { it?.id }
        val newPieces: Array<PuzzlePiece?> = arrayOfNulls(order.size)
        for (i in order.indices) {
            val pieceId = order[i]
            val pieceToMove = if (pieceId == -1) {
                pieces.find { it?.isEmptySpace == true }
            } else {
                currentPieces[pieceId]
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
}
