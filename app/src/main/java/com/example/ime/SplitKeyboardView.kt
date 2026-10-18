package com.example.ime

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.example.data.KeyboardPreferences
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class SplitKeyboardView(
    context: Context,
    val prefs: KeyboardPreferences,
    private val onKeyAction: (KeyModel) -> Unit,
    private val onDismissKeyboard: () -> Unit,
    private val onOpenSettings: () -> Unit,
    private val onLayoutChangedListener: () -> Unit
) : FrameLayout(context) {

    val leftCard: FrameLayout
    val rightCard: FrameLayout

    private val leftKeysContainer: LinearLayout
    private val rightKeysContainer: LinearLayout

    private var currentMode = KeyboardMode.LETTERS
    private var currentShiftState = ShiftState.OFF
    private var lastShiftPressTime = 0L

    init {
        isMotionEventSplittingEnabled = true
        isClickable = false
        isFocusable = false
        setBackgroundColor(Color.TRANSPARENT)

        val density = resources.displayMetrics.density

        // Left Card Container
        leftCard = FrameLayout(context).apply {
            id = generateViewId()
            isMotionEventSplittingEnabled = true
            isClickable = true
            background = createCardDrawable(density)
        }

        // Right Card Container
        rightCard = FrameLayout(context).apply {
            id = generateViewId()
            isMotionEventSplittingEnabled = true
            isClickable = true
            background = createCardDrawable(density)
        }

        // Left Panel Inner Content
        val leftContent = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (6 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }
        val leftHeader = createDragHandle(context, "⠇⠇ LEFT", isLeft = true)
        leftKeysContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        leftContent.addView(leftHeader)
        leftContent.addView(leftKeysContainer)
        leftCard.addView(leftContent)

        // Right Panel Inner Content
        val rightContent = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (6 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }
        val rightHeader = createDragHandle(context, "RIGHT ⠇⠇", isLeft = false)
        rightKeysContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        rightContent.addView(rightHeader)
        rightContent.addView(rightKeysContainer)
        rightCard.addView(rightContent)

        addView(leftCard)
        addView(rightCard)

        applyPanelGeometry()
        rebuildKeys()

        // Notify insets whenever layout happens
        addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            onLayoutChangedListener()
        }
    }

    private fun createCardDrawable(density: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f * density
            val alphaInt = (prefs.panelAlpha * 255).roundToInt().coerceIn(120, 255)
            // Dark navy surface (#0F172A) with adjustable alpha
            setColor(Color.argb(alphaInt, 15, 23, 42))
            setStroke((1.5f * density).toInt(), Color.parseColor("#38BDF8")) // Cyan high-tech border
        }
    }

    fun applyPanelGeometry() {
        val density = resources.displayMetrics.density
        val cardWidth = (prefs.panelWidthDp * density).toInt()
        val cardHeight = (prefs.panelHeightDp * density).toInt()

        val baseMarginBottom = (prefs.panelBottomMarginDp * density).toInt()
        val leftMarginBottom = (baseMarginBottom + prefs.leftPanelOffsetYDp * density).toInt().coerceAtLeast(0)
        val rightMarginBottom = (baseMarginBottom + prefs.rightPanelOffsetYDp * density).toInt().coerceAtLeast(0)

        val leftParams = LayoutParams(cardWidth, cardHeight).apply {
            gravity = Gravity.START or Gravity.BOTTOM
            marginStart = (12 * density).toInt()
            bottomMargin = leftMarginBottom
        }
        leftCard.layoutParams = leftParams

        val rightParams = LayoutParams(cardWidth, cardHeight).apply {
            gravity = Gravity.END or Gravity.BOTTOM
            marginEnd = (12 * density).toInt()
            bottomMargin = rightMarginBottom
        }
        rightCard.layoutParams = rightParams

        // Update background alpha if changed
        leftCard.background = createCardDrawable(density)
        rightCard.background = createCardDrawable(density)

        requestLayout()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createDragHandle(context: Context, label: String, isLeft: Boolean): View {
        val density = resources.displayMetrics.density
        val handle = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            val verticalPad = (4 * density).toInt()
            setPadding(0, verticalPad, 0, (6 * density).toInt())
        }

        val text = TextView(context).apply {
            this.text = label
            textSize = 10f
            setTextColor(Color.parseColor("#94A3B8"))
            gravity = Gravity.CENTER
        }
        handle.addView(text)

        // Vertical dragging
        var startY = 0f
        var initialOffset = 0f

        handle.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    initialOffset = if (isLeft) prefs.leftPanelOffsetYDp else prefs.rightPanelOffsetYDp
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaPx = startY - event.rawY // Dragging up increases bottom margin
                    val deltaDp = deltaPx / density
                    val newOffset = (initialOffset + deltaDp).coerceIn(-120f, 220f)
                    if (isLeft) {
                        prefs.leftPanelOffsetYDp = newOffset
                    } else {
                        prefs.rightPanelOffsetYDp = newOffset
                    }
                    applyPanelGeometry()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    true
                }
                else -> false
            }
        }

        return handle
    }

    fun rebuildKeys() {
        leftKeysContainer.removeAllViews()
        rightKeysContainer.removeAllViews()

        val leftRows = KeyboardLayout.getLeftPanelRows(currentMode, prefs.showNumberRow)
        val rightRows = KeyboardLayout.getRightPanelRows(currentMode, prefs.showNumberRow)

        populateRows(leftKeysContainer, leftRows)
        populateRows(rightKeysContainer, rightRows)
    }

    private fun populateRows(container: LinearLayout, rows: List<List<KeyModel>>) {
        val density = resources.displayMetrics.density
        for (row in rows) {
            val rowView = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                ).apply {
                    bottomMargin = (2 * density).toInt()
                }
                isMotionEventSplittingEnabled = true
            }

            for (keyModel in row) {
                val keyView = KeyView(context, keyModel) { clickedKey ->
                    handleKeyInteraction(clickedKey)
                }
                keyView.setShiftState(currentShiftState)

                val keyParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    keyModel.weight
                ).apply {
                    marginEnd = (1.5f * density).toInt()
                    marginStart = (1.5f * density).toInt()
                }
                rowView.addView(keyView, keyParams)
            }
            container.addView(rowView)
        }
    }

    private fun handleKeyInteraction(key: KeyModel) {
        when (key.type) {
            KeyType.SHIFT -> {
                val now = System.currentTimeMillis()
                currentShiftState = when {
                    currentShiftState == ShiftState.CAPS_LOCKED -> ShiftState.OFF
                    now - lastShiftPressTime < 350 -> ShiftState.CAPS_LOCKED
                    currentShiftState == ShiftState.SHIFTED -> ShiftState.OFF
                    else -> ShiftState.SHIFTED
                }
                lastShiftPressTime = now
                updateKeyViewsShiftState()
            }
            KeyType.SYMBOLS_TOGGLE -> {
                currentMode = if (currentMode == KeyboardMode.LETTERS) {
                    KeyboardMode.SYMBOLS
                } else {
                    KeyboardMode.LETTERS
                }
                rebuildKeys()
            }
            KeyType.HIDE_KEYBOARD -> {
                onDismissKeyboard()
            }
            KeyType.SETTINGS -> {
                onOpenSettings()
            }
            KeyType.CHAR -> {
                val finalChar = if (currentShiftState != ShiftState.OFF) key.shifted else key.primary
                onKeyAction(key.copy(primary = finalChar))
                // If single shift, revert to lowercase
                if (currentShiftState == ShiftState.SHIFTED) {
                    currentShiftState = ShiftState.OFF
                    updateKeyViewsShiftState()
                }
            }
            else -> {
                onKeyAction(key)
            }
        }
    }

    private fun updateKeyViewsShiftState() {
        fun updateContainer(container: LinearLayout) {
            for (i in 0 until container.childCount) {
                val row = container.getChildAt(i) as? LinearLayout ?: continue
                for (j in 0 until row.childCount) {
                    val keyView = row.getChildAt(j) as? KeyView ?: continue
                    keyView.setShiftState(currentShiftState)
                }
            }
        }
        updateContainer(leftKeysContainer)
        updateContainer(rightKeysContainer)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Return false on the root layout so touches outside the left and right cards
        // fall through without being consumed!
        return false
    }
}
