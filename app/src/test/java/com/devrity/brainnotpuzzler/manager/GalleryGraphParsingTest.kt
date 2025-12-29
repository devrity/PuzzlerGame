package com.devrity.brainnotpuzzler.manager

import com.devrity.brainnotpuzzler.model.GalleryGraph
import com.devrity.brainnotpuzzler.model.GalleryNode
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for JSON parsing of gallery graph and puzzle configurations
 * Tests cover: valid/invalid JSON, node structure, puzzle configs, edge cases
 */
class GalleryGraphParsingTest {

    private lateinit var gson: Gson

    @Before
    fun setup() {
        gson = Gson()
    }

    // ==================== Valid JSON Parsing Tests ====================

    @Test
    fun `when valid gallery graph JSON is parsed, should create correct object`() {
        val json = """
            {
                "version": "1.0",
                "startNodeId": "puzzle_001",
                "nodes": {
                    "puzzle_001": {
                        "id": "puzzle_001",
                        "icon": "icon_001.png",
                        "puzzleFolder": "puzzles/puzzle_001.jpg",
                        "puzzleSize": 3,
                        "totalPuzzles": 1,
                        "unlockType": "FREE",
                        "requiredCompletions": 0,
                        "initState": null,
                        "emptyPieceId": null,
                        "incomingEdges": [],
                        "outgoingEdges": ["puzzle_002"],
                        "puzzleImagesRef": []
                    }
                }
            }
        """.trimIndent()

        val graph = gson.fromJson(json, GalleryGraph::class.java)

        assertNotNull(graph)
        assertEquals("1.0", graph.version)
        assertEquals("puzzle_001", graph.startNodeId)
        assertEquals(1, graph.nodes.size)
    }

    @Test
    fun `when valid gallery node is parsed, should have all properties`() {
        val json = """
            {
                "id": "puzzle_001",
                "icon": "icon_001.png",
                "puzzleFolder": "puzzles/puzzle_001.jpg",
                "puzzleSize": 3,
                "totalPuzzles": 1,
                "unlockType": "FREE",
                "requiredCompletions": 0,
                "initState": ["0", "1", "2", "3", "4", "5", "6", "7", "E"],
                "emptyPieceId": 8,
                "incomingEdges": [],
                "outgoingEdges": ["puzzle_002"],
                "puzzleImagesRef": ["img1.jpg"]
            }
        """.trimIndent()

        val node = gson.fromJson(json, GalleryNode::class.java)

        assertEquals("puzzle_001", node.id)
        assertEquals("icon_001.png", node.icon)
        assertEquals("puzzles/puzzle_001.jpg", node.puzzleFolder)
        assertEquals(3, node.puzzleSize)
        assertEquals(1, node.totalPuzzles)
        assertEquals("FREE", node.unlockType)
        assertEquals(0, node.requiredCompletions)
        assertNotNull(node.initState)
        assertEquals(9, node.initState?.size)
        assertEquals(8, node.emptyPieceId)
        assertEquals(0, node.incomingEdges.size)
        assertEquals(1, node.outgoingEdges.size)
        assertEquals("puzzle_002", node.outgoingEdges[0])
    }

    @Test
    fun `when puzzle has init state with locked tiles, should parse correctly`() {
        val json = """
            {
                "id": "puzzle_locked",
                "icon": "icon.png",
                "puzzleFolder": "puzzles/locked.jpg",
                "puzzleSize": 2,
                "totalPuzzles": 1,
                "unlockType": "FREE",
                "requiredCompletions": 0,
                "initState": ["0_L", "1", "2_L", "E"],
                "emptyPieceId": 3,
                "incomingEdges": [],
                "outgoingEdges": [],
                "puzzleImagesRef": []
            }
        """.trimIndent()

        val node = gson.fromJson(json, GalleryNode::class.java)

        assertNotNull(node.initState)
        assertEquals("0_L", node.initState?.get(0))
        assertEquals("1", node.initState?.get(1))
        assertEquals("2_L", node.initState?.get(2))
        assertEquals("E", node.initState?.get(3))
    }

    @Test
    fun `when puzzle has multiple empty spaces, should parse correctly`() {
        val json = """
            {
                "id": "puzzle_multi_empty",
                "icon": "icon.png",
                "puzzleFolder": "puzzles/multi.jpg",
                "puzzleSize": 3,
                "totalPuzzles": 1,
                "unlockType": "FREE",
                "requiredCompletions": 0,
                "initState": ["0", "E", "1", "2", "E", "3", "4", "5", "6"],
                "emptyPieceId": null,
                "incomingEdges": [],
                "outgoingEdges": [],
                "puzzleImagesRef": []
            }
        """.trimIndent()

        val node = gson.fromJson(json, GalleryNode::class.java)

        assertNotNull(node.initState)
        val emptyCount = node.initState?.count { it == "E" }
        assertEquals(2, emptyCount)
    }

