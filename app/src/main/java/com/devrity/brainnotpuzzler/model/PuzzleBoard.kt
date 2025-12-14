package com.devrity.brainnotpuzzler.model

import android.graphics.Bitmap
import kotlin.math.abs

class PuzzleBoard(private val gridSize: Int) {
    private var pieces: Array<PuzzlePiece?>
    private val totalPieces = gridSize * gridSize

    init {
        pieces = arrayOfNulls(totalPieces)
    }

    fun initBoard(pieceBitmaps: List<Bitmap>, emptyPieceIds: List<Int>) {
        if (pieceBitmaps.size != totalPieces) {
            throw IllegalArgumentException("Expected $totalPieces pieces, got ${pieceBitmaps.size}")
        }

        for (i in 0 until totalPieces) {
            val isThisTheEmptyPiece = emptyPieceIds.contains(i)
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
        val allPossiblePieceIds = (0 until totalPieces).toMutableSet()
        val visiblePieceCorrectIds = order.filter { it != "E" }.map { it.toInt() }
        allPossiblePieceIds.removeAll(visiblePieceCorrectIds)
        val emptyPieceCorrectIds = allPossiblePieceIds

        val originalPieces = this.pieces.associateBy { it?.correctPosition }
        val newPieces: Array<PuzzlePiece?> = arrayOfNulls(totalPieces)

        for (i in 0 until totalPieces) {
            val pieceIdentifier = order[i]
            if (pieceIdentifier == "E") {
                val emptyId = emptyPieceCorrectIds.firstOrNull() ?: -1
                val emptyPiece = originalPieces[emptyId]
                if (emptyPiece != null) {
                    newPieces[i] = emptyPiece.copy(currentPosition = i, isEmptySpace = true, bitmap = null)
                    emptyPieceCorrectIds.remove(emptyId)
                }
            } else {
                val correctPositionId = pieceIdentifier.toInt()
                val pieceToMove = originalPieces[correctPositionId]
                newPieces[i] = pieceToMove?.copy(currentPosition = i)
            }
        }
        this.pieces = newPieces
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

        if (row1 == row2 && abs(col1 - col2) == 1) return true
        if (col1 == col2 && abs(row1 - row2) == 1) return true

        return false
    }

    fun canMove(position: Int): Boolean {
        if (position < 0 || position >= totalPieces) return false
        val piece = pieces[position] ?: return false
        if (piece.isEmptySpace) return false
        
        val emptyNeighbors = getEmptyPositions().filter { isAdjacent(position, it) }
        return emptyNeighbors.isNotEmpty()
    }
    
    // Overloaded function for simple, unambiguous taps
    fun movePiece(position: Int): Boolean {
        val emptyNeighbors = getEmptyPositions().filter { isAdjacent(position, it) }
        if (emptyNeighbors.size == 1) {
            return movePiece(position, emptyNeighbors.first())
        }
        return false
    }

    // New, explicit function for swipes
    fun movePiece(from: Int, to: Int): Boolean {
        if (from < 0 || from >= totalPieces || to < 0 || to >= totalPieces) return false
        
        val fromPiece = pieces[from]
        val toPiece = pieces[to]
        
        if (fromPiece == null || toPiece == null) return false
        if (fromPiece.isEmptySpace || !toPiece.isEmptySpace) return false
        if (!isAdjacent(from, to)) return false

        // Swap pieces
        pieces[to] = fromPiece.copy(currentPosition = to)
        pieces[from] = toPiece.copy(currentPosition = from)

        return true
    }
    
    fun shuffle() {
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
            val pieceToMove = if (pieceId == -1) {
                pieces.find { it?.isEmptySpace == true }
            } else {
                currentPieces[pieceId]
            }
            
            if (pieceToMove != null) {
                newPieces[i] = pieceToMove.copy(currentPosition = i)
            }
        }
        this.pieces = newPieces
    }
}
