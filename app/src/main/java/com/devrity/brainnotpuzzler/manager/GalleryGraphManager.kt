package com.devrity.brainnotpuzzler.manager

import android.content.Context
import android.content.SharedPreferences
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
            val inputStream = context.assets.open("gallery_graph.json")
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

    fun isNodeUnlocked(nodeId: String): Boolean {
        if (nodeId == galleryGraph?.startNodeId) {
            return true
        }
        return prefs.getBoolean("unlocked_$nodeId", false)
    }

    fun getNodeStatus(nodeId: String): NodeStatus {
        val statusString = prefs.getString("status_$nodeId", null)
        return if (statusString != null) {
            NodeStatus.valueOf(statusString)
        } else if (isNodeUnlocked(nodeId)) {
            NodeStatus.UNLOCKED
        } else {
            NodeStatus.LOCKED
        }
    }

    fun setNodeStatus(nodeId: String, status: NodeStatus) {
        prefs.edit().putString("status_$nodeId", status.name).apply()
    }

    fun unlockOutgoingNodesFor(nodeId: String) {
        getGalleryNode(nodeId)?.outgoingEdges?.forEach { unlockedNodeId ->
            prefs.edit().putBoolean("unlocked_$unlockedNodeId", true).apply()
        }
    }
}
