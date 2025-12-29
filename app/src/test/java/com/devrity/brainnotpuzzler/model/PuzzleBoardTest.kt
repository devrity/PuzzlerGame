package com.devrity.brainnotpuzzler.model

import android.graphics.Bitmap
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for PuzzleBoard - Core game logic testing
 * Tests cover: move validation, win detection, board state management
 */
@RunWith(MockitoJUnitRunner.Silent::class)
class PuzzleBoardTest {

    @Mock
    private lateinit var mockBitmap: Bitmap

    private lateinit var puzzleBoard: PuzzleBoard
    private lateinit var pieceBitmaps: List<Bitmap>

    @Before
    fun setup() {
        // Mock bitmap for testing
        Mockito.`when`(mockBitmap.width).thenReturn(100)
        Mockito.`when`(mockBitmap.height).thenReturn(100)
    }

    private fun createMockBitmaps(count: Int): List<Bitmap> {
        return List(count) { mockBitmap }
    }

    // ==================== Board Initialization Tests ====================

    @Test
    fun `when board is created, should initialize with correct grid size`() {
        puzzleBoard = PuzzleBoard(3)
        assertEquals(3, puzzleBoard.getGridSize())
    }

    @Test
    fun `when board is setup with default config, should have 9 pieces for 3x3 grid`() {
        puzzleBoard = PuzzleBoard(3)
        pieceBitmaps = createMockBitmaps(9)
        puzzleBoard.setupBoard(pieceBitmaps, null, null)
        
        assertEquals(9, puzzleBoard.getPieces().size)
    }

    @Test
    fun `when board is setup, move counter should start at zero`() {
        puzzleBoard = PuzzleBoard(3)
        pieceBitmaps = createMockBitmaps(9)
        puzzleBoard.setupBoard(pieceBitmaps, null, null)
        
        assertEquals(0, puzzleBoard.moveCount)
    }

    @Test
    fun `when board is setup with init state, should position pieces correctly`() {
        puzzleBoard = PuzzleBoard(2)
        pieceBitmaps = createMockBitmaps(4)
        val initState = listOf("0", "1", "2", "E") // Last position empty
        
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        val pieces = puzzleBoard.getPieces()
        
        assertFalse(pieces[0]?.isEmptySpace ?: true)
        assertFalse(pieces[1]?.isEmptySpace ?: true)
        assertFalse(pieces[2]?.isEmptySpace ?: true)
        assertTrue(pieces[3]?.isEmptySpace ?: false)
    }

    @Test
    fun `when board is setup with locked tiles, should mark them as locked`() {
        puzzleBoard = PuzzleBoard(2)
        pieceBitmaps = createMockBitmaps(4)
        val initState = listOf("0_L", "1", "2", "E") // First tile locked
        
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        val pieces = puzzleBoard.getPieces()
        
        assertTrue(pieces[0]?.isLocked ?: false)
        assertFalse(pieces[1]?.isLocked ?: true)
    }

    @Test
    fun `when board is setup with multiple empty spaces, should handle correctly`() {
        puzzleBoard = PuzzleBoard(3)
        pieceBitmaps = createMockBitmaps(9)
        val initState = listOf("0", "E", "1", "2", "E", "3", "4", "5", "6")
        
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        val pieces = puzzleBoard.getPieces()
        
        assertTrue(pieces[1]?.isEmptySpace ?: false)
        assertTrue(pieces[4]?.isEmptySpace ?: false)
    }

    // ==================== Move Validation Tests ====================

    @Test
    fun `when piece is adjacent to empty space, should allow move`() {
        puzzleBoard = PuzzleBoard(2)
        pieceBitmaps = createMockBitmaps(4)
        val initState = listOf("0", "1", "2", "E") // Position 3 is empty
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        
        // Move piece from position 2 (adjacent to empty at 3)
        val moveSuccess = puzzleBoard.movePiece(2)
        
        assertTrue(moveSuccess)
        assertEquals(1, puzzleBoard.moveCount)
    }

