package com.example.ime

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View

@SuppressLint("ViewConstructor")
class KeyView(
    context: Context,
    val keyModel: KeyModel,
    private val onKeyPressed: (KeyModel) -> Unit
) : View(context) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val altPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.DEFAULT
    }

    private val rectF = RectF()
    private var isShifted: Boolean = false
    private var isCapsLocked: Boolean = false
    private var isTouching = false

    private val repeatHandler = Handler(Looper.getMainLooper())
    private var isRepeating = false

    private val repeatRunnable = object : Runnable {
        override fun run() {
            if (isTouching && keyModel.type == KeyType.BACKSPACE) {
                isRepeating = true
                onKeyPressed(keyModel)
                repeatHandler.postDelayed(this, 50)
            }
        }
    }

    init {
        isClickable = true
        isFocusable = false
        contentDescription = when (keyModel.type) {
            KeyType.SHIFT -> "Shift"
            KeyType.BACKSPACE -> "Backspace"
            KeyType.ENTER -> "Enter"
            KeyType.SPACE -> "Space"
            KeyType.SYMBOLS_TOGGLE -> "Symbols Toggle"
            KeyType.HIDE_KEYBOARD -> "Hide Keyboard"
            KeyType.SETTINGS -> "Settings"
            KeyType.CHAR -> keyModel.primary
        }
    }

    fun setShiftState(shiftState: ShiftState) {
        val newShifted = shiftState != ShiftState.OFF
        val newCaps = shiftState == ShiftState.CAPS_LOCKED
        if (newShifted != isShifted || newCaps != isCapsLocked) {
            isShifted = newShifted
            isCapsLocked = newCaps
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minH = (44 * resources.displayMetrics.density).toInt()
        val h = resolveSize(minH, heightMeasureSpec)
        val w = resolveSize((40 * resources.displayMetrics.density).toInt(), widthMeasureSpec)
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val padding = 3f * density
        val cornerRadius = 9f * density

        rectF.set(
            padding,
            padding,
            width.toFloat() - padding,
            height.toFloat() - padding
        )

        // Colors
        val isAccentKey = when (keyModel.type) {
            KeyType.ENTER -> true
            KeyType.SHIFT -> isShifted
            KeyType.BACKSPACE -> true
            KeyType.HIDE_KEYBOARD -> true
            KeyType.SYMBOLS_TOGGLE -> true
            else -> false
        }

        val baseColor = when {
            keyModel.type == KeyType.ENTER -> Color.parseColor("#0284C7") // Sky blue
            keyModel.type == KeyType.SHIFT && isCapsLocked -> Color.parseColor("#38BDF8") // Glowing active
            keyModel.type == KeyType.SHIFT && isShifted -> Color.parseColor("#0369A1") // Shifted
            isAccentKey -> Color.parseColor("#1E293B") // Dark Slate
            keyModel.type == KeyType.SPACE -> Color.parseColor("#1E293B")
            else -> Color.parseColor("#1E293B") // Standard key dark slate
        }

        val activeColor = if (isTouching) {
            if (isAccentKey) Color.parseColor("#38BDF8") else Color.parseColor("#334155")
        } else {
            baseColor
        }

        bgPaint.color = activeColor
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, bgPaint)

        // Subtle key border
        borderPaint.color = if (isTouching) Color.parseColor("#7DD3FC") else Color.parseColor("#334155")
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint)

        // Text
        val displayChar = when {
            keyModel.type == KeyType.CHAR && isShifted -> keyModel.shifted
            keyModel.type == KeyType.CHAR -> keyModel.primary
            keyModel.type == KeyType.SHIFT && isCapsLocked -> "⇪"
            keyModel.type == KeyType.SHIFT -> "⇧"
            else -> keyModel.primary
        }

        val textColor = when {
            keyModel.type == KeyType.ENTER -> Color.WHITE
            keyModel.type == KeyType.SHIFT && (isShifted || isCapsLocked) -> Color.WHITE
            else -> Color.parseColor("#F1F5F9")
        }

        textPaint.color = textColor
        textPaint.textSize = when {
            keyModel.type == KeyType.SPACE -> 13f * density
            keyModel.type == KeyType.SYMBOLS_TOGGLE -> 12f * density
            keyModel.type in listOf(KeyType.SHIFT, KeyType.ENTER, KeyType.BACKSPACE, KeyType.HIDE_KEYBOARD) -> 17f * density
            displayChar.length > 2 -> 12f * density
            else -> 17f * density
        }

        val textY = (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(displayChar, width / 2f, textY, textPaint)

        // Alt Hint (e.g. number or secondary symbol in corner)
        keyModel.altHint?.let { hint ->
            altPaint.color = Color.parseColor("#64748B")
            altPaint.textSize = 9f * density
            canvas.drawText(
                hint,
                width.toFloat() - (padding + 5f * density),
                padding + 10f * density,
                altPaint
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isTouching = true
                invalidate()
                if (keyModel.type == KeyType.BACKSPACE) {
                    repeatHandler.postDelayed(repeatRunnable, 350)
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isTouching) {
                    repeatHandler.removeCallbacks(repeatRunnable)
                    if (!isRepeating) {
                        onKeyPressed(keyModel)
                    }
                    isRepeating = false
                    isTouching = false
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                repeatHandler.removeCallbacks(repeatRunnable)
                isRepeating = false
                isTouching = false
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
