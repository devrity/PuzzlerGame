package com.devrity.puzzler.model

import android.graphics.Bitmap
import kotlin.math.abs

/**
 * Manages the puzzle board state and game logic.
 */
class PuzzleBoard(private val gridSize: Int) {
    private var pieces: Array<PuzzlePiece?> = arrayOfNulls(gridSize * gridSize)
    private var emptyPosition: Int = -1
    private val totalPieces = gridSize * gridSize
    
    /**
     * Initialize the board with sliced image pieces.
     * Creates all pieces except bottom-right, which becomes the empty space.
     */
    fun initBoard(pieceBitmaps: List<Bitmap>) {
        if (pieceBitmaps.size != totalPieces - 1) {
            throw IllegalArgumentException("Expected ${totalPieces - 1} pieces, got ${pieceBitmaps.size}")
        }
        
        // Create pieces for all positions except bottom-right
        for (i in 0 until totalPieces - 1) {
            pieces[i] = PuzzlePiece(
                id = i,
                correctPosition = i,
                currentPosition = i,
                bitmap = pieceBitmaps[i],
                isEmptySpace = false
            )
        }
        
        // Create empty space at bottom-right position
        emptyPosition = totalPieces - 1
        pieces[emptyPosition] = PuzzlePiece(
            id = totalPieces - 1,
            correctPosition = totalPieces - 1,
            currentPosition = emptyPosition,
            bitmap = null,
            isEmptySpace = true
        )
    }
    
    /**
     * Get all pieces in their current positions.
     */
    fun getPieces(): Array<PuzzlePiece?> = pieces.copyOf()
    
    /**
     * Get the current position of the empty space.
     */
    fun getEmptyPosition(): Int = emptyPosition
    
    /**
     * Check if a piece at given position can move (is adjacent to empty space).
     */
    fun canMove(position: Int): Boolean {
        if (position < 0 || position >= totalPieces) return false
        if (position == emptyPosition) return false
        return isAdjacent(position, emptyPosition)
    }
    
    /**
     * Move a piece at given position to the empty space if possible.
     * @return true if move was successful, false otherwise
     */
    fun movePiece(position: Int): Boolean {
        if (!canMove(position)) return false
        
        val movingPiece = pieces[position] ?: return false
        val emptyPiece = pieces[emptyPosition] ?: return false
        
        if (!emptyPiece.isEmptySpace) return false
        
        // Update the moving piece's position
        val updatedMovingPiece = movingPiece.copy(currentPosition = emptyPosition)
        
        // Update the empty piece's position
        val updatedEmptyPiece = emptyPiece.copy(currentPosition = position)
        
        // Swap the pieces
        pieces[emptyPosition] = updatedMovingPiece
        pieces[position] = updatedEmptyPiece
        
        // Update empty position tracker
        emptyPosition = position
        
        return true
    }
    
    /**
     * Check if two positions are adjacent (horizontally or vertically).
     */
    private fun isAdjacent(pos1: Int, pos2: Int): Boolean {
        val row1 = pos1 / gridSize
        val col1 = pos1 % gridSize
        val row2 = pos2 / gridSize
        val col2 = pos2 % gridSize
        
        // Same row, adjacent columns
        if (row1 == row2 && abs(col1 - col2) == 1) return true
        
        // Same column, adjacent rows
        if (col1 == col2 && abs(row1 - row2) == 1) return true
        
        return false
    }
    
    /**
     * Get list of positions adjacent to empty space that can be moved.
     */
    fun getValidMoves(): List<Int> {
        val validMoves = mutableListOf<Int>()
        for (i in pieces.indices) {
            if (canMove(i)) {
                validMoves.add(i)
            }
        }
        return validMoves
    }
    
    /**
     * Shuffle the board by performing random valid moves.
     * This ensures the puzzle is always solvable.
     */
    fun shuffle(moves: Int = 100) {
        var lastMove = -1
        repeat(moves) {
            val validMoves = getValidMoves().filter { it != lastMove } // Avoid undoing last move
            if (validMoves.isNotEmpty()) {
                val randomMove = validMoves.random()
                movePiece(randomMove)
                lastMove = emptyPosition
            }
        }
    }
    
    /**
     * Check if the puzzle is solved (all pieces in correct positions).
     * Note: Empty space should be at bottom-right for solved state.
     */
    fun isSolved(): Boolean {
        // Check if empty space is at correct position
        if (emptyPosition != totalPieces - 1) return false
        
        // Check if all other pieces are in correct positions
        for (i in 0 until totalPieces - 1) {
            val piece = pieces[i] ?: return false
            if (!piece.isInCorrectPosition()) return false
        }
        return true
    }
    
    /**
     * Get the grid size.
     */
    fun getGridSize(): Int = gridSize
}
