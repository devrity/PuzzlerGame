package com.devrity.brainnotpuzzler.manager

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.devrity.brainnotpuzzler.R
import com.devrity.brainnotpuzzler.model.GalleryGraph
import com.devrity.brainnotpuzzler.model.GalleryNode
import com.devrity.brainnotpuzzler.model.NodeStatus
import com.devrity.brainnotpuzzler.util.Constants
import com.google.gson.Gson
import java.io.InputStreamReader

class GalleryGraphManager(private val context: Context) {
    private var galleryGraph: GalleryGraph? = null
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME, 
        Context.MODE_PRIVATE
    )

    init {
        loadGalleryGraph()
    }

    private fun loadGalleryGraph() {
        try {
            val jsonFileName = context.getString(R.string.gallery_graph_json)
            val inputStream = context.assets.open(jsonFileName)
            val reader = InputStreamReader(inputStream)
            galleryGraph = Gson().fromJson(reader, GalleryGraph::class.java)
            reader.close()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getGalleryGraph(): GalleryGraph? = galleryGraph

    fun getStartGallery(): GalleryNode? {
        return galleryGraph?.let { graph ->
            graph.nodes[graph.startNodeId]
        }
    }

    fun getGalleryNode(nodeId: String): GalleryNode? {
        return galleryGraph?.nodes?.get(nodeId)
    }

    fun getNodeStatus(nodeId: String): NodeStatus {
        if (nodeId == galleryGraph?.startNodeId && prefs.getString("status_$nodeId", null) == null) {
            return NodeStatus.UNLOCKED
        }
        val statusString = prefs.getString("status_$nodeId", NodeStatus.LOCKED.name)
        return NodeStatus.valueOf(statusString ?: NodeStatus.LOCKED.name)
    }

    fun setNodeStatus(nodeId: String, status: NodeStatus) {
        prefs.edit {
            putString("status_$nodeId", status.name)
        }
    }

    fun unlockOutgoingNodesFor(nodeId: String) {
        getGalleryNode(nodeId)?.outgoingEdges?.forEach { unlockedNodeId ->
            // Only unlock if it's currently locked, to avoid overwriting a COMPLETED status
            if (getNodeStatus(unlockedNodeId) == NodeStatus.LOCKED) {
                setNodeStatus(unlockedNodeId, NodeStatus.UNLOCKED)
            }
        }
    }
}
