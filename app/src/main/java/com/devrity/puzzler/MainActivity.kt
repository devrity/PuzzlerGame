package com.devrity.puzzler

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.devrity.puzzler.manager.HapticManager
import com.devrity.puzzler.manager.ImageManager
import com.devrity.puzzler.manager.SoundManager
import com.devrity.puzzler.model.PuzzleBoard
import com.devrity.puzzler.ui.GameView
import com.devrity.puzzler.util.Constants
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    
    private lateinit var gameView: GameView
    private lateinit var thumbnailPreview: ImageView
    private lateinit var homeButton: ImageButton
    private lateinit var restartButton: ImageButton
    private lateinit var hintButton: ImageButton
    private lateinit var settingsButton: ImageButton
    
    private lateinit var soundManager: SoundManager
    private lateinit var hapticManager: HapticManager
    
    private var puzzleBoard: PuzzleBoard? = null
    private var currentImage: Bitmap? = null
    private val gridSize = Constants.DEFAULT_GRID_SIZE
    private val handler = Handler(Looper.getMainLooper())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Hide ActionBar in landscape orientation
        updateActionBarVisibility()
        
        // Initialize managers
        soundManager = SoundManager(this)
        hapticManager = HapticManager(this)
        
        initViews()
        setupListeners()
        
        // Start a new game
        startNewGame()
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Update ActionBar visibility when orientation changes
        updateActionBarVisibility()
    }
    
    /**
     * Show ActionBar in portrait, hide in landscape.
     */
    private fun updateActionBarVisibility() {
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                supportActionBar?.hide()
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                supportActionBar?.show()
            }
        }
    }
    
    private fun initViews() {
        gameView = findViewById(R.id.game_view)
        thumbnailPreview = findViewById(R.id.thumbnail_preview)
        homeButton = findViewById(R.id.home_button)
        restartButton = findViewById(R.id.restart_button)
        hintButton = findViewById(R.id.hint_button)
        settingsButton = findViewById(R.id.settings_button)
    }
    
    private fun setupListeners() {
        // Game view listener for piece moves (with sound/haptic)
        gameView.onPieceMoved = {
            // Play sound and haptic on successful move
            soundManager.playPieceMoveSound()
            hapticManager.playPieceMoveHaptic()
        }
        
        // Game view listener for win condition
        gameView.onPieceMovedListener = { isSolved ->
            if (isSolved) {
                onPuzzleSolved()
            }
        }
        
        // Restart button
        restartButton.setOnClickListener {
            restartGame()
        }
        
        // Hint button - show full image (placeholder)
        hintButton.setOnClickListener {
            showHint()
        }
        
        // Home button - placeholder
        homeButton.setOnClickListener {
            Toast.makeText(this, "Home - Coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        // Settings button - placeholder
        settingsButton.setOnClickListener {
            Toast.makeText(this, "Settings - Coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        // Thumbnail click - placeholder for fullscreen view
        thumbnailPreview.setOnClickListener {
            Toast.makeText(this, "Fullscreen preview - Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Start a new game with a random image.
     */
    private fun startNewGame() {
        // Load image from assets or use test pattern
        currentImage = ImageManager.getRandomImage(this) ?: createTestImage()
        
        if (currentImage == null) {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Ensure image is square
        currentImage = ImageManager.ensureSquare(currentImage!!)
        
        // Set thumbnail
        thumbnailPreview.setImageBitmap(currentImage)
        
        // Slice the image
        val pieces = ImageManager.sliceImage(currentImage!!, gridSize)
        
        // Initialize puzzle board
        puzzleBoard = PuzzleBoard(gridSize)
        puzzleBoard?.initBoard(pieces)
        
        // Shuffle the board
        puzzleBoard?.shuffle()
        
        // Set up the game view
        gameView.setPuzzleBoard(puzzleBoard!!)
        gameView.setShowGridLines(true)
        gameView.setInteractionEnabled(true)
        
        Toast.makeText(this, "Tap or swipe pieces adjacent to the empty space!", Toast.LENGTH_LONG).show()
    }
    
    /**
     * Restart the current game.
     */
    private fun restartGame() {
        if (currentImage != null) {
            // Slice the image again
            val pieces = ImageManager.sliceImage(currentImage!!, gridSize)
            
            // Reinitialize puzzle board
            puzzleBoard = PuzzleBoard(gridSize)
            puzzleBoard?.initBoard(pieces)
            
            // Shuffle the board
            puzzleBoard?.shuffle()
            
            // Update the game view
            gameView.setPuzzleBoard(puzzleBoard!!)
            gameView.setShowGridLines(true)
            gameView.setInteractionEnabled(true)
            
            Toast.makeText(this, "Game restarted!", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Show hint (full original image).
     */
    private fun showHint() {
        Toast.makeText(this, "Hint: Check the thumbnail!", Toast.LENGTH_SHORT).show()
        // TODO: Implement fullscreen image view in Phase 3
    }
    
    /**
     * Handle puzzle solved event with victory sequence.
     */
    private fun onPuzzleSolved() {
        // Disable interaction
        gameView.setInteractionEnabled(false)
        
        // Play victory sound and haptic
        soundManager.playVictorySound()
        hapticManager.playVictoryHaptic()
        
        // Wait 1 second, then hide grid lines
        handler.postDelayed({
            gameView.setShowGridLines(false)
            
            // Show victory dialog after brief delay
            handler.postDelayed({
                showVictoryDialog()
            }, 500)
        }, Constants.VICTORY_ANIMATION_DELAY)
    }
    
    /**
     * Show victory dialog with confetti and options.
     */
    private fun showVictoryDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_victory, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        // Set up buttons
        val playAgainButton = dialogView.findViewById<MaterialButton>(R.id.play_again_button)
        val newGameButton = dialogView.findViewById<MaterialButton>(R.id.new_game_button)
        
        playAgainButton.setOnClickListener {
            dialog.dismiss()
            restartGame()
        }
        
        newGameButton.setOnClickListener {
            dialog.dismiss()
            startNewGame()
        }
        
        dialog.show()
    }
    
    /**
     * Create a test image with colored squares for development.
     */
    private fun createTestImage(): Bitmap {
        val size = 900 // 9x9 grid works well
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // Draw a gradient pattern
        val paint = android.graphics.Paint()
        val colors = intArrayOf(
            0xFFE57373.toInt(), 0xFFF06292.toInt(), 0xFFBA68C8.toInt(),
            0xFF9575CD.toInt(), 0xFF7986CB.toInt(), 0xFF64B5F6.toInt(),
            0xFF4FC3F7.toInt(), 0xFF4DD0E1.toInt(), 0xFF4DB6AC.toInt()
        )
        
        val pieceSize = size / 3
        var colorIndex = 0
        for (row in 0 until 3) {
            for (col in 0 until 3) {
                paint.color = colors[colorIndex % colors.size]
                canvas.drawRect(
                    (col * pieceSize).toFloat(),
                    (row * pieceSize).toFloat(),
                    ((col + 1) * pieceSize).toFloat(),
                    ((row + 1) * pieceSize).toFloat(),
                    paint
                )
                colorIndex++
            }
        }
        
        return bitmap
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources
        soundManager.release()
        currentImage?.recycle()
        currentImage = null
    }
}
