package com.devrity.brainnotpuzzler

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.createBitmap
import com.devrity.brainnotpuzzler.manager.HapticManager
import com.devrity.brainnotpuzzler.manager.ImageManager
import com.devrity.brainnotpuzzler.manager.ProgressionManager
import com.devrity.brainnotpuzzler.manager.SoundManager
import com.devrity.brainnotpuzzler.model.PuzzleBoard
import com.devrity.brainnotpuzzler.ui.GameView
import com.devrity.brainnotpuzzler.util.Constants

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var thumbnailPreview: ImageView
    private lateinit var replayButton: ImageButton
    private lateinit var settingsButton: ImageButton
    private lateinit var galleryButton: ImageButton
    private lateinit var premiumButton: ImageButton

    private lateinit var soundManager: SoundManager
    private lateinit var hapticManager: HapticManager
    private lateinit var progressionManager: ProgressionManager

    private var puzzleBoard: PuzzleBoard? = null
    private var currentImage: Bitmap? = null
    private var currentPuzzleId: String? = null
    private val gridSize = Constants.DEFAULT_GRID_SIZE
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val GALLERY_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Hide ActionBar in landscape orientation
        updateActionBarVisibility()

        // Initialize managers
        soundManager = SoundManager(this)
        hapticManager = HapticManager(this)
        progressionManager = ProgressionManager(this)

        initViews()
        setupListeners()

        // Start a new game with the first puzzle
        startNewGame(progressionManager.getPuzzleStates().first().node.id)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.getStringExtra("selected_puzzle")?.let {
                startNewGame(it)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        
        // Reload the layout and re-initialize views while preserving state
        setContentView(R.layout.activity_main)
        updateActionBarVisibility()
        initViews()
        setupListeners()

        // Restore the state of the puzzle
        puzzleBoard?.let {
            gameView.setPuzzleBoard(it)
        }
        currentImage?.let {
            thumbnailPreview.setImageBitmap(it)
        }
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
        galleryButton = findViewById(R.id.gallery_button)
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
            currentPuzzleId?.let { replayGame(it) }
        }

        // Settings button - placeholder
        settingsButton.setOnClickListener {
            Toast.makeText(this, "Settings - Coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Gallery button - launch gallery activity
        galleryButton.setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
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
     * Start a new game with a specific puzzle image.
     */
    private fun startNewGame(puzzleId: String) {
        currentPuzzleId = puzzleId
        // Load image from assets or use test pattern
        currentImage = ImageManager.getRandomImage(this) ?: createTestImage() // This is a placeholder, needs to be fixed

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
                        gameView.setShowGridLines(true)
                                    gameView.setInteractionEnabled(true)
        }
    }

    /**
     * Replay current puzzle - restart with same image but reshuffled
     */
    private fun replayGame(puzzleId: String) {
        startNewGame(puzzleId)
    }

    /**
     * Called when puzzle is solved
     */
    private fun onPuzzleSolved() {
        // Play victory sound and haptic
        soundManager.playVictorySound()
        hapticManager.playVictoryHaptic()

        currentPuzzleId?.let {
            progressionManager.unlockNextPuzzle(it)
        }

        // Wait 1 second then launch gallery
        handler.postDelayed({
            val intent = Intent(this, GalleryActivity::class.java)
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }, 1000)
    }

    /**
     * Create a test image (temporary fallback)
     */
    private fun createTestImage(): Bitmap {
        // Placeholder - will be replaced with actual image loading
        return createBitmap(300, 300, Bitmap.Config.ARGB_8888)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
        handler.removeCallbacksAndMessages(null)
    }
}
