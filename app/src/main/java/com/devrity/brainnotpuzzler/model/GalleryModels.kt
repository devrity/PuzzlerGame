package com.devrity.brainnotpuzzler.model

import com.google.gson.annotations.SerializedName

enum class NodeStatus {
    LOCKED, IN_PROGRESS, UNLOCKED, COMPLETED
}

enum class UnlockType {
    FREE, AD_REQUIRED, PREMIUM
}

data class GalleryNode(
    val id: String,
    val icon: String,
    val puzzleFolder: String,
    val totalPuzzles: Int,
    val unlockType: String,
    val requiredCompletions: Int,
    val initState: List<String>?,
    @SerializedName("incomingEdges")
    val incomingEdges: List<String>,
    @SerializedName("outgoingEdges")
    val outgoingEdges: List<String>,
    @SerializedName("puzzleImagesRef")
    val puzzleImagesRef: List<String>
)

data class GalleryGraph(
    val version: String,
    val startNodeId: String,
    val nodes: Map<String, GalleryNode>
)
