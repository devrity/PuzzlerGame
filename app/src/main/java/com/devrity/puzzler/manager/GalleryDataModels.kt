package com.devrity.puzzler.manager

import com.google.gson.annotations.SerializedName

// Enums
enum class NodeStatus {
    LOCKED, IN_PROGRESS, UNLOCKED, COMPLETED
}

enum class UnlockType {
    FREE, AD_REQUIRED, PREMIUM
}

// Data Models
data class GalleryNode(
    val id: String,
    val icon: String,
    val puzzleFolder: String,
    val totalPuzzles: Int,
    val status: String,
    val unlockType: String,
    val requiredCompletions: Int,
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

data class GalleryNodeStatus(
    val nodeId: String,
    val completionCount: Int,
    val isUnlocked: Boolean,
    val nodeStatus: String
)
