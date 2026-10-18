package com.example.ime

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Region
import android.graphics.drawable.ColorDrawable
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import com.example.MainActivity
import com.example.data.KeyboardPreferences

class SplitKeyboardService : InputMethodService() {

    private lateinit var prefs: KeyboardPreferences
    private var keyboardView: SplitKeyboardView? = null
    private var audioManager: AudioManager? = null
    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        prefs = KeyboardPreferences(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onConfigureWindow(win: Window, isFullscreen: Boolean, isCandidatesOnly: Boolean) {
        super.onConfigureWindow(win, isFullscreen, isCandidatesOnly)
        // Ensure transparent backdrop and non-modal behavior
        win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        win.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        win.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        win.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        // Prevent Android from forcing fullscreen extract mode in landscape tablet orientation.
        return false
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return true
    }

    override fun onCreateInputView(): View {
        keyboardView = SplitKeyboardView(
            context = this,
            prefs = prefs,
            onKeyAction = { key -> handleKey(key) },
            onDismissKeyboard = { requestHideSelf(0) },
            onOpenSettings = {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
            },
            onLayoutChangedListener = {
                // Request a layout pass on the root decor view to trigger onComputeInsets
                window?.window?.decorView?.post {
                    window?.window?.decorView?.requestLayout()
                }
            }
        )
        return keyboardView!!
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        keyboardView?.let { view ->
            view.applyPanelGeometry()
            view.rebuildKeys()
        }
    }

    private fun handleKey(key: KeyModel) {
        triggerFeedback()
        val ic = currentInputConnection ?: return

        when (key.type) {
            KeyType.CHAR -> {
                ic.commitText(key.primary, 1)
            }
            KeyType.SPACE -> {
                ic.commitText(" ", 1)
            }
            KeyType.BACKSPACE -> {
                val selected = ic.getSelectedText(0)
                if (selected.isNullOrEmpty()) {
                    ic.deleteSurroundingText(1, 0)
                } else {
                    ic.commitText("", 1)
                }
            }
            KeyType.ENTER -> {
                val info = currentInputEditorInfo
                val imeOptions = info?.imeOptions ?: 0
                val actionId = imeOptions and EditorInfo.IME_MASK_ACTION
                if (actionId != EditorInfo.IME_ACTION_NONE && (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) == 0) {
                    ic.performEditorAction(actionId)
                } else {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                }
            }
            else -> {}
        }
    }

    private fun triggerFeedback() {
        if (prefs.soundFeedback) {
            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.4f)
        }
        if (prefs.hapticFeedback && vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        }
    }

    /**
     * Critical method: Informs WindowManager of the exact touchable region.
     * By setting TOUCHABLE_INSETS_REGION and adding only the Left and Right panels,
     * the center gap between them is 100% transparent to touch events,
     * passing through directly to the underlying application.
     */
    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        val view = keyboardView ?: return
        val left = view.leftCard
        val right = view.rightCard

        if (left.width <= 0 || right.width <= 0) {
            return
        }

        val leftLoc = IntArray(2)
        val rightLoc = IntArray(2)
        left.getLocationInWindow(leftLoc)
        right.getLocationInWindow(rightLoc)

        val leftRect = Rect(
            leftLoc[0],
            leftLoc[1],
            leftLoc[0] + left.width,
            leftLoc[1] + left.height
        )

        val rightRect = Rect(
            rightLoc[0],
            rightLoc[1],
            rightLoc[0] + right.width,
            rightLoc[1] + right.height
        )

        outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_REGION
        outInsets.touchableRegion.set(leftRect)
        outInsets.touchableRegion.op(rightRect, Region.Op.UNION)

        // Prevent pushing/cropping the underlying app content
        val totalHeight = view.height
        if (totalHeight > 0) {
            outInsets.contentTopInsets = totalHeight
            outInsets.visibleTopInsets = totalHeight
        }
    }
}
