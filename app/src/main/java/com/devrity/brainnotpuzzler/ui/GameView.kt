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
import kotlin.math.abs

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var puzzleBoard: PuzzleBoard? = null
    private var pieces: Array<PuzzlePiece?> = emptyArray()
    private var fullImage: Bitmap? = null
    private var lockIcon: Bitmap? = null
    private var isVictoryState: Boolean = false
    private var gridSize: Int = 3 // Default size, will be updated by PuzzleBoard
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
        color = 0xFF000000.toInt() // Black
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }
    private val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF000000.toInt() // Black
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }

    private val pieceDrawingRect = RectF()

    private var touchStartX: Float = 0f
    private var touchStartY: Float = 0f
    private val swipeThreshold = 50f

    var onPieceMovedListener: ((isSolved: Boolean) -> Unit)? = null
    var onPieceMoved: (() -> Unit)? = null

    fun setLockIcon(icon: Bitmap?) {
        this.lockIcon = icon
    }

    fun setPuzzleBoard(board: PuzzleBoard, image: Bitmap) {
        this.puzzleBoard = board
        this.gridSize = board.getGridSize()
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

                pieceDrawingRect.set(left, top, right, bottom)

                if (it.isEmptySpace) {
                    canvas.drawRect(pieceDrawingRect, emptySpacePaint)
                } else {
                    val bitmap = it.bitmap
                    if (bitmap != null) {
                        canvas.drawBitmap(bitmap, null, pieceDrawingRect, piecePaint)
                    }
                }

                if (it.isLocked) {
                    lockIcon?.let {
                        canvas.drawBitmap(it, null, pieceDrawingRect, piecePaint)
                    }
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

                if (abs(deltaX) < swipeThreshold && abs(deltaY) < swipeThreshold) {
                    // It's a tap
                    handleTap(touchStartX, touchStartY)
                } else {
                    // It's a swipe
                    handleSwipe(touchStartX, touchStartY, deltaX, deltaY)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleTap(x: Float, y: Float) {
        val position = getTouchedPosition(x, y)
        if (position != -1) {
            movePieceByTap(position)
        }
    }

    private fun handleSwipe(startX: Float, startY: Float, deltaX: Float, deltaY: Float) {
        val fromPosition = getTouchedPosition(startX, startY)
        if (fromPosition == -1) return

        val toPosition = when {
            abs(deltaX) > abs(deltaY) && deltaX > 0 -> fromPosition + 1 // Right
            abs(deltaX) > abs(deltaY) && deltaX < 0 -> fromPosition - 1 // Left
            abs(deltaY) > abs(deltaX) && deltaY > 0 -> fromPosition + gridSize // Down
            else -> fromPosition - gridSize // Up
        }

        movePieceBySwipe(fromPosition, toPosition)
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

    private fun movePieceByTap(position: Int) {
        puzzleBoard?.let { board ->
            if (board.movePiece(position)) {
                updatePiecesAndNotify()
            }
        }
    }

    private fun movePieceBySwipe(from: Int, to: Int) {
        puzzleBoard?.let { board ->
            if (board.movePiece(from, to)) {
                updatePiecesAndNotify()
            }
        }
    }

    private fun updatePiecesAndNotify() {
        updatePieces()
        onPieceMoved?.invoke()
        puzzleBoard?.let {
            onPieceMovedListener?.invoke(it.isSolved())
        }
    }
}
