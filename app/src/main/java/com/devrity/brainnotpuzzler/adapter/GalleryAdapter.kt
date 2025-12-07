package com.devrity.brainnotpuzzler.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.devrity.brainnotpuzzler.R
import com.devrity.brainnotpuzzler.manager.GalleryGraphManager
import com.devrity.brainnotpuzzler.manager.GalleryNode
import com.devrity.brainnotpuzzler.manager.ImageManager
import com.devrity.brainnotpuzzler.manager.NodeStatus

class GalleryAdapter(
    private val context: Context,
    private val nodes: List<GalleryNode>,
    private val galleryGraphManager: GalleryGraphManager,
    private val onNodeSelected: (GalleryNode) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.NodeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_gallery_node, parent, false)
        return NodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
        val node = nodes[position]
        holder.bind(node)
    }

    override fun getItemCount(): Int = nodes.size

    inner class NodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.node_icon)
        private val lockIcon: ImageView = itemView.findViewById(R.id.lock_icon)

        fun bind(node: GalleryNode) {
            val bitmap = ImageManager.getImageByPath(context, node.icon)
            iconView.setImageBitmap(bitmap)

            when (galleryGraphManager.getNodeStatus(node.id)) {
                NodeStatus.LOCKED -> {
                    lockIcon.visibility = View.VISIBLE
                    val matrix = ColorMatrix()
                    matrix.setSaturation(0f) // Grayscale
                    iconView.colorFilter = ColorMatrixColorFilter(matrix)
                    itemView.setOnClickListener(null)
                    itemView.setBackgroundColor(Color.TRANSPARENT)
                }
                NodeStatus.UNLOCKED, NodeStatus.IN_PROGRESS -> {
                    lockIcon.visibility = View.GONE
                    iconView.colorFilter = null
                    itemView.setOnClickListener { onNodeSelected(node) }
                    itemView.setBackgroundColor(Color.TRANSPARENT)
                }
                NodeStatus.COMPLETED -> {
                    lockIcon.visibility = View.GONE
                    iconView.colorFilter = null
                    itemView.setOnClickListener { onNodeSelected(node) }
                    itemView.setBackgroundColor(Color.parseColor("#FFD700")) // Gold
                }
            }
        }
    }
}
