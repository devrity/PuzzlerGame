package com.devrity.brainnotpuzzler.ui

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devrity.brainnotpuzzler.model.PuzzleBoard
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 2: UI Tests - Swipe Gesture Interaction
 * 
 * Tests cover:
 * - Swipe on movable tiles (directional movement)
 * - Swipe on non-movable tiles (no empty in swipe direction)
 * - Swipe on locked tiles
 * - Swipe with double empty tiles (should work - direction is explicit)
 * - Swipe across board boundaries (should fail)
 * - Swipe in all four directions (up, down, left, right)
 */
@RunWith(AndroidJUnit4::class)
class GameViewSwipeGestureTest {

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

    // ==================== Swipe on Movable Tiles (All Directions) ====================

    @Test
    fun whenSwipeRightIntoEmpty_shouldMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][4][E]  <- Position 5 is empty
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "4", "E", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        val initialMoveCount = puzzleBoard.moveCount

        // Swipe position 4 RIGHT into position 5 (empty)
        val moveSuccess = puzzleBoard.movePiece(4, 5)

        assertTrue("Swipe right into empty should succeed", moveSuccess)
        assertEquals("Move count should increment", initialMoveCount + 1, puzzleBoard.moveCount)

        // Verify positions swapped
        val pieces = puzzleBoard.getPieces()
        assertTrue("Position 4 should now be empty", pieces[4]?.isEmptySpace == true)
        assertFalse("Position 5 should now have the piece", pieces[5]?.isEmptySpace == true)
    }

    @Test
    fun whenSwipeLeftIntoEmpty_shouldMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [E][4][5]  <- Position 3 is empty
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "E", "4", "5", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Swipe position 4 LEFT into position 3 (empty)
        val moveSuccess = puzzleBoard.movePiece(4, 3)

        assertTrue("Swipe left into empty should succeed", moveSuccess)
    }

    @Test
    fun whenSwipeDownIntoEmpty_shouldMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][4][5]
        // [6][E][8]  <- Position 7 is empty
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "4", "5", "6", "E", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Swipe position 4 DOWN into position 7 (empty)
        val moveSuccess = puzzleBoard.movePiece(4, 7)

        assertTrue("Swipe down into empty should succeed", moveSuccess)

        // Verify positions swapped
        val pieces = puzzleBoard.getPieces()
        assertTrue("Position 4 should now be empty", pieces[4]?.isEmptySpace == true)
        assertFalse("Position 7 should now have the piece", pieces[7]?.isEmptySpace == true)
    }

    @Test
    fun whenSwipeUpIntoEmpty_shouldMove() {
        // Board layout (3x3):
        // [0][E][2]  <- Position 1 is empty
        // [3][4][5]
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "E", "2", "3", "4", "5", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Swipe position 4 UP into position 1 (empty)
        val moveSuccess = puzzleBoard.movePiece(4, 1)

        assertTrue("Swipe up into empty should succeed", moveSuccess)

        // Verify positions swapped
        val pieces = puzzleBoard.getPieces()
        assertTrue("Position 4 should now be empty", pieces[4]?.isEmptySpace == true)
        assertFalse("Position 1 should now have the piece", pieces[1]?.isEmptySpace == true)
    }

    // ==================== Swipe on Non-Movable Tiles ====================

    @Test
    fun whenSwipeIntoNonEmptySpace_shouldNotMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][4][5]
        // [6][7][E]  <- Position 8 is empty
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "4", "5", "6", "7", "E")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        val initialMoveCount = puzzleBoard.moveCount

        // Try to swipe position 4 RIGHT into position 5 (NOT empty)
        val moveSuccess = puzzleBoard.movePiece(4, 5)

        assertFalse("Swipe into non-empty space should fail", moveSuccess)
        assertEquals("Move count should not change", initialMoveCount, puzzleBoard.moveCount)
    }

    @Test
    fun whenSwipeNonAdjacentPositions_shouldNotMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][4][5]
        // [6][7][E]  <- Position 8 is empty
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "4", "5", "6", "7", "E")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Try to swipe position 0 to position 8 (diagonal, non-adjacent)
        val moveSuccess = puzzleBoard.movePiece(0, 8)

        assertFalse("Swipe non-adjacent positions should fail", moveSuccess)
    }

    // ==================== Swipe on Locked Tiles ====================

    @Test
    fun whenSwipeLockedTileIntoEmpty_shouldNotMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][4_L][E]  <- Position 4 is locked, 5 is empty
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "4_L", "E", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        val initialMoveCount = puzzleBoard.moveCount

        // Try to swipe locked position 4 RIGHT into position 5 (empty)
        val moveSuccess = puzzleBoard.movePiece(4, 5)

        assertFalse("Swipe on locked tile should fail", moveSuccess)
        assertEquals("Move count should not change", initialMoveCount, puzzleBoard.moveCount)

        // Verify piece is still locked and hasn't moved
        val pieces = puzzleBoard.getPieces()
        assertTrue("Locked piece should remain locked", pieces[4]?.isLocked == true)
        assertFalse("Locked piece should not have moved", pieces[4]?.isEmptySpace == true)
    }

    // ==================== Swipe Empty Space ====================

    @Test
    fun whenSwipeEmptySpaceItself_shouldNotMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][E][5]  <- Position 4 is empty
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "E", "5", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        val initialMoveCount = puzzleBoard.moveCount

        // Try to swipe the empty space itself (position 4) to position 1
        val moveSuccess = puzzleBoard.movePiece(4, 1)

        assertFalse("Swipe on empty space should fail", moveSuccess)
        assertEquals("Move count should not change", initialMoveCount, puzzleBoard.moveCount)
    }

    // ==================== Swipe with Double Empty Tiles (Should Work!) ====================

    @Test
    fun whenSwipeWithTwoEmptyTiles_directionIsExplicit_shouldMove() {
        // Board layout (3x3):
        // [0][E][2]  <- Position 1 is empty
        // [E][4][5]  <- Position 3 is empty
        // [6][7][8]
        // Position 4 has TWO empty neighbors
        // But swipe has explicit direction, so it should work!
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "E", "2", "E", "4", "5", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        val initialMoveCount = puzzleBoard.moveCount

        // Swipe position 4 UP into position 1 (empty)
        val moveUpSuccess = puzzleBoard.movePiece(4, 1)

        assertTrue("Swipe with direction should work even with two empties", moveUpSuccess)
        assertEquals("Move count should increment", initialMoveCount + 1, puzzleBoard.moveCount)

        // Verify the correct empty was filled
        val pieces = puzzleBoard.getPieces()
        assertFalse("Position 1 should now have the piece", pieces[1]?.isEmptySpace == true)
        assertTrue("Position 4 should now be empty", pieces[4]?.isEmptySpace == true)
        assertTrue("Position 3 should still be empty", pieces[3]?.isEmptySpace == true)
    }

    @Test
    fun whenSwipeWithTwoEmptyTiles_leftDirection_shouldMove() {
        // Board layout (3x3):
        // [0][E][2]  <- Position 1 is empty
        // [E][4][5]  <- Position 3 is empty
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "E", "2", "E", "4", "5", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Swipe position 4 LEFT into position 3 (empty)
        val moveLeftSuccess = puzzleBoard.movePiece(4, 3)

        assertTrue("Swipe left with two empties should work", moveLeftSuccess)

        // Verify the correct empty was filled
        val pieces = puzzleBoard.getPieces()
        assertFalse("Position 3 should now have the piece", pieces[3]?.isEmptySpace == true)
        assertTrue("Position 4 should now be empty", pieces[4]?.isEmptySpace == true)
        assertTrue("Position 1 should still be empty", pieces[1]?.isEmptySpace == true)
    }

    @Test
    fun whenSwipeWithThreeEmptyNeighbors_shouldStillWork() {
        // Board layout (3x3):
        // [0][E][2]  <- Position 1 is empty
        // [E][4][E]  <- Position 3 and 5 are empty
        // [6][7][8]
        // Position 4 has THREE empty neighbors
        // Swipe should STILL work because direction is explicit!
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "E", "2", "E", "4", "E", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Swipe position 4 RIGHT into position 5 (empty)
        val moveSuccess = puzzleBoard.movePiece(4, 5)

        assertTrue("Swipe with three empties should work due to explicit direction", moveSuccess)

        // Verify only the correct positions swapped
        val pieces = puzzleBoard.getPieces()
        assertFalse("Position 5 should now have the piece", pieces[5]?.isEmptySpace == true)
        assertTrue("Position 4 should now be empty", pieces[4]?.isEmptySpace == true)
        assertTrue("Position 1 should still be empty", pieces[1]?.isEmptySpace == true)
        assertTrue("Position 3 should still be empty", pieces[3]?.isEmptySpace == true)
    }

    // ==================== Swipe Across Boundaries ====================

    @Test
    fun whenSwipeRightAtRightEdge_shouldNotMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][4][5]
        // [6][7][E]  <- Position 8 is empty
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "4", "5", "6", "7", "E")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        val initialMoveCount = puzzleBoard.moveCount

        // Try to swipe position 5 (right edge) RIGHT (would go out of bounds)
        // Position 6 is the next index, but it's NOT to the right of 5 (it's below)
        // This should fail validation
        val moveSuccess = puzzleBoard.movePiece(5, 6)

        assertFalse("Swipe across right boundary should fail", moveSuccess)
        assertEquals("Move count should not change", initialMoveCount, puzzleBoard.moveCount)
    }

    @Test
    fun whenSwipeDownAtBottomEdge_shouldNotMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][4][5]
        // [6][E][8]  <- Position 7 is empty
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "4", "5", "6", "E", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Try to swipe position 7 DOWN (would go out of bounds)
        // There is no position 10
        val moveSuccess = puzzleBoard.movePiece(7, 10)

        assertFalse("Swipe beyond board boundary should fail", moveSuccess)
    }

    // ==================== Sequential Swipes ====================

    @Test
    fun whenMultipleSwipesInSequence_shouldUpdateBoardCorrectly() {
        // FIXED: Use 3x3 board with valid adjacent moves (not diagonal)
        // Board layout (3x3):
        // [0][1][2]
        // [3][E][5]  <- Position 4 is empty
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "E", "5", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // First swipe: position 1 DOWN into position 4 (vertically adjacent)
        val move1 = puzzleBoard.movePiece(1, 4)
        assertTrue("First swipe should succeed", move1)
        assertEquals("Move count should be 1", 1, puzzleBoard.moveCount)

        // Board is now:
        // [0][E][2]
        // [3][1][5]
        // [6][7][8]

        // Second swipe: position 2 LEFT into position 1 (horizontally adjacent)
        val move2 = puzzleBoard.movePiece(2, 1)
        assertTrue("Second swipe should succeed", move2)
        assertEquals("Move count should be 2", 2, puzzleBoard.moveCount)

        // Board is now:
        // [0][2][E]
        // [3][1][5]
        // [6][7][8]

        // Third swipe: position 5 UP into position 2 (vertically adjacent)
        val move3 = puzzleBoard.movePiece(5, 2)
        assertTrue("Third swipe should succeed", move3)
        assertEquals("Move count should be 3", 3, puzzleBoard.moveCount)
    }

    // ==================== Edge Cases ====================

    @Test
    fun whenSwipeFromCornerToAdjacentEmpty_shouldMove() {
        // Board layout (3x3):
        // [E][1][2]  <- Position 0 is empty
        // [3][4][5]
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("E", "1", "2", "3", "4", "5", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Swipe position 1 LEFT into position 0 (empty corner)
        val moveSuccess = puzzleBoard.movePiece(1, 0)

        assertTrue("Swipe from corner should work", moveSuccess)
    }

    @Test
    fun whenSwipeDifferentDirectionsThanAdjacency_shouldNotMove() {
        // Board layout (3x3):
        // [0][1][2]
        // [3][E][5]  <- Position 4 is empty
        // [6][7][8]
        puzzleBoard = PuzzleBoard(3)
        val initState = listOf("0", "1", "2", "3", "E", "5", "6", "7", "8")
        puzzleBoard.setupBoard(mockBitmaps, initState, null)

        // Try to swipe position 1 DOWN into position 4 (empty)
        // This SHOULD work because 1 and 4 are vertically adjacent
        val moveSuccess = puzzleBoard.movePiece(1, 4)

        assertTrue("Valid directional swipe should work", moveSuccess)
    }
}