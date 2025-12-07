package com.devrity.brainnotpuzzler.adapter

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.devrity.brainnotpuzzler.R
import com.devrity.brainnotpuzzler.manager.ImageManager
import com.devrity.brainnotpuzzler.manager.PuzzleState

class PuzzleAdapter(
    private val context: Context,
    private val puzzleStates: List<PuzzleState>,
    private val onPuzzleSelected: (String) -> Unit
) : RecyclerView.Adapter<PuzzleAdapter.PuzzleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PuzzleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_puzzle, parent, false)
        return PuzzleViewHolder(view)
    }

    override fun onBindViewHolder(holder: PuzzleViewHolder, position: Int) {
        val puzzleState = puzzleStates[position]
        holder.bind(puzzleState)
    }

    override fun getItemCount(): Int = puzzleStates.size

    inner class PuzzleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailView: ImageView = itemView.findViewById(R.id.puzzle_thumbnail)
        private val lockIcon: ImageView = itemView.findViewById(R.id.lock_icon)

        fun bind(puzzleState: PuzzleState) {
            val imagePath = "puzzles/${puzzleState.node.id}"
            val bitmap = ImageManager.getRandomImage(context) // This is a placeholder, needs to be fixed
            thumbnailView.setImageBitmap(bitmap)

            if (puzzleState.isUnlocked) {
                lockIcon.visibility = View.GONE
                thumbnailView.colorFilter = null
                itemView.setOnClickListener {
                    onPuzzleSelected(puzzleState.node.id)
                }
            } else {
                lockIcon.visibility = View.VISIBLE
                val matrix = ColorMatrix()
                matrix.setSaturation(0f) // Grayscale
                thumbnailView.colorFilter = ColorMatrixColorFilter(matrix)
                itemView.setOnClickListener(null)
            }
        }
    }
}
