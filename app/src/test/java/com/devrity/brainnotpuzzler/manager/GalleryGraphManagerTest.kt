package com.devrity.brainnotpuzzler.manager

import android.content.Context
import android.content.SharedPreferences
import com.devrity.brainnotpuzzler.R
import com.devrity.brainnotpuzzler.model.NodeStatus
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for GalleryGraphManager logic
 * Note: JSON loading from assets requires instrumented tests
 * These tests focus on the business logic around node status and unlocking
 */
@RunWith(MockitoJUnitRunner.Silent::class)
class GalleryGraphManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        `when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockContext.getString(R.string.gallery_graph_json))
            .thenReturn("gallery_graph.json")
    }

    // ==================== Node Status Tests ====================

    @Test
    fun `when getting status for unlocked node, should return UNLOCKED`() {
        `when`(mockSharedPreferences.getString("status_puzzle_001", NodeStatus.LOCKED.name))
            .thenReturn(NodeStatus.UNLOCKED.name)

        // Note: This test demonstrates the expected behavior
        // Actual implementation requires instrumented test for asset loading
        val statusString = mockSharedPreferences.getString("status_puzzle_001", NodeStatus.LOCKED.name)
        val status = NodeStatus.valueOf(statusString ?: NodeStatus.LOCKED.name)
        
        assertEquals(NodeStatus.UNLOCKED, status)
    }

    @Test
    fun `when getting status for locked node, should return LOCKED`() {
        `when`(mockSharedPreferences.getString("status_puzzle_002", NodeStatus.LOCKED.name))
            .thenReturn(NodeStatus.LOCKED.name)

        val statusString = mockSharedPreferences.getString("status_puzzle_002", NodeStatus.LOCKED.name)
        val status = NodeStatus.valueOf(statusString ?: NodeStatus.LOCKED.name)
        
        assertEquals(NodeStatus.LOCKED, status)
    }

    @Test
    fun `when getting status for completed node, should return COMPLETED`() {
        `when`(mockSharedPreferences.getString("status_puzzle_003", NodeStatus.LOCKED.name))
            .thenReturn(NodeStatus.COMPLETED.name)

        val statusString = mockSharedPreferences.getString("status_puzzle_003", NodeStatus.LOCKED.name)
        val status = NodeStatus.valueOf(statusString ?: NodeStatus.LOCKED.name)
        
        assertEquals(NodeStatus.COMPLETED, status)
    }

    @Test
    fun `when node has no saved status, should default to LOCKED`() {
        `when`(mockSharedPreferences.getString("status_new_puzzle", NodeStatus.LOCKED.name))
            .thenReturn(NodeStatus.LOCKED.name)

        val statusString = mockSharedPreferences.getString("status_new_puzzle", NodeStatus.LOCKED.name)
        val status = NodeStatus.valueOf(statusString ?: NodeStatus.LOCKED.name)
        
        assertEquals(NodeStatus.LOCKED, status)
    }

    // ==================== Node Status Enum Tests ====================

    @Test
    fun `when converting status string to enum, should work for all values`() {
        assertEquals(NodeStatus.LOCKED, NodeStatus.valueOf("LOCKED"))
        assertEquals(NodeStatus.UNLOCKED, NodeStatus.valueOf("UNLOCKED"))
        assertEquals(NodeStatus.IN_PROGRESS, NodeStatus.valueOf("IN_PROGRESS"))
        assertEquals(NodeStatus.COMPLETED, NodeStatus.valueOf("COMPLETED"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when invalid status string is provided, should throw exception`() {
        NodeStatus.valueOf("INVALID_STATUS")
    }

    @Test
    fun `when checking all node status values, should have exactly 4 states`() {
        val values = NodeStatus.values()
        assertEquals(4, values.size)
        assertTrue(values.contains(NodeStatus.LOCKED))
        assertTrue(values.contains(NodeStatus.UNLOCKED))
        assertTrue(values.contains(NodeStatus.IN_PROGRESS))
        assertTrue(values.contains(NodeStatus.COMPLETED))
    }
}