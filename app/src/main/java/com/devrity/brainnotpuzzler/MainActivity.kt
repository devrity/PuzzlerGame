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
import com.devrity.brainnotpuzzler.manager.GalleryGraphManager
import com.devrity.brainnotpuzzler.manager.HapticManager
import com.devrity.brainnotpuzzler.manager.ImageManager
import com.devrity.brainnotpuzzler.manager.NodeStatus
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
    private lateinit var galleryGraphManager: GalleryGraphManager

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
        galleryGraphManager = GalleryGraphManager(this)

        initViews()
        setupListeners()

        // Start a new game with the first puzzle in the progression
        galleryGraphManager.getStartGallery()?.let { startNewGame(it.id) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.getStringExtra("selected_puzzle_id")?.let {
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
        gameView.onPieceMoved = {
            soundManager.playPieceMoveSound()
            hapticManager.playPieceMoveHaptic()
        }

        gameView.onPieceMovedListener = { isSolved ->
            if (isSolved) {
                onPuzzleSolved()
            }
        }

        replayButton.setOnClickListener {
            currentPuzzleId?.let { replayGame(it) }
        }

        settingsButton.setOnClickListener {
            Toast.makeText(this, "Settings - Coming soon!", Toast.LENGTH_SHORT).show()
        }

        galleryButton.setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        premiumButton.setOnClickListener {
            Toast.makeText(this, "Premium coming in Phase 4!", Toast.LENGTH_SHORT).show()
        }

        thumbnailPreview.setOnClickListener {
            Toast.makeText(this, "Fullscreen preview - Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startNewGame(puzzleId: String?) {
        currentPuzzleId = puzzleId

        val puzzleNode = galleryGraphManager.getGalleryNode(puzzleId ?: "")
        val imagePath = puzzleNode?.puzzleFolder

        if (imagePath == null) {
            currentImage = ImageManager.getRandomImage(this) ?: createTestImage()
        } else {
            currentImage = ImageManager.getImageByPath(this, imagePath) ?: createTestImage()
        }

        currentImage = ImageManager.ensureSquare(currentImage!!)
        thumbnailPreview.setImageBitmap(currentImage)

        puzzleBoard = PuzzleBoard(gridSize)
        val pieceBitmaps = ImageManager.sliceImage(currentImage!!, gridSize)
        puzzleBoard?.initBoard(pieceBitmaps)
        puzzleBoard?.shuffle()

        if (puzzleBoard != null) {
            gameView.setPuzzleBoard(puzzleBoard!!)
            gameView.setShowGridLines(true)
            gameView.setInteractionEnabled(true)
        }
    }

    private fun replayGame(puzzleId: String) {
        startNewGame(puzzleId)
    }

    private fun onPuzzleSolved() {
        soundManager.playVictorySound()
        hapticManager.playVictoryHaptic()

        currentPuzzleId?.let {
            galleryGraphManager.setNodeStatus(it, NodeStatus.COMPLETED)
            galleryGraphManager.unlockOutgoingNodesFor(it)
        }

        handler.postDelayed({
            val intent = Intent(this, GalleryActivity::class.java)
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }, 1000)
    }

    private fun createTestImage(): Bitmap {
        return createBitmap(300, 300, Bitmap.Config.ARGB_8888)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
        handler.removeCallbacksAndMessages(null)
    }
}
