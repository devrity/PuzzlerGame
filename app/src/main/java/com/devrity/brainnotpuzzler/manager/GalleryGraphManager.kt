package com.devrity.brainnotpuzzler.manager

import android.content.Context
import com.google.gson.Gson
import java.io.InputStreamReader

class GalleryGraphManager(private val context: Context) {
    private var galleryGraph: GalleryGraph? = null

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

    fun getNextGalleries(nodeId: String): List<GalleryNode> {
        val nextGalleries = mutableListOf<GalleryNode>()
        galleryGraph?.let { graph ->
            graph.nodes[nodeId]?.let { currentNode ->
                currentNode.outgoingEdges.forEach { edgeId ->
                    graph.nodes.values.find { it.id == edgeId }?.let {
                        nextGalleries.add(it)
                    }
                }
            }
        }
        return nextGalleries
    }

    fun getGalleryNode(nodeId: String): GalleryNode? {
        return galleryGraph?.nodes?.get(nodeId)
    }
}
