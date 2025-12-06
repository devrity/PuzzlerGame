package com.devrity.puzzler.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.devrity.puzzler.model.PuzzleBoard
import com.devrity.puzzler.model.PuzzlePiece
import com.devrity.puzzler.util.Constants
import kotlin.math.abs

/**
 * Custom view for displaying and interacting with the puzzle game.
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var puzzleBoard: PuzzleBoard? = null
    private var pieces: Array<PuzzlePiece?> = emptyArray()
    private var gridSize: Int = Constants.DEFAULT_GRID_SIZE
    private var pieceSize: Float = 0f
    
    private var showGridLines: Boolean = true
    private var isInteractionEnabled: Boolean = false
    
    // Paint objects for drawing
    private val piecePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Constants.GRID_LINE_COLOR
        strokeWidth = Constants.GRID_LINE_WIDTH
        style = Paint.Style.STROKE
    }
    
    // Touch handling
    private var touchStartX: Float = 0f
    private var touchStartY: Float = 0f
    private val swipeThreshold = 50f // Minimum distance for swipe
    
    // Callback for piece moves
    var onPieceMovedListener: ((isSolved: Boolean) -> Unit)? = null
    
    /**
     * Initialize the game with a puzzle board.
     */
    fun setPuzzleBoard(board: PuzzleBoard) {
        this.puzzleBoard = board
        this.gridSize = board.getGridSize()
        this.pieces = board.getPieces()
        calculatePieceSize()
        invalidate()
    }
    
    /**
     * Update the pieces array and redraw.
     */
    fun updatePieces() {
        puzzleBoard?.let {
            pieces = it.getPieces()
            invalidate()
        }
    }
    
    /**
     * Enable or disable grid lines.
     */
    fun setShowGridLines(show: Boolean) {
        showGridLines = show
        invalidate()
    }
    
    /**
     * Enable or disable user interaction.
     */
    fun setInteractionEnabled(enabled: Boolean) {
        isInteractionEnabled = enabled
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculatePieceSize()
    }
    
    /**
     * Calculate the size of each puzzle piece based on view dimensions.
     */
    private fun calculatePieceSize() {
        val size = minOf(width, height).toFloat()
        pieceSize = size / gridSize
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (pieces.isEmpty()) return
        
        // Center the puzzle in the view
        val totalSize = pieceSize * gridSize
        val offsetX = (width - totalSize) / 2f
        val offsetY = (height - totalSize) / 2f
        
        // Draw each piece
        for (piece in pieces) {
            piece?.let {
                if (!it.isEmptySpace && it.bitmap != null) {
                    val row = it.currentPosition / gridSize
                    val col = it.currentPosition % gridSize
                    val left = offsetX + col * pieceSize
                    val top = offsetY + row * pieceSize
                    
                    canvas.drawBitmap(
                        it.bitmap,
                        null,
                        android.graphics.RectF(
                            left,
                            top,
                            left + pieceSize,
                            top + pieceSize
                        ),
                        piecePaint
                    )
                }
            }
        }
        
        // Draw grid lines if enabled
        if (showGridLines) {
            for (i in 0..gridSize) {
                // Vertical lines
                val x = offsetX + i * pieceSize
                canvas.drawLine(x, offsetY, x, offsetY + totalSize, gridPaint)
                
                // Horizontal lines
                val y = offsetY + i * pieceSize
                canvas.drawLine(offsetX, y, offsetX + totalSize, y, gridPaint)
            }
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isInteractionEnabled || puzzleBoard == null) {
            return super.onTouchEvent(event)
        }
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y
                return true
            }
            
            MotionEvent.ACTION_UP -> {
                val deltaX = event.x - touchStartX
                val deltaY = event.y - touchStartY
                val distance = Math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toFloat()
                
                if (distance < swipeThreshold) {
                    // Treat as tap
                    handleTap(event.x, event.y)
                } else {
                    // Treat as swipe
                    handleSwipe(touchStartX, touchStartY, deltaX, deltaY)
                }
                return true
            }
        }
        
        return super.onTouchEvent(event)
    }
    
    /**
     * Handle tap gesture on a piece.
     */
    private fun handleTap(x: Float, y: Float) {
        val position = getTouchedPosition(x, y)
        if (position != -1) {
            movePiece(position)
        }
    }
    
    /**
     * Handle swipe gesture.
     */
    private fun handleSwipe(startX: Float, startY: Float, deltaX: Float, deltaY: Float) {
        val position = getTouchedPosition(startX, startY)
        if (position == -1) return
        
        // Determine swipe direction
        val absDeltaX = abs(deltaX)
        val absDeltaY = abs(deltaY)
        
        val board = puzzleBoard ?: return
        val emptyPos = board.getEmptyPosition()
        val row = position / gridSize
        val col = position % gridSize
        val emptyRow = emptyPos / gridSize
        val emptyCol = emptyPos % gridSize
        
        // Check if swipe direction matches empty space direction
        if (absDeltaX > absDeltaY) {
            // Horizontal swipe
            if (deltaX > 0 && emptyRow == row && emptyCol == col - 1) {
                // Swipe right, empty is on left
                movePiece(position)
            } else if (deltaX < 0 && emptyRow == row && emptyCol == col + 1) {
                // Swipe left, empty is on right
                movePiece(position)
            }
        } else {
            // Vertical swipe
            if (deltaY > 0 && emptyCol == col && emptyRow == row - 1) {
                // Swipe down, empty is above
                movePiece(position)
            } else if (deltaY < 0 && emptyCol == col && emptyRow == row + 1) {
                // Swipe up, empty is below
                movePiece(position)
            }
        }
    }
    
    /**
     * Get the position of the piece at given coordinates.
     */
    private fun getTouchedPosition(x: Float, y: Float): Int {
        val totalSize = pieceSize * gridSize
        val offsetX = (width - totalSize) / 2f
        val offsetY = (height - totalSize) / 2f
        
        // Check if touch is within puzzle bounds
        if (x < offsetX || x > offsetX + totalSize || y < offsetY || y > offsetY + totalSize) {
            return -1
        }
        
        val col = ((x - offsetX) / pieceSize).toInt()
        val row = ((y - offsetY) / pieceSize).toInt()
        
        if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) {
            return -1
        }
        
        return row * gridSize + col
    }
    
    /**
     * Attempt to move a piece at given position.
     */
    private fun movePiece(position: Int) {
        puzzleBoard?.let { board ->
            if (board.movePiece(position)) {
                updatePieces()
                onPieceMovedListener?.invoke(board.isSolved())
            }
        }
    }
}
