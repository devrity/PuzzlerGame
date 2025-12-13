package com.devrity.brainnotpuzzler.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.devrity.brainnotpuzzler.util.Constants
import java.io.IOException

object ImageManager {

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

    fun sliceImage(bitmap: Bitmap): List<Bitmap> {
        val gridSize = Constants.GRID_SIZE
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
