package com.example.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages user preferences for the Split Keyboard geometry and behavior.
 */
class KeyboardPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var panelWidthDp: Float
        get() = prefs.getFloat(KEY_PANEL_WIDTH, 270f)
        set(value) = prefs.edit().putFloat(KEY_PANEL_WIDTH, value).apply()

    var panelHeightDp: Float
        get() = prefs.getFloat(KEY_PANEL_HEIGHT, 260f)
        set(value) = prefs.edit().putFloat(KEY_PANEL_HEIGHT, value).apply()

    var panelBottomMarginDp: Float
        get() = prefs.getFloat(KEY_PANEL_BOTTOM_MARGIN, 28f)
        set(value) = prefs.edit().putFloat(KEY_PANEL_BOTTOM_MARGIN, value).apply()

    var leftPanelOffsetYDp: Float
        get() = prefs.getFloat(KEY_LEFT_OFFSET_Y, 0f)
        set(value) = prefs.edit().putFloat(KEY_LEFT_OFFSET_Y, value).apply()

    var rightPanelOffsetYDp: Float
        get() = prefs.getFloat(KEY_RIGHT_OFFSET_Y, 0f)
        set(value) = prefs.edit().putFloat(KEY_RIGHT_OFFSET_Y, value).apply()

    var hapticFeedback: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    var soundFeedback: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var showNumberRow: Boolean
        get() = prefs.getBoolean(KEY_SHOW_NUMBERS, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_NUMBERS, value).apply()

    var panelAlpha: Float
        get() = prefs.getFloat(KEY_PANEL_ALPHA, 0.94f)
        set(value) = prefs.edit().putFloat(KEY_PANEL_ALPHA, value).apply()

    fun resetGeometry() {
        prefs.edit()
            .putFloat(KEY_PANEL_WIDTH, 270f)
            .putFloat(KEY_PANEL_HEIGHT, 260f)
            .putFloat(KEY_PANEL_BOTTOM_MARGIN, 28f)
            .putFloat(KEY_LEFT_OFFSET_Y, 0f)
            .putFloat(KEY_RIGHT_OFFSET_Y, 0f)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "split_keyboard_prefs"
        private const val KEY_PANEL_WIDTH = "panel_width_dp"
        private const val KEY_PANEL_HEIGHT = "panel_height_dp"
        private const val KEY_PANEL_BOTTOM_MARGIN = "panel_bottom_margin_dp"
        private const val KEY_LEFT_OFFSET_Y = "left_offset_y_dp"
        private const val KEY_RIGHT_OFFSET_Y = "right_offset_y_dp"
        private const val KEY_HAPTIC = "haptic_feedback"
        private const val KEY_SOUND = "sound_feedback"
        private const val KEY_SHOW_NUMBERS = "show_numbers"
        private const val KEY_PANEL_ALPHA = "panel_alpha"
    }
}