    @Test
    fun `when multiple nodes are in graph, should parse all correctly`() {
        val json = """
            {
                "version": "1.0",
                "startNodeId": "puzzle_001",
                "nodes": {
                    "puzzle_001": {
                        "id": "puzzle_001",
                        "icon": "icon_001.png",
                        "puzzleFolder": "puzzles/puzzle_001.jpg",
                        "puzzleSize": 2,
                        "totalPuzzles": 1,
                        "unlockType": "FREE",
                        "requiredCompletions": 0,
                        "initState": null,
                        "emptyPieceId": null,
                        "incomingEdges": [],
                        "outgoingEdges": ["puzzle_002"],
                        "puzzleImagesRef": []
                    },
                    "puzzle_002": {
                        "id": "puzzle_002",
                        "icon": "icon_002.png",
                        "puzzleFolder": "puzzles/puzzle_002.jpg",
                        "puzzleSize": 3,
                        "totalPuzzles": 1,
                        "unlockType": "AD_REQUIRED",
                        "requiredCompletions": 1,
                        "initState": null,
                        "emptyPieceId": null,
                        "incomingEdges": ["puzzle_001"],
                        "outgoingEdges": [],
                        "puzzleImagesRef": []
                    }
                }
            }
        """.trimIndent()

        val graph = gson.fromJson(json, GalleryGraph::class.java)

        assertEquals(2, graph.nodes.size)
        assertNotNull(graph.nodes["puzzle_001"])
        assertNotNull(graph.nodes["puzzle_002"])
        
        val node1 = graph.nodes["puzzle_001"]!!
        val node2 = graph.nodes["puzzle_002"]!!
        
        assertEquals(1, node1.outgoingEdges.size)
        assertEquals("puzzle_002", node1.outgoingEdges[0])
        assertEquals(1, node2.incomingEdges.size)
        assertEquals("puzzle_001", node2.incomingEdges[0])
    }

    @Test
    fun `when node has different puzzle sizes, should parse correctly`() {
        val sizes = listOf(2, 3, 4, 5, 9)
        
        sizes.forEach { size ->
            val json = """
                {
                    "id": "puzzle_$size",
                    "icon": "icon.png",
                    "puzzleFolder": "puzzles/puzzle.jpg",
                    "puzzleSize": $size,
                    "totalPuzzles": 1,
                    "unlockType": "FREE",
                    "requiredCompletions": 0,
                    "initState": null,
                    "emptyPieceId": null,
                    "incomingEdges": [],
                    "outgoingEdges": [],
                    "puzzleImagesRef": []
                }
            """.trimIndent()

            val node = gson.fromJson(json, GalleryNode::class.java)
            assertEquals(size, node.puzzleSize)
        }
    }

    // ==================== Null and Optional Fields Tests ====================

    @Test
    fun `when initState is null, should parse without error`() {
        val json = """
            {
                "id": "puzzle_001",
                "icon": "icon.png",
                "puzzleFolder": "puzzles/puzzle.jpg",
                "puzzleSize": 3,
                "totalPuzzles": 1,
                "unlockType": "FREE",
                "requiredCompletions": 0,
                "initState": null,
                "emptyPieceId": null,
                "incomingEdges": [],
                "outgoingEdges": [],
                "puzzleImagesRef": []
            }
        """.trimIndent()

        val node = gson.fromJson(json, GalleryNode::class.java)
        assertNull(node.initState)
        assertNull(node.emptyPieceId)
    }

    @Test
    fun `when edges are empty arrays, should parse correctly`() {
        val json = """
            {
                "id": "puzzle_isolated",
                "icon": "icon.png",
                "puzzleFolder": "puzzles/puzzle.jpg",
                "puzzleSize": 3,
                "totalPuzzles": 1,
                "unlockType": "FREE",
                "requiredCompletions": 0,
                "initState": null,
                "emptyPieceId": null,
                "incomingEdges": [],
                "outgoingEdges": [],
                "puzzleImagesRef": []
            }
        """.trimIndent()

        val node = gson.fromJson(json, GalleryNode::class.java)
        assertTrue(node.incomingEdges.isEmpty())
        assertTrue(node.outgoingEdges.isEmpty())
    }

