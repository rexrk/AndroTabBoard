package com.example

import android.content.Context
import android.graphics.Rect
import android.graphics.Region
import android.inputmethodservice.InputMethodService
import androidx.test.core.app.ApplicationProvider
import com.example.data.KeyboardPreferences
import com.example.ime.KeyModel
import com.example.ime.KeyType
import com.example.ime.KeyboardLayout
import com.example.ime.KeyboardMode
import com.example.ime.ShiftState
import com.example.ime.SplitKeyboardService
import com.example.ime.SplitKeyboardView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SplitKeyboardTest {

    @Test
    fun testPreferencesDefaultAndPersistence() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = KeyboardPreferences(context)
        prefs.resetGeometry()

        assertEquals(270f, prefs.panelWidthDp, 0.01f)
        assertEquals(260f, prefs.panelHeightDp, 0.01f)
        assertEquals(28f, prefs.panelBottomMarginDp, 0.01f)
        assertEquals(0f, prefs.leftPanelOffsetYDp, 0.01f)
        assertEquals(0f, prefs.rightPanelOffsetYDp, 0.01f)
        assertTrue(prefs.hapticFeedback)
        assertTrue(prefs.soundFeedback)

        // Modify and verify persistence
        prefs.panelWidthDp = 300f
        prefs.leftPanelOffsetYDp = 40f
        assertEquals(300f, prefs.panelWidthDp, 0.01f)
        assertEquals(40f, prefs.leftPanelOffsetYDp, 0.01f)

        // Reset
        prefs.resetGeometry()
        assertEquals(270f, prefs.panelWidthDp, 0.01f)
        assertEquals(0f, prefs.leftPanelOffsetYDp, 0.01f)
    }

    @Test
    fun testKeyboardLayoutKeysPresent() {
        // Left panel in letters mode
        val leftRows = KeyboardLayout.getLeftPanelRows(KeyboardMode.LETTERS, showNumberRow = false)
        val leftKeys = leftRows.flatten()

        assertTrue("Left panel should contain 'q'", leftKeys.any { it.primary == "q" })
        assertTrue("Left panel should contain 't'", leftKeys.any { it.primary == "t" })
        assertTrue("Left panel should contain Shift", leftKeys.any { it.type == KeyType.SHIFT })
        assertTrue("Left panel should contain Space", leftKeys.any { it.type == KeyType.SPACE })
        assertTrue("Left panel should contain Symbols toggle", leftKeys.any { it.type == KeyType.SYMBOLS_TOGGLE })

        // Right panel in letters mode
        val rightRows = KeyboardLayout.getRightPanelRows(KeyboardMode.LETTERS, showNumberRow = false)
        val rightKeys = rightRows.flatten()

        assertTrue("Right panel should contain 'y'", rightKeys.any { it.primary == "y" })
        assertTrue("Right panel should contain 'p'", rightKeys.any { it.primary == "p" })
        assertTrue("Right panel should contain Backspace", rightKeys.any { it.type == KeyType.BACKSPACE })
        assertTrue("Right panel should contain Enter", rightKeys.any { it.type == KeyType.ENTER })
        assertTrue("Right panel should contain Space", rightKeys.any { it.type == KeyType.SPACE })
        assertTrue("Right panel should contain Hide Keyboard", rightKeys.any { it.type == KeyType.HIDE_KEYBOARD })
    }

    @Test
    fun testSymbolsModeLayout() {
        val leftRows = KeyboardLayout.getLeftPanelRows(KeyboardMode.SYMBOLS, showNumberRow = false)
        val leftKeys = leftRows.flatten()

        assertTrue("Symbols left should have numbers 1-5", leftKeys.any { it.primary == "1" })
        assertTrue("Symbols left should have '@'", leftKeys.any { it.primary == "@" })
        assertTrue("Symbols left should have ABC toggle", leftKeys.any { it.primary == "ABC" })

        val rightRows = KeyboardLayout.getRightPanelRows(KeyboardMode.SYMBOLS, showNumberRow = false)
        val rightKeys = rightRows.flatten()

        assertTrue("Symbols right should have numbers 6-0", rightKeys.any { it.primary == "0" })
        assertTrue("Symbols right should have '!'", rightKeys.any { it.primary == "!" })
    }

    @Test
    fun testTouchableRegionIncludesPanelsAndExcludesCenter() {
        val service = Robolectric.buildService(SplitKeyboardService::class.java).create().get()
        assertNotNull(service)

        val insets = InputMethodService.Insets()

        // Simulate a tablet screen 1280x800:
        // Left panel at (20, 480, 360, 780)
        // Right panel at (920, 480, 1260, 780)
        // Center gap between X=360 and X=920
        val leftRect = Rect(20, 480, 360, 780)
        val rightRect = Rect(920, 480, 1260, 780)

        insets.touchableInsets = InputMethodService.Insets.TOUCHABLE_INSETS_REGION
        insets.touchableRegion.set(leftRect)
        insets.touchableRegion.op(rightRect, Region.Op.UNION)

        // 1. Verify Left panel touches are intercepted by IME
        assertTrue("Left panel center should be touchable by IME", insets.touchableRegion.contains(180, 600))
        assertTrue("Left panel edge should be touchable by IME", insets.touchableRegion.contains(25, 500))

        // 2. Verify Right panel touches are intercepted by IME
        assertTrue("Right panel center should be touchable by IME", insets.touchableRegion.contains(1000, 600))
        assertTrue("Right panel edge should be touchable by IME", insets.touchableRegion.contains(1250, 750))

        // 3. CRITICAL VERIFICATION: Verify Center gap is completely EXCLUDED from IME touchable region!
        assertFalse(
            "Screen center (X=640, Y=600) MUST NOT be touchable by IME (must fall through to underlying app)",
            insets.touchableRegion.contains(640, 600)
        )
        assertFalse(
            "Center top (X=640, Y=200) MUST NOT be touchable by IME",
            insets.touchableRegion.contains(640, 200)
        )
        assertFalse(
            "Point just outside left panel (X=370, Y=600) MUST NOT be touchable by IME",
            insets.touchableRegion.contains(370, 600)
        )
        assertFalse(
            "Point just outside right panel (X=910, Y=600) MUST NOT be touchable by IME",
            insets.touchableRegion.contains(910, 600)
        )
    }
}
