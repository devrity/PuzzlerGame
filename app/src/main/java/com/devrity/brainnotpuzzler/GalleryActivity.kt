package com.devrity.brainnotpuzzler

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devrity.brainnotpuzzler.adapter.PuzzleAdapter
import com.devrity.brainnotpuzzler.manager.ProgressionManager

class GalleryActivity : AppCompatActivity() {

    private lateinit var progressionManager: ProgressionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        progressionManager = ProgressionManager(this)

        val puzzleStates = progressionManager.getPuzzleStates()

        val recyclerView = findViewById<RecyclerView>(R.id.puzzle_grid)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = PuzzleAdapter(this, puzzleStates) { puzzleId ->
            val resultIntent = Intent()
            resultIntent.putExtra("selected_puzzle", puzzleId)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