    @Test
    fun `when requiredCompletions is zero, should parse correctly`() {
        val json = """
            {
                "id": "puzzle_free",
                "icon": "icon.png",
                "puzzleFolder": "puzzles/puzzle.jpg",
                "puzzleSize": 3,
                "totalPuzzles": 1,
                "unlockType": "FREE",
                "requiredCompletions": 0,
                "initState": null,
                "emptyPieceId": null,
                "incomingEdges": [],
                "outgoingEdges": [],
                "puzzleImagesRef": []
            }
        """.trimIndent()

        val node = gson.fromJson(json, GalleryNode::class.java)
        assertEquals(0, node.requiredCompletions)
    }

    // ==================== Invalid JSON Tests ====================

    @Test(expected = JsonSyntaxException::class)
    fun `when JSON is malformed, should throw JsonSyntaxException`() {
        val malformedJson = """
            {
                "version": "1.0",
                "startNodeId": "puzzle_001"
                "nodes": {  // Missing comma
        """.trimIndent()

        gson.fromJson(malformedJson, GalleryGraph::class.java)
    }

    @Test(expected = JsonSyntaxException::class)
    fun `when JSON has invalid number format, should throw exception`() {
        val json = """
            {
                "id": "puzzle_001",
                "icon": "icon.png",
                "puzzleFolder": "puzzles/puzzle.jpg",
                "puzzleSize": "not_a_number",
                "totalPuzzles": 1,
                "unlockType": "FREE",
                "requiredCompletions": 0,
                "initState": null,
                "emptyPieceId": null,
                "incomingEdges": [],
                "outgoingEdges": [],
                "puzzleImagesRef": []
            }
        """.trimIndent()

        gson.fromJson(json, GalleryNode::class.java)
    }

    @Test
    fun `when JSON has extra unknown fields, should ignore them`() {
        val json = """
            {
                "id": "puzzle_001",
                "icon": "icon.png",
                "puzzleFolder": "puzzles/puzzle.jpg",
                "puzzleSize": 3,
                "totalPuzzles": 1,
                "unlockType": "FREE",
                "requiredCompletions": 0,
                "initState": null,
                "emptyPieceId": null,
                "incomingEdges": [],
                "outgoingEdges": [],
                "puzzleImagesRef": [],
                "unknownField": "should be ignored",
                "anotherUnknownField": 123
            }
        """.trimIndent()

        val node = gson.fromJson(json, GalleryNode::class.java)
        assertNotNull(node)
        assertEquals("puzzle_001", node.id)
    }

    // ==================== Edge Cases and Complex Scenarios ====================

    @Test
    fun `when graph has circular dependencies, should parse structure`() {
        val json = """
            {
                "version": "1.0",
                "startNodeId": "puzzle_001",
                "nodes": {
                    "puzzle_001": {
                        "id": "puzzle_001",
                        "icon": "icon.png",
                        "puzzleFolder": "puzzles/puzzle_001.jpg",
                        "puzzleSize": 3,
                        "totalPuzzles": 1,
                        "unlockType": "FREE",
                        "requiredCompletions": 0,
                        "initState": null,
                        "emptyPieceId": null,
                        "incomingEdges": ["puzzle_002"],
                        "outgoingEdges": ["puzzle_002"],
                        "puzzleImagesRef": []
                    },
                    "puzzle_002": {
                        "id": "puzzle_002",
                        "icon": "icon.png",
                        "puzzleFolder": "puzzles/puzzle_002.jpg",
                        "puzzleSize": 3,
                        "totalPuzzles": 1,
                        "unlockType": "FREE",
                        "requiredCompletions": 0,
                        "initState": null,
                        "emptyPieceId": null,
                        "incomingEdges": ["puzzle_001"],
                        "outgoingEdges": ["puzzle_001"],
                        "puzzleImagesRef": []
                    }
                }
            }
        """.trimIndent()

        val graph = gson.fromJson(json, GalleryGraph::class.java)
        assertNotNull(graph)
        assertEquals(2, graph.nodes.size)
    }

    @Test
    fun `when node has complex init state, should parse all elements`() {
        val json = """
            {
                "id": "puzzle_complex",
                "icon": "icon.png",
                "puzzleFolder": "puzzles/complex.jpg",
                "puzzleSize": 4,
                "totalPuzzles": 1,
                "unlockType": "PREMIUM",
                "requiredCompletions": 5,
                "initState": ["0_L", "1", "E", "3", "4_L", "5", "6", "E", "8", "9_L", "10", "11", "12", "13", "14", "E"],
                "emptyPieceId": null,
                "incomingEdges": ["puzzle_001", "puzzle_002"],
                "outgoingEdges": ["puzzle_003", "puzzle_004", "puzzle_005"],
                "puzzleImagesRef": ["img1.jpg", "img2.jpg", "img3.jpg"]
            }
        """.trimIndent()

        val node = gson.fromJson(json, GalleryNode::class.java)
        
        assertEquals(16, node.initState?.size)
        assertEquals(3, node.initState?.count { it == "E" })
        assertEquals(3, node.initState?.count { it.endsWith("_L") })
        assertEquals(2, node.incomingEdges.size)
        assertEquals(3, node.outgoingEdges.size)
        assertEquals(3, node.puzzleImagesRef.size)
    }

