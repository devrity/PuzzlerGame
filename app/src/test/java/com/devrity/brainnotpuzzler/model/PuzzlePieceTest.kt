package com.devrity.brainnotpuzzler.model

import android.graphics.Bitmap
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for PuzzlePiece data class
 */
@RunWith(MockitoJUnitRunner::class)
class PuzzlePieceTest {

    @Mock
    private lateinit var mockBitmap: Bitmap

    private lateinit var piece: PuzzlePiece

    @Before
    fun setup() {
        piece = PuzzlePiece(
            id = 0,
            correctPosition = 5,
            currentPosition = 3,
            bitmap = mockBitmap,
            isEmptySpace = false,
            isLocked = false
        )
    }

    @Test
    fun `when piece is in correct position, isInCorrectPosition should return true`() {
        val correctPiece = piece.copy(currentPosition = 5) // Same as correctPosition
        
        assertTrue(correctPiece.isInCorrectPosition())
    }

    @Test
    fun `when piece is not in correct position, isInCorrectPosition should return false`() {
        assertFalse(piece.isInCorrectPosition()) // currentPosition=3, correctPosition=5
    }

    @Test
    fun `when piece is created as empty space, should have empty space properties`() {
        val emptyPiece = PuzzlePiece(
            id = 8,
            correctPosition = 8,
            currentPosition = 8,
            bitmap = null,
            isEmptySpace = true
        )
        
        assertTrue(emptyPiece.isEmptySpace)
        assertNull(emptyPiece.bitmap)
    }

    @Test
    fun `when piece is locked, should maintain locked state`() {
        val lockedPiece = piece.copy(isLocked = true)
        
        assertTrue(lockedPiece.isLocked)
    }

    @Test
    fun `when piece is copied, should create new instance with updated values`() {
        val copiedPiece = piece.copy(currentPosition = 7)
        
        assertEquals(7, copiedPiece.currentPosition)
        assertEquals(piece.correctPosition, copiedPiece.correctPosition)
        assertEquals(piece.id, copiedPiece.id)
    }

    @Test
    fun `when piece position changes, should update currentPosition`() {
        piece.currentPosition = 10
        
        assertEquals(10, piece.currentPosition)
        assertEquals(5, piece.correctPosition) // correctPosition unchanged
    }

    @Test
    fun `when piece is empty and in correct position, should return true`() {
        val emptyPiece = PuzzlePiece(
            id = 8,
            correctPosition = 8,
            currentPosition = 8,
            bitmap = null,
            isEmptySpace = true
        )
        
        assertTrue(emptyPiece.isInCorrectPosition())
    }

    @Test
    fun `when two pieces have same properties, should be equal`() {
        val piece1 = PuzzlePiece(0, 5, 3, mockBitmap, false, false)
        val piece2 = PuzzlePiece(0, 5, 3, mockBitmap, false, false)
        
        assertEquals(piece1, piece2)
    }
}