package com.devrity.brainnotpuzzler

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
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
    private lateinit var movesCounterText: TextView
    private lateinit var startImageView: ImageView
    private lateinit var singleWinImageView: ImageView
    private lateinit var mainLayoutGroup: Group

    private lateinit var soundManager: SoundManager
    private lateinit var hapticManager: HapticManager
    private lateinit var galleryGraphManager: GalleryGraphManager

    private var puzzleBoard: PuzzleBoard? = null
    private var currentImage: Bitmap? = null
    private var currentPuzzleId: String? = null
    private var lockIconBitmap: Bitmap? = null
    private var isSolved: Boolean = false
    private var isWinScreenShowing: Boolean = false
    private var puzzlesSolvedThisSession = 0

    private val handler = Handler(Looper.getMainLooper())

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            it.data?.getStringExtra("selected_puzzle_id")?.let {
                startNewGame(it, null)
            }
        }
    }

    companion object {
        private const val KEY_CURRENT_PUZZLE_ID = "KEY_CURRENT_PUZZLE_ID"
        private const val KEY_BOARD_STATE = "KEY_BOARD_STATE"
        private const val KEY_IS_SOLVED = "KEY_IS_SOLVED"
        private const val KEY_MOVE_COUNT = "KEY_MOVE_COUNT"
        private const val KEY_IS_WIN_SCREEN_SHOWING = "KEY_IS_WIN_SCREEN_SHOWING"
        private const val KEY_PUZZLES_SOLVED_THIS_SESSION = "KEY_PUZZLES_SOLVED_THIS_SESSION"
    private const val KEY_MAIN_LAYOUT_VISIBILITY = "main_layout_visibility"
    private const val KEY_START_IMAGE_VISIBILITY = "start_image_visibility"
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

        lockIconBitmap = ImageManager.getImageByPath(this, "puzzles/lock.png")
        gameView.setLockIcon(lockIconBitmap)

        if (savedInstanceState != null) {
            puzzlesSolvedThisSession = savedInstanceState.getInt(KEY_PUZZLES_SOLVED_THIS_SESSION, 0)
            isSolved = savedInstanceState.getBoolean(KEY_IS_SOLVED, false)
            isWinScreenShowing = savedInstanceState.getBoolean(KEY_IS_WIN_SCREEN_SHOWING, false)
            val savedPuzzleId = savedInstanceState.getString(KEY_CURRENT_PUZZLE_ID)
            val savedBoardState = savedInstanceState.getStringArrayList(KEY_BOARD_STATE)

            // Restore view visibility state
            mainLayoutGroup.visibility = savedInstanceState.getInt(
                KEY_MAIN_LAYOUT_VISIBILITY, View.VISIBLE
            )
            startImageView.visibility = savedInstanceState.getInt(
                KEY_START_IMAGE_VISIBILITY, View.GONE
            )
            startNewGame(savedPuzzleId, savedBoardState)

            if (isWinScreenShowing) {
                showWinScreen()
            } else if (isSolved) {
                gameView.displayFullImage()
                showVictoryConfetti()
                val winScreenRunnable = Runnable { showWinScreen() }
                handler.postDelayed(winScreenRunnable, 1000) 
            }

        } else {
            showStartScreen()
        }
    }

    private fun showStartScreen() {
        // Only set initial visibility on first launch, not on rotation
        if (savedInstanceState == null) {
            mainLayoutGroup.visibility = View.INVISIBLE
            startImageView.visibility = View.VISIBLE
        }
        startImageView.setImageBitmap(ImageManager.getImageByPath(this, "start.jpg"))
        startImageView.setOnClickListener {
            startImageView.visibility = View.GONE
            mainLayoutGroup.visibility = View.VISIBLE
            galleryGraphManager.getStartGallery()?.let { startNewGame(it.id, null) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CURRENT_PUZZLE_ID, currentPuzzleId)
        outState.putBoolean(KEY_IS_SOLVED, isSolved)
        outState.putBoolean(KEY_IS_WIN_SCREEN_SHOWING, isWinScreenShowing)
        outState.putInt(KEY_PUZZLES_SOLVED_THIS_SESSION, puzzlesSolvedThisSession)
        puzzleBoard?.let {
            outState.putStringArrayList(KEY_BOARD_STATE, it.getCurrentStateForSave())
            outState.putInt(KEY_MOVE_COUNT, it.moveCount)
        }
        // Save view visibility state
        outState.putInt(KEY_MAIN_LAYOUT_VISIBILITY, mainLayoutGroup.visibility)
        outState.putInt(KEY_START_IMAGE_VISIBILITY, startImageView.visibility)
    }

    private fun updateActionBarVisibility() {
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> supportActionBar?.hide()
            Configuration.ORIENTATION_PORTRAIT -> supportActionBar?.show()
            else -> {}
        }
    }

    private fun initViews() {
        mainLayoutGroup = findViewById(R.id.main_layout_group)
        startImageView = findViewById(R.id.start_image_view)
        singleWinImageView = findViewById(R.id.single_win_image_view)
        gameView = findViewById(R.id.game_view)
        thumbnailPreview = findViewById(R.id.thumbnail_preview)
        replayButton = findViewById(R.id.replay_button)
        settingsButton = findViewById(R.id.settings_button)
        galleryButton = findViewById(R.id.gallery_button)
        premiumButton = findViewById(R.id.premium_button)
        konfettiView = findViewById(R.id.konfetti_view)
        movesCounterText = findViewById(R.id.moves_counter_text)
    }

    private fun setupListeners() {
        gameView.onPieceMoved = {
            soundManager.playPieceMoveSound()
            updateMovesCounter()
        }

        gameView.onPieceMovedListener = { isSolved ->
            if (isSolved) {
                onPuzzleSolved()
            }
        }

        replayButton.setOnClickListener {
            startNewGame(currentPuzzleId, null)
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

    private fun startNewGame(puzzleId: String?, savedBoardState: ArrayList<String>?) {
        if (savedBoardState == null) {
            isSolved = false
            isWinScreenShowing = false
        }
        konfettiView.visibility = View.GONE
        singleWinImageView.visibility = View.GONE
        handler.removeCallbacksAndMessages(null)

        currentPuzzleId = puzzleId

        val puzzleNode = galleryGraphManager.getGalleryNode(puzzleId ?: "")
        val imagePath = puzzleNode?.puzzleFolder
        val puzzleSize = puzzleNode?.puzzleSize ?: 3

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

        puzzleBoard = PuzzleBoard(puzzleSize)
        val pieceBitmaps = ImageManager.sliceImage(currentImage!!, puzzleSize)
        puzzleBoard?.setupBoard(pieceBitmaps, puzzleNode?.initState, savedBoardState)
        
        updateMovesCounter()

        if (puzzleBoard != null) {
            gameView.setPuzzleBoard(puzzleBoard!!, currentImage!!)
            gameView.setShowGridLines(true)
            gameView.setInteractionEnabled(true)
        }
    }

    private fun updateMovesCounter() {
        movesCounterText.text = puzzleBoard?.moveCount.toString()
    }

    private fun onPuzzleSolved() {
        if (isSolved) return 
        isSolved = true
        puzzlesSolvedThisSession++

        soundManager.playVictorySound()
        hapticManager.playVictoryHaptic()

        currentPuzzleId?.let {
            galleryGraphManager.setNodeStatus(it, NodeStatus.COMPLETED)
            galleryGraphManager.unlockOutgoingNodesFor(it)
        }

        gameView.displayFullImage()
        showVictoryConfetti()

        val winScreenRunnable = Runnable { showWinScreen() }
        handler.postDelayed(winScreenRunnable, 3000)
    }
    
    private fun showWinScreen() {
        isWinScreenShowing = true
        konfettiView.visibility = View.GONE
        
        val winBitmap = ImageManager.getImageByPath(this, "single_win.png")
        var leftThumbnail: Bitmap? = null
        var rightThumbnail: Bitmap? = null

        if (puzzlesSolvedThisSession <= 1) {
            // First puzzle solved, place on the left page
            leftThumbnail = currentImage
        } else {
            // Second or subsequent puzzle, place on the right page
            rightThumbnail = currentImage
            val puzzleNode = galleryGraphManager.getGalleryNode(currentPuzzleId ?: "")
            val previousPuzzleId = puzzleNode?.incomingEdges?.firstOrNull()
            if (previousPuzzleId != null) {
                val previousPuzzleNode = galleryGraphManager.getGalleryNode(previousPuzzleId)
                if (previousPuzzleNode != null) {
                    leftThumbnail = ImageManager.getImageByPath(this, previousPuzzleNode.puzzleFolder)
                }
            }
        }

        if (winBitmap != null) {
            val finalBitmap = ImageManager.createWinScreenBitmap(winBitmap, leftThumbnail, rightThumbnail)
            singleWinImageView.setImageBitmap(finalBitmap)
            singleWinImageView.visibility = View.VISIBLE
            singleWinImageView.setOnClickListener {
                launchGallery()
            }
        } else {
            // Fallback if images can't be loaded
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