    @Test
    fun `when parsing different unlock types, should preserve values`() {
        val unlockTypes = listOf("FREE", "AD_REQUIRED", "PREMIUM")
        
        unlockTypes.forEach { type ->
            val json = """
                {
                    "id": "puzzle_$type",
                    "icon": "icon.png",
                    "puzzleFolder": "puzzles/puzzle.jpg",
                    "puzzleSize": 3,
                    "totalPuzzles": 1,
                    "unlockType": "$type",
                    "requiredCompletions": 0,
                    "initState": null,
                    "emptyPieceId": null,
                    "incomingEdges": [],
                    "outgoingEdges": [],
                    "puzzleImagesRef": []
                }
            """.trimIndent()

            val node = gson.fromJson(json, GalleryNode::class.java)
            assertEquals(type, node.unlockType)
        }
    }

    @Test
    fun `when empty graph is parsed, should have empty nodes map`() {
        val json = """
            {
                "version": "1.0",
                "startNodeId": "puzzle_001",
                "nodes": {}
            }
        """.trimIndent()

        val graph = gson.fromJson(json, GalleryGraph::class.java)
        assertNotNull(graph)
        assertTrue(graph.nodes.isEmpty())
    }

    @Test
    fun `when node has maximum complexity, should parse all data`() {
        val json = """
            {
                "id": "puzzle_max",
                "icon": "icons/very/deep/path/icon.png",
                "puzzleFolder": "puzzles/category/subcategory/puzzle.jpg",
                "puzzleSize": 9,
                "totalPuzzles": 100,
                "unlockType": "PREMIUM",
                "requiredCompletions": 99,
                "initState": ["0_L", "1", "2_L", "3", "4_L", "5", "6_L", "7", "8_L", "9", "10_L", "11", "12_L", "13", "14_L", "15", "16_L", "17", "18_L", "19", "20_L", "21", "22_L", "23", "24_L", "25", "26_L", "27", "28_L", "29", "30_L", "31", "32_L", "33", "34_L", "35", "36_L", "37", "38_L", "39", "40_L", "41", "42_L", "43", "44_L", "45", "46_L", "47", "48_L", "49", "50_L", "51", "52_L", "53", "54_L", "55", "56_L", "57", "58_L", "59", "60_L", "61", "62_L", "63", "64_L", "65", "66_L", "67", "68_L", "69", "70_L", "71", "72_L", "73", "74_L", "75", "76_L", "77", "78_L", "79", "E"],
                "emptyPieceId": 80,
                "incomingEdges": ["p1", "p2", "p3", "p4", "p5"],
                "outgoingEdges": ["p6", "p7", "p8", "p9", "p10"],
                "puzzleImagesRef": ["img1.jpg", "img2.jpg", "img3.jpg", "img4.jpg", "img5.jpg"]
            }
        """.trimIndent()

        val node = gson.fromJson(json, GalleryNode::class.java)
        
        assertEquals(9, node.puzzleSize)
        assertEquals(81, node.initState?.size)
        assertEquals(5, node.incomingEdges.size)
        assertEquals(5, node.outgoingEdges.size)
        assertEquals(99, node.requiredCompletions)
    }

    // ==================== SerializedName Annotation Tests ====================

    @Test
    fun `when using SerializedName annotations, should map correctly`() {
        // Test that @SerializedName works for incomingEdges and outgoingEdges
        val json = """
            {
                "id": "puzzle_001",
                "icon": "icon.png",
                "puzzleFolder": "puzzles/puzzle.jpg",
                "puzzleSize": 3,
                "totalPuzzles": 1,
                "unlockType": "FREE",
                "requiredCompletions": 0,
                "initState": null,
                "emptyPieceId": null,
                "incomingEdges": ["prev1", "prev2"],
                "outgoingEdges": ["next1", "next2"],
                "puzzleImagesRef": ["ref1.jpg"]
            }
        """.trimIndent()

        val node = gson.fromJson(json, GalleryNode::class.java)
        
        assertEquals(2, node.incomingEdges.size)
        assertEquals("prev1", node.incomingEdges[0])
        assertEquals("prev2", node.incomingEdges[1])
        assertEquals(2, node.outgoingEdges.size)
        assertEquals("next1", node.outgoingEdges[0])
        assertEquals("next2", node.outgoingEdges[1])
        assertEquals(1, node.puzzleImagesRef.size)
    }
}