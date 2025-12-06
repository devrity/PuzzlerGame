package com.devrity.puzzler.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.devrity.puzzler.util.Constants
import java.io.IOException

/**
 * Manages loading and processing of puzzle images.
 */
object ImageManager {
    
    /**
     * Get a random puzzle image from the assets folder.
     * @return Bitmap of the selected image, or null if error occurs
     */
    fun getRandomImage(context: Context): Bitmap? {
        return try {
            val assetManager = context.assets
            val imageFiles = assetManager.list(Constants.PUZZLE_IMAGES_FOLDER) ?: emptyArray()
            
            if (imageFiles.isEmpty()) {
                // Fallback: use a drawable resource if no assets available
                // For now, return null - we'll add sample images later
                null
            } else {
                val randomFile = imageFiles.random()
                val inputStream = assetManager.open("${Constants.PUZZLE_IMAGES_FOLDER}/$randomFile")
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                
                // Ensure the image is square
                ensureSquare(bitmap)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Ensure the bitmap is square by cropping to the smaller dimension.
     * @param bitmap The input bitmap
     * @return A square bitmap
     */
    fun ensureSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        
        // If already square, return as is
        if (bitmap.width == bitmap.height) {
            return bitmap
        }
        
        // Calculate offsets to center the crop
        val xOffset = (bitmap.width - size) / 2
        val yOffset = (bitmap.height - size) / 2
        
        return Bitmap.createBitmap(bitmap, xOffset, yOffset, size, size)
    }
    
    /**
     * Slice an image into a grid of pieces.
     * @param bitmap The source image (should be square)
     * @param gridSize The grid dimension (e.g., 3 for 3x3 = 9 pieces)
     * @return List of bitmap pieces (excludes bottom-right piece)
     */
    fun sliceImage(bitmap: Bitmap, gridSize: Int): List<Bitmap> {
        val pieceWidth = bitmap.width / gridSize
        val pieceHeight = bitmap.height / gridSize
        val pieces = mutableListOf<Bitmap>()
        val totalPieces = gridSize * gridSize
        
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val position = row * gridSize + col
                
                // Skip the bottom-right piece (last piece)
                if (position == totalPieces - 1) {
                    continue
                }
                
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
    
    /**
     * Scale a bitmap to a specific size while maintaining aspect ratio.
     * @param bitmap The source bitmap
     * @param maxSize Maximum width/height
     * @return Scaled bitmap
     */
    fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        if (bitmap.width <= maxSize && bitmap.height <= maxSize) {
            return bitmap
        }
        
        val ratio = minOf(
            maxSize.toFloat() / bitmap.width,
            maxSize.toFloat() / bitmap.height
        )
        
        val width = (bitmap.width * ratio).toInt()
        val height = (bitmap.height * ratio).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}
