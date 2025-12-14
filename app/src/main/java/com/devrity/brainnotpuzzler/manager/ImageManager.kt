package com.devrity.brainnotpuzzler.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import java.io.IOException

object ImageManager {

    // Anchor box definitions for the two pages in the win screen book
    private val WIN_SCREEN_ANCHOR_BOXES = listOf(
        // Page 1 (Left): x=346, y=522, w=140, h=140 on a 1024x1024 image
        RectF(0.338f, 0.510f, 0.338f + 0.137f, 0.510f + 0.137f),
        // Page 2 (Right): x=538, y=522, w=140, h=140 on a 1024x1024 image
        RectF(0.525f, 0.510f, 0.525f + 0.137f, 0.510f + 0.137f)
    )

    fun getImageByPath(context: Context, path: String): Bitmap? {
        return try {
            val assetManager = context.assets
            val inputStream = assetManager.open(path)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun createWinScreenBitmap(winBitmap: Bitmap, leftThumbnail: Bitmap?, rightThumbnail: Bitmap?): Bitmap {
        val resultBitmap = winBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)

        // Draw left page thumbnail if provided
        leftThumbnail?.let {
            val leftAnchor = WIN_SCREEN_ANCHOR_BOXES[0]
            val leftDestRect = RectF(
                leftAnchor.left * winBitmap.width,
                leftAnchor.top * winBitmap.height,
                leftAnchor.right * winBitmap.width,
                leftAnchor.bottom * winBitmap.height
            )
            canvas.drawBitmap(it, null, leftDestRect, null)
        }

        // Draw right page thumbnail if provided
        rightThumbnail?.let {
            val rightAnchor = WIN_SCREEN_ANCHOR_BOXES[1]
            val rightDestRect = RectF(
                rightAnchor.left * winBitmap.width,
                rightAnchor.top * winBitmap.height,
                rightAnchor.right * winBitmap.width,
                rightAnchor.bottom * winBitmap.height
            )
            canvas.drawBitmap(it, null, rightDestRect, null)
        }

        return resultBitmap
    }

    fun getDefaultImage(context: Context): Bitmap? {
        return getImageByPath(context, "puzzles/default_empty.jpg")
    }

    fun ensureSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        if (bitmap.width == bitmap.height) {
            return bitmap
        }
        val xOffset = (bitmap.width - size) / 2
        val yOffset = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, xOffset, yOffset, size, size)
    }

    fun sliceImage(bitmap: Bitmap, gridSize: Int): List<Bitmap> {
        val pieceWidth = bitmap.width / gridSize
        val pieceHeight = bitmap.height / gridSize
        val pieces = mutableListOf<Bitmap>()

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val piece = Bitmap.createBitmap(
                    bitmap,
                    col * pieceWidth,
                    row * pieceHeight,
                    pieceWidth,
                    pieceHeight
                )
                pieces.add(piece)
            }
        }
        return pieces
    }
}
