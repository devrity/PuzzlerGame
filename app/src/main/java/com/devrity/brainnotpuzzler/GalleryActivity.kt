package com.devrity.brainnotpuzzler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout

class GalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val rootView = findViewById<ConstraintLayout>(R.id.gallery_root)
        rootView.setOnClickListener {
            finish()
        }
    }
}
