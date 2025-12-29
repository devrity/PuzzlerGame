package com.devrity.brainnotpuzzler

import android.graphics.Bitmap
import org.mockito.Mockito

/**
 * Utility functions for testing
 */
object TestUtils {
    
    /**
     * Create a mock bitmap for testing purposes
     */
    fun createMockBitmap(width: Int = 100, height: Int = 100): Bitmap {
        val bitmap = Mockito.mock(Bitmap::class.java)
        Mockito.`when`(bitmap.width).thenReturn(width)
        Mockito.`when`(bitmap.height).thenReturn(height)
        return bitmap
    }
    
    /**
     * Create multiple mock bitmaps
     */
    fun createMockBitmaps(count: Int, width: Int = 100, height: Int = 100): List<Bitmap> {
        return List(count) { createMockBitmap(width, height) }
    }
    
    /**
     * Create a simple board state string for testing
     */
    fun createSimpleBoardState(gridSize: Int, emptyPosition: Int): List<String> {
        return (0 until gridSize * gridSize).map { position ->
            if (position == emptyPosition) "E" else position.toString()
        }
    }
    
    /**
     * Create a solved board state
     */
    fun createSolvedBoardState(gridSize: Int): List<String> {
        val totalPieces = gridSize * gridSize
        return (0 until totalPieces - 1).map { it.toString() } + listOf("E")
    }
    
    /**
     * Create a board state with locked tiles
     */
    fun createBoardStateWithLocks(gridSize: Int, lockedPositions: Set<Int>, emptyPosition: Int): List<String> {
        return (0 until gridSize * gridSize).map { position ->
            when {
                position == emptyPosition -> "E"
                position in lockedPositions -> "${position}_L"
                else -> position.toString()
            }
        }
    }
}