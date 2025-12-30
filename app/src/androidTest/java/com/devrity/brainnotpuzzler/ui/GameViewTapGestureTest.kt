package com.devrity.brainnotpuzzler.ui

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devrity.brainnotpuzzler.model.PuzzleBoard
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 2: UI Tests - Tap Gesture Interaction
 * 
 * Tests cover:
 * - Tap on movable tiles (single empty neighbor)
 * - Tap on non-movable tiles (no empty neighbors)
 * - Tap on locked tiles
 * - Tap with double empty tiles (ambiguity - should fail)
 * - Tap with double empty tiles but only one possibility (should succeed)
 */
@RunWith(AndroidJUnit4::class)
class GameViewTapGestureTest {

    private lateinit var puzzleBoard: PuzzleBoard
    private lateinit var mockBitmaps: List<Bitmap>

    @Before
    fun setup() {
        // Create mock bitmaps for testing
        mockBitmaps = List(9) { createMockBitmap(100, 100) }
    }

    private fun createMockBitmap(width: Int, height: Int): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    // ==================== Tap on Movable Tiles ====================

    @Test
    fun whenTapOnTileWithOneEmptyNeighbor_shouldMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][4][E]  <- Position 5 is empty
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "4", "E", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        val initialMoveCount = puzzleBoard.moveCount

        // Tap on position 4 (adjacent to empty at 5)
        val moveSuccess = puzzleBoard.movePiece(4)

        assertTrue("Tap on tile with one empty neighbor should succeed", moveSuccess)
        assertEquals("Move count should increment", initialMoveCount + 1, puzzleBoard.moveCount)

        // Verify positions swapped
        val pieces = puzzleBoard.getPieces()
        assertTrue("Position 4 should now be empty", pieces[4]?.isEmptySpace == true)
        assertFalse("Position 5 should now have the piece", pieces[5]?.isEmptySpace == true)
    }

    @Test
    fun whenTapOnTileVerticallyAdjacentToEmpty_shouldMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][E][5]  <- Position 4 is empty
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "E", "5", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Tap on position 1 (vertically adjacent to empty at 4)
        val moveSuccess = puzzleBoard.movePiece(1)

        assertTrue("Tap on vertically adjacent tile should succeed", moveSuccess)
        
        // Verify positions swapped
        val pieces = puzzleBoard.getPieces()
        assertTrue("Position 1 should now be empty", pieces[1]?.isEmptySpace == true)
        assertFalse("Position 4 should now have the piece", pieces[4]?.isEmptySpace == true)
    }

    @Test
    fun whenTapOnCornerTileAdjacentToEmpty_shouldMove() {
        // Board layout (3x3):
        // [E][1][2]  <- Position 0 is empty
        // [3][4][5]
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("E", "1", "2", "3", "4", "5", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Tap on position 1 (corner tile adjacent to empty)
        val moveSuccess = puzzleBoard.movePiece(1)

        assertTrue("Tap on corner tile adjacent to empty should succeed", moveSuccess)
    }

    // ==================== Tap on Non-Movable Tiles ====================

    @Test
    fun whenTapOnTileWithNoEmptyNeighbor_shouldNotMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][4][5]
        // [6][7][E]  <- Position 8 is empty
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "4", "5", "6", "7", "E")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        val initialMoveCount = puzzleBoard.moveCount

        // Tap on position 0 (not adjacent to empty at 8)
        val moveSuccess = puzzleBoard.movePiece(0)

        assertFalse("Tap on tile with no empty neighbor should fail", moveSuccess)
        assertEquals("Move count should not change", initialMoveCount, puzzleBoard.moveCount)
    }

    @Test
    fun whenTapOnTileFarFromEmpty_shouldNotMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][E][5]  <- Position 4 is empty
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "E", "5", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Tap on position 8 (diagonal from empty, not adjacent)
        val moveSuccess = puzzleBoard.movePiece(8)

        assertFalse("Tap on diagonal tile should fail", moveSuccess)
    }

    // ==================== Tap on Locked Tiles ====================

    @Test
    fun whenTapOnLockedTileAdjacentToEmpty_shouldNotMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][4_L][E]  <- Position 4 is locked, 5 is empty
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "4_L", "E", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        val initialMoveCount = puzzleBoard.moveCount

        // Tap on position 4 (locked tile)
        val moveSuccess = puzzleBoard.movePiece(4)

        assertFalse("Tap on locked tile should fail even if adjacent to empty", moveSuccess)
        assertEquals("Move count should not change", initialMoveCount, puzzleBoard.moveCount)

        // Verify piece is still locked
        val pieces = puzzleBoard.getPieces()
        assertTrue("Locked piece should remain locked", pieces[4]?.isLocked == true)
    }

    // ==================== Tap on Empty Tile ====================

    @Test
    fun whenTapOnEmptySpace_shouldNotMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][4][E]  <- Position 5 is empty
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "4", "E", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        val initialMoveCount = puzzleBoard.moveCount

        // Tap on the empty space itself (position 5)
        val moveSuccess = puzzleBoard.movePiece(5)

        assertFalse("Tap on empty space should fail", moveSuccess)
        assertEquals("Move count should not change", initialMoveCount, puzzleBoard.moveCount)
    }

    // ==================== Tap with Double Empty Tiles (Ambiguity) ====================

    @Test
    fun whenTapOnTileWithTwoEmptyNeighbors_shouldNotMove() {
        // Board layout (3x3):
        // [0][E][2]  <- Position 1 is empty
        // [E][4][5]  <- Position 3 is empty
        // [6][7][8]
        // Position 4 has TWO empty neighbors (1 and 3) - AMBIGUOUS!
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "E", "2", "E", "4", "5", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        val initialMoveCount = puzzleBoard.moveCount

        // Tap on position 4 (ambiguous - two empty neighbors)
        val moveSuccess = puzzleBoard.movePiece(4)

        assertFalse("Tap with two empty neighbors should fail due to ambiguity", moveSuccess)
        assertEquals("Move count should not change", initialMoveCount, puzzleBoard.moveCount)
    }

    @Test
    fun whenTapOnTileWithThreeEmptyNeighbors_shouldNotMove() {
        // Board layout (3x3):
        // [0][E][2]  <- Position 1 is empty
        // [E][4][E]  <- Position 3 and 5 are empty
        // [6][7][8]
        // Position 4 has THREE empty neighbors - VERY AMBIGUOUS!
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "E", "2", "E", "4", "E", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Tap on position 4 (three empty neighbors)
        val moveSuccess = puzzleBoard.movePiece(4)

        assertFalse("Tap with three empty neighbors should fail", moveSuccess)
    }

    // ==================== Tap with Double Empty BUT Only One Possibility ====================

    @Test
    fun whenTapOnTileWithTwoEmptyButOnlyOnePossibility_shouldMove() {
        // Board layout (3x3):
        // [0][1][E]  <- Position 2 is empty
        // [3][4][5]
        // [6][7][E]  <- Position 8 is empty
        // Position 5 is adjacent to position 2 (only one adjacent empty)
        // Position 8 is NOT adjacent to position 5 (too far)
        // So tapping position 5 should work!
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "E", "3", "4", "5", "6", "7", "E")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        val initialMoveCount = puzzleBoard.moveCount

        // Tap on position 5 (has only ONE adjacent empty at position 2)
        val moveSuccess = puzzleBoard.movePiece(5)

        assertTrue("Tap with one adjacent empty should succeed even with distant empties", moveSuccess)
        assertEquals("Move count should increment", initialMoveCount + 1, puzzleBoard.moveCount)
    }

    @Test
    fun whenTapOnCornerWithTwoEmptyButOnlyOneAdjacent_shouldMove() {
        // Board layout (3x3):
        // [0][E][2]  <- Position 1 is empty
        // [3][4][5]
        // [E][7][8]  <- Position 6 is empty
        // Position 0 is adjacent ONLY to position 1 (not to 6)
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "E", "2", "3", "4", "5", "E", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Tap on position 0 (only one adjacent empty at position 1)
        val moveSuccess = puzzleBoard.movePiece(0)

        assertTrue("Corner tap with one adjacent empty should succeed", moveSuccess)
    }

    // ==================== Edge Cases ====================

    @Test
    fun whenTapOnEdgeTileWithOneEmptyNeighbor_shouldMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][4][E]  <- Position 5 is empty
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "4", "E", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Tap on position 2 (edge tile, adjacent to empty at 5)
        val moveSuccess = puzzleBoard.movePiece(2)

        assertTrue("Edge tile tap should succeed when adjacent to empty", moveSuccess)
    }

    @Test
    fun whenMultipleTapsInSequence_shouldUpdateBoardCorrectly() {
        // Board layout (2x2) for simplicity:
        // [0][1]
        // [2][E]  <- Position 3 is empty
        puzzleBoard = PuzzleBoard(2)
        val mockBitmaps2x2 = List(4) { createMockBitmap(100, 100) }
        val initState = listOf("0", "1", "2", "E")
        puzzleBoard.setupBoard(mockBitmaps2x2, initState, null)

        // First tap: position 2 -> empty at 3
        val move1 = puzzleBoard.movePiece(2)
        assertTrue("First tap should succeed", move1)
        assertEquals("Move count should be 1", 1, puzzleBoard.moveCount)

        // Second tap: position 1 -> empty now at 2
        val move2 = puzzleBoard.movePiece(1)
        assertTrue("Second tap should succeed", move2)
        assertEquals("Move count should be 2", 2, puzzleBoard.moveCount)

        // Third tap: position 0 -> empty now at 1
        val move3 = puzzleBoard.movePiece(0)
        assertTrue("Third tap should succeed", move3)
        assertEquals("Move count should be 3", 3, puzzleBoard.moveCount)
    }
}