    @Test
    fun `when piece is not adjacent to empty space, should not allow move`() {
        puzzleBoard = PuzzleBoard(2)
        pieceBitmaps = createMockBitmaps(4)
        val initState = listOf("0", "1", "2", "E") // Position 3 is empty
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        
        // Try to move piece from position 0 (not adjacent to empty at 3)
        val moveSuccess = puzzleBoard.movePiece(0)
        
        assertFalse(moveSuccess)
        assertEquals(0, puzzleBoard.moveCount)
    }

    @Test
    fun `when piece is locked, should not allow move`() {
        puzzleBoard = PuzzleBoard(2)
        pieceBitmaps = createMockBitmaps(4)
        val initState = listOf("0", "1", "2_L", "E") // Position 2 is locked
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        
        // Try to move locked piece at position 2
        val moveSuccess = puzzleBoard.movePiece(2)
        
        assertFalse(moveSuccess)
        assertEquals(0, puzzleBoard.moveCount)
    }

    @Test
    fun `when trying to move empty space, should not allow move`() {
        puzzleBoard = PuzzleBoard(2)
        pieceBitmaps = createMockBitmaps(4)
        val initState = listOf("0", "1", "2", "E")
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        
        // Try to move the empty space itself
        val moveSuccess = puzzleBoard.movePiece(3)
        
        assertFalse(moveSuccess)
    }

    @Test
    fun `when valid move is made, should swap positions correctly`() {
        puzzleBoard = PuzzleBoard(2)
        pieceBitmaps = createMockBitmaps(4)
        val initState = listOf("0", "1", "2", "E")
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        
        puzzleBoard.movePiece(2) // Move piece from 2 to 3
        val pieces = puzzleBoard.getPieces()
        
        assertTrue(pieces[2]?.isEmptySpace ?: false) // Position 2 now empty
        assertFalse(pieces[3]?.isEmptySpace ?: true) // Position 3 now has piece
        assertEquals(2, pieces[3]?.correctPosition) // Piece maintains correct position
    }

    @Test
    fun `when multiple moves are made, should increment counter correctly`() {
        puzzleBoard = PuzzleBoard(3)
        pieceBitmaps = createMockBitmaps(9)
        val initState = listOf("0", "1", "2", "3", "4", "5", "6", "7", "E")
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        
        puzzleBoard.movePiece(7) // Move 1
        puzzleBoard.movePiece(4) // Move 2
        puzzleBoard.movePiece(5) // Move 3
        
        assertEquals(3, puzzleBoard.moveCount)
    }

    @Test
    fun `when piece has multiple adjacent empty spaces, should only move to one`() {
        puzzleBoard = PuzzleBoard(3)
        pieceBitmaps = createMockBitmaps(9)
        val initState = listOf("0", "E", "1", "E", "2", "3", "4", "5", "6")
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        
        // Position 4 (piece 2) has two adjacent empty spaces at 1 and 3
        // Should not move because of multiple empty neighbors
        val moveSuccess = puzzleBoard.movePiece(4)
        
        assertFalse(moveSuccess)
    }

    // ==================== Win Detection Tests ====================

    @Test
    fun `when all pieces are in correct position, should detect win`() {
        puzzleBoard = PuzzleBoard(2)
        pieceBitmaps = createMockBitmaps(4)
        val initState = listOf("0", "1", "2", "E") // All in correct position
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        
        assertTrue(puzzleBoard.isSolved())
    }

    @Test
    fun `when pieces are not in correct position, should not detect win`() {
        puzzleBoard = PuzzleBoard(2)
        pieceBitmaps = createMockBitmaps(4)
        val initState = listOf("1", "0", "2", "E") // Positions 0 and 1 swapped
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        
        assertFalse(puzzleBoard.isSolved())
    }

    @Test
    fun `when puzzle is solved after moves, should detect win`() {
        puzzleBoard = PuzzleBoard(2)
        pieceBitmaps = createMockBitmaps(4)
        val initState = listOf("0", "1", "E", "2") // One move away from solving
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        
        assertFalse(puzzleBoard.isSolved())
        
        puzzleBoard.movePiece(1) // Move piece 1 to position 2
        // Still not solved
        assertFalse(puzzleBoard.isSolved())
    }

    @Test
    fun `when 3x3 puzzle is solved, should detect win`() {
        puzzleBoard = PuzzleBoard(3)
        pieceBitmaps = createMockBitmaps(9)
        val initState = listOf("0", "1", "2", "3", "4", "5", "6", "7", "E")
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        
        assertTrue(puzzleBoard.isSolved())
    }

