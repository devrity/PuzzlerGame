package com.devrity.brainnotpuzzler

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devrity.brainnotpuzzler.adapter.GalleryAdapter
import com.devrity.brainnotpuzzler.manager.GalleryGraphManager

class GalleryActivity : AppCompatActivity() {

    private lateinit var galleryGraphManager: GalleryGraphManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        galleryGraphManager = GalleryGraphManager(this)

        val graph = galleryGraphManager.getGalleryGraph()
        val nodes = graph?.nodes?.values?.toList() ?: emptyList()

        val recyclerView = findViewById<RecyclerView>(R.id.puzzle_grid)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = GalleryAdapter(this, nodes, galleryGraphManager) { node ->
            val resultIntent = Intent()
            resultIntent.putExtra("selected_puzzle_id", node.id)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
