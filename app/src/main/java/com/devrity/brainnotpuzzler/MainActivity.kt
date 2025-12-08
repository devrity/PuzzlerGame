package com.devrity.brainnotpuzzler

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.devrity.brainnotpuzzler.manager.GalleryGraphManager
import com.devrity.brainnotpuzzler.manager.HapticManager
import com.devrity.brainnotpuzzler.manager.ImageManager
import com.devrity.brainnotpuzzler.model.NodeStatus
import com.devrity.brainnotpuzzler.manager.SoundManager
import com.devrity.brainnotpuzzler.model.PuzzleBoard
import com.devrity.brainnotpuzzler.ui.GameView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var thumbnailPreview: ImageView
    private lateinit var replayButton: ImageButton
    private lateinit var settingsButton: ImageButton
    private lateinit var galleryButton: ImageButton
    private lateinit var premiumButton: ImageButton
    private lateinit var konfettiView: KonfettiView

    private lateinit var soundManager: SoundManager
    private lateinit var hapticManager: HapticManager
    private lateinit var galleryGraphManager: GalleryGraphManager

    private var puzzleBoard: PuzzleBoard? = null
    private var currentImage: Bitmap? = null
    private var currentPuzzleId: String? = null

    private val handler = Handler(Looper.getMainLooper())

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.getStringExtra("selected_puzzle_id")?.let {
                startNewGame(it)
            }
        }
    }

    companion object {
        private const val KEY_CURRENT_PUZZLE_ID = "KEY_CURRENT_PUZZLE_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateActionBarVisibility()

        soundManager = SoundManager(this)
        hapticManager = HapticManager(this)
        galleryGraphManager = GalleryGraphManager(this)

        initViews()
        setupListeners()

        if (savedInstanceState != null) {
            val savedPuzzleId = savedInstanceState.getString(KEY_CURRENT_PUZZLE_ID)
            startNewGame(savedPuzzleId)
        } else {
            galleryGraphManager.getStartGallery()?.let { startNewGame(it.id) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CURRENT_PUZZLE_ID, currentPuzzleId)
    }

    private fun updateActionBarVisibility() {
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> supportActionBar?.hide()
            Configuration.ORIENTATION_PORTRAIT -> supportActionBar?.show()
            else -> {}
        }
    }

    private fun initViews() {
        gameView = findViewById(R.id.game_view)
        thumbnailPreview = findViewById(R.id.thumbnail_preview)
        replayButton = findViewById(R.id.replay_button)
        settingsButton = findViewById(R.id.settings_button)
        galleryButton = findViewById(R.id.gallery_button)
        premiumButton = findViewById(R.id.premium_button)
        konfettiView = findViewById(R.id.konfetti_view)
    }

    private fun setupListeners() {
        gameView.onPieceMoved = {
            soundManager.playPieceMoveSound()
        }

        gameView.onPieceMovedListener = { isSolved ->
            if (isSolved) {
                onPuzzleSolved()
            }
        }

        replayButton.setOnClickListener {
            currentPuzzleId?.let { startNewGame(it) }
        }

        settingsButton.setOnClickListener {
            Toast.makeText(this, "Settings - Coming soon!", Toast.LENGTH_SHORT).show()
        }

        galleryButton.setOnClickListener {
            launchGallery()
        }

        premiumButton.setOnClickListener {
            Toast.makeText(this, "Premium coming in Phase 4!", Toast.LENGTH_SHORT).show()
        }

        thumbnailPreview.setOnClickListener {
            Toast.makeText(this, "Fullscreen preview - Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startNewGame(puzzleId: String?) {
        // This is the crucial fix: Always clean up the victory state before starting a new game.
        konfettiView.visibility = View.GONE
        konfettiView.setOnClickListener(null)
        handler.removeCallbacksAndMessages(null)

        currentPuzzleId = puzzleId

        val puzzleNode = galleryGraphManager.getGalleryNode(puzzleId ?: "")
        val imagePath = puzzleNode?.puzzleFolder

        currentImage = if (imagePath == null) {
            ImageManager.getDefaultImage(this)
        } else {
            ImageManager.getImageByPath(this, imagePath)
        }
        
        if (currentImage == null) {
            Toast.makeText(this, "Error: Could not load any image.", Toast.LENGTH_LONG).show()
            return
        }

        currentImage = ImageManager.ensureSquare(currentImage!!)
        thumbnailPreview.setImageBitmap(currentImage)

        puzzleBoard = PuzzleBoard()
        val pieceBitmaps = ImageManager.sliceImage(currentImage!!)
        puzzleBoard?.initBoard(pieceBitmaps)
        puzzleBoard?.shuffle()

        if (puzzleBoard != null) {
            gameView.setPuzzleBoard(puzzleBoard!!, currentImage!!)
            gameView.setShowGridLines(true)
            gameView.setInteractionEnabled(true)
        }
    }

    private fun onPuzzleSolved() {
        soundManager.playVictorySound()
        hapticManager.playVictoryHaptic()

        currentPuzzleId?.let {
            galleryGraphManager.setNodeStatus(it, NodeStatus.COMPLETED)
            galleryGraphManager.unlockOutgoingNodesFor(it)
        }

        gameView.displayFullImage()
        showVictoryConfetti()

        val galleryRunnable = Runnable { launchGallery() }
        handler.postDelayed(galleryRunnable, 3000)

        konfettiView.setOnClickListener {
            handler.removeCallbacks(galleryRunnable)
            launchGallery()
        }
    }

    private fun showVictoryConfetti() {
        konfettiView.visibility = View.VISIBLE
        konfettiView.start(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(200)
            )
        )
    }

    private fun launchGallery() {
        val intent = Intent(this, GalleryActivity::class.java)
        galleryLauncher.launch(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
        handler.removeCallbacksAndMessages(null)
    }
}
