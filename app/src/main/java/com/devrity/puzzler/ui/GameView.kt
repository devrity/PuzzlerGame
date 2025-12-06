package com.devrity.puzzler.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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
    private var totalSize: Float = 0f
    private var offsetX: Float = 0f
    private var offsetY: Float = 0f

    private var showGridLines: Boolean = true
    private var isInteractionEnabled: Boolean = false

    // Paint objects for drawing
    private val piecePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val emptySpacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Constants.GRID_LINE_COLOR
        strokeWidth = Constants.GRID_LINE_WIDTH
        style = Paint.Style.STROKE
    }
    private val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Constants.GRID_LINE_COLOR
        strokeWidth = Constants.GRID_OUTER_BORDER_WIDTH
        style = Paint.Style.STROKE
    }

    // Reusable object for drawing to avoid allocations in onDraw
    private val pieceDrawingRect = RectF()

    // Touch handling
    private var touchStartX: Float = 0f
    private var touchStartY: Float = 0f
    private val swipeThreshold = 50f // Minimum distance for swipe

    // Callbacks
    var onPieceMovedListener: ((isSolved: Boolean) -> Unit)? = null
    var onPieceMoved: (() -> Unit)? = null  // For sound/haptic on successful move

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
     * Uses the smaller dimension to ensure the puzzle fits completely.
     */
    private fun calculatePieceSize() {
        val size = minOf(width, height).toFloat()
        totalSize = size
        pieceSize = size / gridSize

        // Center the puzzle in the view
        offsetX = (width - totalSize) / 2f
        offsetY = (height - totalSize) / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (pieces.isEmpty()) return

        // Draw each piece
        for (piece in pieces) {
            piece?.let {
                val row = it.currentPosition / gridSize
                val col = it.currentPosition % gridSize
                val left = offsetX + col * pieceSize
                val top = offsetY + row * pieceSize
                val right = left + pieceSize
                val bottom = top + pieceSize

                if (it.isEmptySpace) {
                    // Draw white square for empty space
                    canvas.drawRect(left, top, right, bottom, emptySpacePaint)
                } else if (it.bitmap != null) {
                    // Draw the piece bitmap, slightly inset to prevent overlap with grid lines
                    val inset = Constants.GRID_LINE_WIDTH / 2f
                    pieceDrawingRect.set(left + inset, top + inset, right - inset, bottom - inset)
                    canvas.drawBitmap(
                        it.bitmap,
                        null,
                        pieceDrawingRect,
                        piecePaint
                    )
                }
            }
        }

        // Draw grid lines on top of pieces
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

        // Draw outer border
        canvas.drawRect(offsetX, offsetY, offsetX + totalSize, offsetY + totalSize, outerBorderPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
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
                    performClick()
                } else {
                    // Treat as swipe
                    handleSwipe(touchStartX, touchStartY, deltaX, deltaY)
                }
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        handleTap(touchStartX, touchStartY)
        return true
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
                onPieceMoved?.invoke()  // Trigger sound/haptic feedback
                onPieceMovedListener?.invoke(board.isSolved())
            }
        }
    }
}
