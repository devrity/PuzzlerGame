package com.devrity.brainnotpuzzler.manager

import com.google.gson.annotations.SerializedName

/**
 * Represents the entire puzzle progression, loaded from JSON.
 */
data class PuzzleProgression(
    val version: String,
    val puzzles: List<PuzzleNode>
)

/**
 * Represents a single puzzle in the progression tree.
 *
 * @param id The unique ID of the puzzle (usually the image filename).
 * @param unlocks The ID of the puzzle that this one unlocks upon completion.
 */
data class PuzzleNode(
    val id: String,
    val unlocks: String?
)

/**
 * Represents the current state of a puzzle for the player.
 */
data class PuzzleState(
    val node: PuzzleNode,
    var isUnlocked: Boolean
)