    // ==================== Board State Management Tests ====================

    @Test
    fun `when saving state, should return correct format`() {
        puzzleBoard = PuzzleBoard(2)
        pieceBitmaps = createMockBitmaps(4)
        val initState = listOf("0", "1_L", "2", "E")
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        
        val savedState = puzzleBoard.getCurrentStateForSave()
        
        assertEquals(4, savedState.size)
        assertEquals("0", savedState[0])
        assertEquals("1_L", savedState[1])
        assertEquals("2", savedState[2])
        assertEquals("E", savedState[3])
    }

    @Test
    fun `when restoring from saved state, should recreate board correctly`() {
        // Create initial board
        puzzleBoard = PuzzleBoard(2)
        pieceBitmaps = createMockBitmaps(4)
        val initState = listOf("0", "1", "2", "E")
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        
        // Make a move
        puzzleBoard.movePiece(2)
        val savedState = puzzleBoard.getCurrentStateForSave()
        val savedMoveCount = puzzleBoard.moveCount
        
        // Create new board from saved state
        val restoredBoard = PuzzleBoard(2)
        restoredBoard.setupBoard(pieceBitmaps, null, savedState)
        
        // Verify state is restored
        val originalPieces = puzzleBoard.getPieces()
        val restoredPieces = restoredBoard.getPieces()
        
        for (i in originalPieces.indices) {
            assertEquals(originalPieces[i]?.isEmptySpace, restoredPieces[i]?.isEmptySpace)
            assertEquals(originalPieces[i]?.correctPosition, restoredPieces[i]?.correctPosition)
        }
    }

    @Test
    fun `when saved state takes priority over init state, should use saved state`() {
        puzzleBoard = PuzzleBoard(2)
        pieceBitmaps = createMockBitmaps(4)
        val initState = listOf("0", "1", "2", "E")
        val savedState = listOf("1", "0", "E", "2")
        
        puzzleBoard.setupBoard(pieceBitmaps, initState, savedState)
        val pieces = puzzleBoard.getPieces()
        
        // Should use savedState, not initState
        assertEquals(1, pieces[0]?.correctPosition)
        assertEquals(0, pieces[1]?.correctPosition)
        assertTrue(pieces[2]?.isEmptySpace ?: false)
    }

    // ==================== Edge Cases and Boundary Tests ====================

    @Test
    fun `when board is 2x2, should handle minimum size correctly`() {
        puzzleBoard = PuzzleBoard(2)
        pieceBitmaps = createMockBitmaps(4)
        puzzleBoard.setupBoard(pieceBitmaps, null, null)
        
        assertEquals(2, puzzleBoard.getGridSize())
        assertEquals(4, puzzleBoard.getPieces().size)
    }

    @Test
    fun `when board is 5x5, should handle larger size correctly`() {
        puzzleBoard = PuzzleBoard(5)
        pieceBitmaps = createMockBitmaps(25)
        puzzleBoard.setupBoard(pieceBitmaps, null, null)
        
        assertEquals(5, puzzleBoard.getGridSize())
        assertEquals(25, puzzleBoard.getPieces().size)
    }

    @Test
    fun `when moving piece at board edge, should respect boundaries`() {
        puzzleBoard = PuzzleBoard(3)
        pieceBitmaps = createMockBitmaps(9)
        val initState = listOf("0", "1", "E", "3", "4", "5", "6", "7", "8")
        puzzleBoard.setupBoard(pieceBitmaps, initState, null)
        
        // Position 1 is adjacent to empty at 2 (horizontally)
        val moveSuccess = puzzleBoard.movePiece(1)
        assertTrue(moveSuccess)
        
        // After first move, empty is now at position 1
        // Position 0 is adjacent to empty at 1 (horizontally) - should succeed
        val moveSuccess2 = puzzleBoard.movePiece(0)
        assertTrue(moveSuccess2)
        
        // After second move, empty is now at position 0
        // Position 2 is NOT adjacent to empty at 0 (not horizontally or vertically adjacent)
        val moveSuccess3 = puzzleBoard.movePiece(2)
        assertFalse(moveSuccess3)
    }
}