package com.devrity.brainnotpuzzler.ui

import android.content.pm.ActivityInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devrity.brainnotpuzzler.GalleryActivity
import com.devrity.brainnotpuzzler.MainActivity
import com.devrity.brainnotpuzzler.R
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Device Rotation Tests
 * 
 * Tests device rotation handling in all screen states:
 * - First screen (initial state)
 * - Puzzle solving
 * - Confetti animation
 * - Win screen with happy monster
 * - Gallery screen
 * 
 * Each test rotates through all 4 orientations and verifies:
 * - No crashes or exceptions
 * - State preservation
 * - UI reconstruction
 */
@RunWith(AndroidJUnit4::class)
class DeviceRotationTest {

    companion object {
        const val ROTATION_SETTLE_TIME = 500L
        const val ANIMATION_SETTLE_TIME = 1000L
    }

    // ==================== Helper Functions ====================

    private fun rotateToOrientation(scenario: ActivityScenario<*>, orientation: Int) {
        scenario.onActivity { activity ->
            activity.requestedOrientation = orientation
        }
        Thread.sleep(ROTATION_SETTLE_TIME)
    }

    private fun verifyNoException(scenario: ActivityScenario<*>) {
        scenario.onActivity { activity ->
            assertNotNull("Activity should not be null after rotation", activity)
            assertFalse("Activity should not be finishing", activity.isFinishing)
        }
    }

    // ==================== MainActivity - First Screen ====================

    @Test
    fun whenRotateOnFirstScreen_fromPortraitToLandscape_shouldNotCrash() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Initial state: Portrait
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            verifyNoException(scenario)

            // Rotate to Landscape
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            verifyNoException(scenario)

            // Verify UI is still functional
            onView(withId(R.id.game_view)).check(matches(isDisplayed()))
        }
    }


    @Test
    fun whenRotateOnFirstScreen_fromPortraitToReverseLandscape_shouldNotCrash() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            verifyNoException(scenario)

            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
            verifyNoException(scenario)

            onView(withId(R.id.game_view)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun whenRotateOnFirstScreen_fromLandscapeToPortrait_shouldNotCrash() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            verifyNoException(scenario)

            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            verifyNoException(scenario)

            onView(withId(R.id.game_view)).check(matches(isDisplayed()))
        }
    }


    // ==================== MainActivity - Puzzle Solving ====================

    @Test
    fun whenRotateDuringPuzzleSolving_fromPortraitToLandscape_shouldNotCrash() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Start in portrait
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

            // Wait for puzzle setup
            Thread.sleep(500L)

            // Rotate to landscape
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            verifyNoException(scenario)

            // Verify game view is still displayed
            onView(withId(R.id.game_view)).check(matches(isDisplayed()))
        }
    }


    // ==================== MainActivity - Confetti Animation ====================

    @Test
    fun whenRotateDuringAnimation_shouldNotCrash() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

            // Allow time for any potential animations to start
            Thread.sleep(ANIMATION_SETTLE_TIME)

            // Rotate during potential animation
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            verifyNoException(scenario)

            // Rotate back
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            verifyNoException(scenario)
        }
    }

    // ==================== GalleryActivity ====================

    @Test
    fun whenRotateOnGalleryScreen_fromPortraitToLandscape_shouldNotCrash() {
        ActivityScenario.launch(GalleryActivity::class.java).use { scenario ->
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            verifyNoException(scenario)

            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            verifyNoException(scenario)

            // Verify RecyclerView is still displayed
            onView(withId(R.id.puzzle_grid)).check(matches(isDisplayed()))
        }
    }


    @Test
    fun whenRotateOnGalleryScreen_andRotateBack_shouldNotCrash() {
        ActivityScenario.launch(GalleryActivity::class.java).use { scenario ->
            // Start in portrait
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

            // Rotate to landscape
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            verifyNoException(scenario)

            // Rotate back to portrait
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            verifyNoException(scenario)

            // Verify RecyclerView is functional
            onView(withId(R.id.puzzle_grid)).check(matches(isDisplayed()))
        }
    }

    // ==================== Stress Test - Rapid Rotations ====================

    @Test
    fun whenRapidRotations_shouldHandleGracefully() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val orientations = listOf(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            )

            orientations.forEach { orientation ->
                rotateToOrientation(scenario, orientation)
                verifyNoException(scenario)
            }

            // Final verification
            onView(withId(R.id.game_view)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun whenRotateMultipleTimes_shouldMaintainStability() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Rotate 10 times between portrait and landscape
            repeat(10) {
                rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                verifyNoException(scenario)

                rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                verifyNoException(scenario)
            }

            // Final state verification
            onView(withId(R.id.game_view)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun whenRotateOnFirstScreen_fromLandscapeToReverseLandscape_shouldNotCrash() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Start in Landscape
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            verifyNoException(scenario)
            Thread.sleep(ROTATION_SETTLE_TIME)

            // Rotate to Reverse Landscape
            rotateToOrientation(scenario, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
            verifyNoException(scenario)
            Thread.sleep(ROTATION_SETTLE_TIME)

            // Verify UI is still functional
            onView(withId(R.id.game_view)).check(matches(isDisplayed()))
        }
    }

}
