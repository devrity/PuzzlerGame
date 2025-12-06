package com.devrity.puzzler

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var thumbnailPreview: ImageView
    private lateinit var replayButton: ImageButton
    private lateinit var settingsButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var premiumButton: ImageButton

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
        replayButton = findViewById(R.id.replay_button)
        settingsButton = findViewById(R.id.settings_button)
        nextButton = findViewById(R.id.next_button)
        premiumButton = findViewById(R.id.premium_button)
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

        // Replay button - restart current puzzle
        replayButton.setOnClickListener {
            replayGame()
        }

        // Settings button - placeholder
        settingsButton.setOnClickListener {
            Toast.makeText(this, "Settings - Coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Next button - load random new puzzle
        nextButton.setOnClickListener {
            startNewGame()
        }

        // Premium button - placeholder (no backend yet)
        premiumButton.setOnClickListener {
            Toast.makeText(this, "Premium coming in Phase 4!", Toast.LENGTH_SHORT).show()
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

        // Create puzzle board
        puzzleBoard = PuzzleBoard(gridSize)
        
        // Slice image into pieces and initialize board
        val pieceBitmaps = ImageManager.sliceImage(currentImage!!, gridSize)
        puzzleBoard?.initBoard(pieceBitmaps)
        
        // Shuffle the board
        puzzleBoard?.shuffle()

        // Set puzzle board to game view
        if (puzzleBoard != null) {
            gameView.setPuzzleBoard(puzzleBoard!!)
        }
    }

    /**
     * Replay current puzzle - restart with same image but reshuffled
     */
    private fun replayGame() {
        if (currentImage == null) {
            startNewGame()
            return
        }

        // Create new puzzle board with same image
        puzzleBoard = PuzzleBoard(gridSize)
        
        // Slice image into pieces and initialize board
        val pieceBitmaps = ImageManager.sliceImage(currentImage!!, gridSize)
        puzzleBoard?.initBoard(pieceBitmaps)
        
        // Shuffle the board
        puzzleBoard?.shuffle()

        // Set puzzle board to game view
        if (puzzleBoard != null) {
            gameView.setPuzzleBoard(puzzleBoard!!)
        }
    }

    /**
     * Called when puzzle is solved
     */
    private fun onPuzzleSolved() {
        // Play victory sound and haptic
        soundManager.playVictorySound()
        hapticManager.playVictoryHaptic()

        // Wait 1 second then hide grid
        handler.postDelayed({
            gameView.
        }, 1000)

        // Wait 1.5 seconds then show victory dialog
        handler.postDelayed({
            showVictoryDialog()
        }, 1500)
    }

    /**
     * Show victory dialog with confetti effect
     */
    private fun showVictoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("🎉 Congratulations! 🎉")
        builder.setMessage("You solved the puzzle!")
        builder.setPositiveButton("Play Again") { _, _ ->
            replayGame()
        }
        builder.setNegativeButton("New Game") { _, _ ->
            startNewGame()
        }
        builder.setCancelable(false)
        builder.show()
    }

    /**
     * Create a test image (temporary fallback)
     */
    private fun createTestImage(): Bitmap? {
        // Placeholder - will be replaced with actual image loading
        return Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
        handler.removeCallbacksAndMessages(null)
    }
}
