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

            if (savedPuzzleId != null) {
                // Restore game state
                startNewGame(savedPuzzleId, savedBoardState)

                // Ensure main layout is visible after game restoration
                mainLayoutGroup.visibility = View.VISIBLE
                gameView.visibility = View.VISIBLE
                startImageView.visibility = View.GONE

                if (isWinScreenShowing) {
                    showWinScreen()
                } else if (isSolved) {
                    gameView.displayFullImage()
                    showVictoryConfetti()
                    val winScreenRunnable = Runnable { showWinScreen() }
                    handler.postDelayed(winScreenRunnable, 1000)
                }
            } else {
                // Still on start screen, restore start screen state
                showStartScreen(savedInstanceState)
            }