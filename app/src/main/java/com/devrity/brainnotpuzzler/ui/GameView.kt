package com.devrity.brainnotpuzzler.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.devrity.brainnotpuzzler.model.PuzzleBoard
import com.devrity.brainnotpuzzler.model.PuzzlePiece
import com.devrity.brainnotpuzzler.util.Constants
import kotlin.math.abs

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var puzzleBoard: PuzzleBoard? = null
    private var pieces: Array<PuzzlePiece?> = emptyArray()
    private var fullImage: Bitmap? = null
    private var isVictoryState: Boolean = false
    private var gridSize: Int = Constants.GRID_SIZE
    private var pieceSize: Float = 0f
    private var totalSize: Float = 0f
    private var offsetX: Float = 0f
    private var offsetY: Float = 0f

    private var showGridLines: Boolean = true
    private var isInteractionEnabled: Boolean = false

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

    private val pieceDrawingRect = RectF()

    private var touchStartX: Float = 0f
    private var touchStartY: Float = 0f
    private val swipeThreshold = 50f

    var onPieceMovedListener: ((isSolved: Boolean) -> Unit)? = null
    var onPieceMoved: (() -> Unit)? = null

    fun setPuzzleBoard(board: PuzzleBoard, image: Bitmap) {
        this.puzzleBoard = board
        this.fullImage = image
        this.pieces = board.getPieces()
        this.isVictoryState = false
        calculatePieceSize()
        invalidate()
    }

    fun displayFullImage() {
        isVictoryState = true
        isInteractionEnabled = false
        invalidate()
    }

    fun updatePieces() {
        puzzleBoard?.let {
            pieces = it.getPieces()
            invalidate()
        }
    }

    fun setShowGridLines(show: Boolean) {
        showGridLines = show
        invalidate()
    }

    fun setInteractionEnabled(enabled: Boolean) {
        isInteractionEnabled = enabled
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculatePieceSize()
    }

    private fun calculatePieceSize() {
        val size = minOf(width, height).toFloat()
        totalSize = size
        pieceSize = size / gridSize
        offsetX = (width - totalSize) / 2f
        offsetY = (height - totalSize) / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isVictoryState && fullImage != null) {
            canvas.drawBitmap(fullImage!!, null, pieceDrawingRect.apply { set(offsetX, offsetY, offsetX + totalSize, offsetY + totalSize) }, piecePaint)
            return
        }

        if (pieces.isEmpty()) return

        for (piece in pieces) {
            piece?.let {
                val row = it.currentPosition / gridSize
                val col = it.currentPosition % gridSize
                val left = offsetX + col * pieceSize
                val top = offsetY + row * pieceSize
                val right = left + pieceSize
                val bottom = top + pieceSize

                if (it.isEmptySpace) {
                    canvas.drawRect(left, top, right, bottom, emptySpacePaint)
                } else if (it.bitmap != null) {
                    val inset = Constants.GRID_LINE_WIDTH / 2f
                    pieceDrawingRect.set(left + inset, top + inset, right - inset, bottom - inset)
                    canvas.drawBitmap(it.bitmap, null, pieceDrawingRect, piecePaint)
                }
            }
        }

        if (showGridLines) {
            for (i in 0..gridSize) {
                val x = offsetX + i * pieceSize
                canvas.drawLine(x, offsetY, x, offsetY + totalSize, gridPaint)
                val y = offsetY + i * pieceSize
                canvas.drawLine(offsetX, y, offsetX + totalSize, y, gridPaint)
            }
        }

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
                    handleTap(touchStartX, touchStartY)
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
        if (isVictoryState) return true
        handleTap(touchStartX, touchStartY)
        return true
    }

    private fun handleTap(x: Float, y: Float) {
        val position = getTouchedPosition(x, y)
        if (position != -1) {
            movePiece(position)
        }
    }

    private fun handleSwipe(startX: Float, startY: Float, deltaX: Float, deltaY: Float) {
        val position = getTouchedPosition(startX, startY)
        if (position == -1) return

        val absDeltaX = abs(deltaX)
        val absDeltaY = abs(deltaY)

        val board = puzzleBoard ?: return
        val emptyPos = board.getEmptyPosition()
        val row = position / gridSize
        val col = position % gridSize
        val emptyRow = emptyPos / gridSize
        val emptyCol = emptyPos % gridSize

        if (absDeltaX > absDeltaY) { // Horizontal swipe
            if (deltaX > 0 && emptyRow == row && emptyCol == col + 1) {
                // Swipe Right -> Empty space must be to the right
                movePiece(position)
            } else if (deltaX < 0 && emptyRow == row && emptyCol == col - 1) {
                // Swipe Left -> Empty space must be to the left
                movePiece(position)
            }
        } else { // Vertical swipe
            if (deltaY > 0 && emptyCol == col && emptyRow == row + 1) {
                // Swipe Down -> Empty space must be below
                movePiece(position)
            } else if (deltaY < 0 && emptyCol == col && emptyRow == row - 1) {
                // Swipe Up -> Empty space must be above
                movePiece(position)
            }
        }
    }

    private fun getTouchedPosition(x: Float, y: Float): Int {
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

    private fun movePiece(position: Int) {
        puzzleBoard?.let { board ->
            if (board.movePiece(position)) {
                updatePieces()
                onPieceMoved?.invoke()
                onPieceMovedListener?.invoke(board.isSolved())
            }
        }
    }
}
