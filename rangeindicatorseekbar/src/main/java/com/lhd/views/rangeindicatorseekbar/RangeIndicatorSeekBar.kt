package com.lhd.views.rangeindicatorseekbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class RangeIndicatorSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val THUMB_INDEX_LEFT = 0
        const val THUMB_INDEX_RIGHT = 1
        const val THUMB_INDEX_NONE = -1

        const val DEF_BAR_BACKGROUND_COLOR = Color.GRAY
        const val DEF_BAR_SELECTED_COLOR = Color.CYAN
        const val DEF_THUMB_COLOR = Color.YELLOW
        const val DEF_THUMB_RIPPLE_COLOR = Color.YELLOW
        const val DEF_THUMB_RIPPLE_ALPHA = 0.5f
        const val DEF_THUMB_SIZE = 20f
        const val DEF_THUMB_RIPPLE_SIZE = 40f
        const val DEF_BAR_HEIGHT = 40f
    }

    private val rectView = RectF()
    private val rectBar = RectF()
    private val rectBarSelectedLeft = RectF()
    private val rectBarSelectedRight = RectF()
    private val rectThumbLeftRipple = RectF()
    private val rectThumbLeft = RectF()
    private val rectThumbRightRipple = RectF()
    private val rectThumbRight = RectF()
    private val rectText = Rect()

    private var barHeight = 0f
    private var barCorners = 0f
    private var thumbSize = 0f
    private var thumbRippleSize = 0f
    private var touchExpandSize = 0f
    private var textIndicatorBottom = 0f

    private val paintBar = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintSelectedBar = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintThumb = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintThumbRipple = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintTextIndicator = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var min = 0f
    private var max = 0f
    private var minProgress = 0f
    private var maxProgress = 0f
    private var progressVisibleAsInt = false
    var currentThumbIndex = THUMB_INDEX_NONE
        private set

    var isMovingThumb = false
        private set
    private val touchPointF = PointF()
    private val touchSlop by lazy {
        ViewConfiguration.get(context).scaledTouchSlop
    }

    init {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.RangeIndicatorSeekBar)
            thumbSize =
                ta.getDimension(R.styleable.RangeIndicatorSeekBar_thumb_size, DEF_THUMB_SIZE)
            thumbRippleSize = ta.getDimension(
                R.styleable.RangeIndicatorSeekBar_thumb_ripple_size,
                DEF_THUMB_RIPPLE_SIZE
            )
            paintThumb.color = ta.getColor(
                R.styleable.RangeIndicatorSeekBar_thumb_color,
                DEF_THUMB_COLOR
            )
            paintThumbRipple.color = ta.getColor(
                R.styleable.RangeIndicatorSeekBar_thumb_ripple_color,
                DEF_THUMB_RIPPLE_COLOR
            )
            touchExpandSize =
                ta.getDimension(R.styleable.RangeIndicatorSeekBar_thumb_expand_touch_size, 0f)

            barHeight =
                ta.getDimension(R.styleable.RangeIndicatorSeekBar_bar_height, DEF_THUMB_RIPPLE_SIZE)
            barCorners = ta.getDimension(R.styleable.RangeIndicatorSeekBar_bar_corner, 0f)
            paintBar.color = ta.getColor(
                R.styleable.RangeIndicatorSeekBar_bar_color_background,
                DEF_BAR_BACKGROUND_COLOR
            )
            paintSelectedBar.color = ta.getColor(
                R.styleable.RangeIndicatorSeekBar_bar_color_background,
                DEF_BAR_SELECTED_COLOR
            )

            paintTextIndicator.textSize =
                ta.getDimension(R.styleable.RangeIndicatorSeekBar_text_indicator_size, 0f)
            paintTextIndicator.color = ta.getColor(
                R.styleable.RangeIndicatorSeekBar_text_indicator_color,
                paintThumb.color
            )
            textIndicatorBottom =
                ta.getDimension(R.styleable.RangeIndicatorSeekBar_text_indicator_bottom, 0f)

            min = ta.getInt(R.styleable.RangeIndicatorSeekBar_min, 0).toFloat()
            max = ta.getInt(R.styleable.RangeIndicatorSeekBar_max, 100).toFloat()
            minProgress =
                ta.getInt(R.styleable.RangeIndicatorSeekBar_min_progress, min.toInt()).toFloat()
            maxProgress =
                ta.getInt(R.styleable.RangeIndicatorSeekBar_max_progress, max.toInt()).toFloat()
            progressVisibleAsInt =
                ta.getBoolean(R.styleable.RangeIndicatorSeekBar_progress_visible_as_int, false)

            ta.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val measureWidth = measureDimension(widthSize, widthMeasureSpec)
        var minHeight = max(max(thumbSize, barHeight), thumbRippleSize).toInt()
        if (paintTextIndicator.textSize > 0) {
            paintTextIndicator.getTextBounds("1", 0, 1, rectText)
            minHeight += textIndicatorBottom.toInt() + rectText.height()
        }
        val measureHeight = measureDimension(
            minHeight,
            heightMeasureSpec
        )
        setMeasuredDimension(measureWidth, measureHeight)
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == MeasureSpec.AT_MOST) {
                result = min(result, specSize)
            }
        }
        if (result < desiredSize) {
            //eLog("The view is too small, the content might get cut")
        }
        return result
    }

    /**
     * Configure View
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val left = getSpaceLeft()
        val right = w - getSpaceRight()
        eLog("<================ SizeChange>")
        eLog("ViewSize: ($w,$h)")
        rectView.set(left, paddingTop, right, h - paddingBottom)
        eLog("RectView: $rectView, center: ${rectView.centerY()}")
        rectBar.set(
            rectView.left + thumbSize / 2f,
            rectView.centerY() - barHeight / 2f,
            rectView.right - thumbSize / 2f,
            rectView.centerY() + barHeight / 2f
        )
        if (paintTextIndicator.textSize > 0) {
            rectBar.bottom = rectView.bottom - (max(
                max(thumbSize, barHeight),
                thumbRippleSize
            ).toInt() - barHeight) / 2f
            rectBar.top = rectBar.bottom - barHeight
        }
        eLog("RectBar: $rectBar, center: ${rectBar.centerY()}")
        syncThumbWithProgress()
        syncThumbRippleWithThumb()
        syncSelectedBarWithThumb()
        eLog("<SizeChange=======================>")
    }

    private fun syncThumbWithProgress() {
        val minPos = minProgress.getPositionByProgress()
        val maxPos = maxProgress.getPositionByProgress()
        rectThumbLeft.setCenter(minPos, rectBar.centerY(), thumbSize, thumbSize)
        rectThumbRight.setCenter(maxPos, rectBar.centerY(), thumbSize, thumbSize)
    }

    private fun syncThumbRippleWithThumb() {
        rectThumbLeftRipple.setCenter(
            rectThumbLeft.centerX(),
            rectThumbLeft.centerY(),
            thumbRippleSize,
            thumbRippleSize
        )
        rectThumbRightRipple.setCenter(
            rectThumbRight.centerX(),
            rectThumbRight.centerY(),
            thumbRippleSize,
            thumbRippleSize
        )
    }

    private fun syncSelectedBarWithThumb() {
        rectBarSelectedLeft.set(rectBar.left, rectBar.top, rectThumbLeft.centerX(), rectBar.bottom)
        rectBarSelectedRight.set(
            rectThumbRight.centerX(),
            rectBar.top,
            rectBar.right,
            rectBar.bottom
        )
    }

    private fun syncTextIndicatorWithThumb(canvas: Canvas) {
        if (currentThumbIndex == THUMB_INDEX_NONE || !isMovingThumb)
            return
        val thumb = if (currentThumbIndex == THUMB_INDEX_RIGHT) rectThumbRight else rectThumbLeft
        val progress = if (currentThumbIndex == THUMB_INDEX_RIGHT) maxProgress else minProgress
        val textProgress =
            if (progressVisibleAsInt) progress.toInt().toString() else progress.toString()
        paintTextIndicator.getTextBounds(textProgress, 0, textProgress.length, rectText)
        var xText = thumb.centerX() - rectText.width() / 2
        if (xText < 0)
            xText = 0f
        if (xText > width - rectText.width())
            xText = (width - rectText.width()).toFloat()
        val yText = thumb.top - textIndicatorBottom - rectText.height()
        canvas.drawText(textProgress, xText, yText, paintTextIndicator)
    }

    /**
     * Get padding
     */
    private fun getSpaceLeft(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            paddingStart
        } else {
            paddingLeft
        }
    }

    private fun getSpaceRight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            paddingEnd
        } else {
            paddingRight
        }
    }
    ///////

    /**
     * Calculate view
     */

    private fun Number.getPositionByProgress(): Float {
        return ((this.toFloat() - min) / (max - min)) * rectBar.width() + rectView.left + thumbSize / 2f
    }

    private fun Float.getProgressByPosition(): Float {
        return (this - rectBar.left) / rectBar.width() * (max - min) + min
    }

    /**
     * Draw the view
     */
    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            drawView(canvas)
        }
    }

    private fun drawView(canvas: Canvas) {
        canvas.drawRoundRect(rectBar, barCorners, barCorners, paintBar)
        canvas.drawRoundRect(rectBarSelectedLeft, barCorners, barCorners, paintSelectedBar)
        canvas.drawRoundRect(rectBarSelectedRight, barCorners, barCorners, paintSelectedBar)
        canvas.drawOval(rectThumbLeftRipple, paintThumbRipple)
        canvas.drawOval(rectThumbRightRipple, paintThumbRipple)
        canvas.drawOval(rectThumbLeft, paintThumb)
        canvas.drawOval(rectThumbRight, paintThumb)
        syncTextIndicatorWithThumb(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    touchPointF.set(it.x, it.y)
                    currentThumbIndex = getThumbIndexWhenTouch(PointF(it.x, it.y))
                    eLog("TouchThumbIndex: $currentThumbIndex")
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (currentThumbIndex != THUMB_INDEX_NONE) {
                        val disMove = it.x - touchPointF.x
                        if (isMovingThumb) {
                            movingThumb(disMove.toInt())
                            touchPointF.set(it.x, it.y)
                            return true
                        } else {
                            if (disMove.abs() >= touchSlop) {
                                isMovingThumb = true
                                return true
                            }
                        }
                    }
                    return false
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    if (paintTextIndicator.textSize > 0)
                        invalidate()
                    currentThumbIndex = THUMB_INDEX_NONE
                    isMovingThumb = false
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun movingThumb(disMove: Int) {
        when (currentThumbIndex) {
            THUMB_INDEX_LEFT -> {
                doMoveThumb(rectThumbLeft, disMove, rectBar.left, rectThumbRight.centerX())
                //eLog("Thumb Left Moving: Range[${rectBar.left},${rectThumbRight.centerX()}]")
                minProgress = rectThumbLeft.centerX().getProgressByPosition()
                eLog("Thumb Left Moving - Value Range [$minProgress,$maxProgress]")
            }
            THUMB_INDEX_RIGHT -> {
                doMoveThumb(rectThumbRight, disMove, rectThumbLeft.centerX(), rectBar.right)
                //eLog("Thumb Right Moving: Range[${rectThumbLeft.centerX()},${rectBar.right}]")
                maxProgress = rectThumbRight.centerX().getProgressByPosition()
                eLog("Thumb Right Moving - Value Range [$minProgress,$maxProgress]")
            }
            else -> {
            }
        }
        invalidate()
    }

    private fun doMoveThumb(thumb: RectF, disMove: Int, minCenter: Float, maxCenter: Float) {
        var planCenter = thumb.centerX() + disMove
        if (thumb.centerX() + disMove < minCenter) {
            planCenter = minCenter
        } else if (thumb.centerX() + disMove > maxCenter) {
            planCenter = maxCenter
        }
        thumb.setCenterX(planCenter, thumbSize)
        syncThumbRippleWithThumb()
        syncSelectedBarWithThumb()
    }

    private fun getThumbIndexWhenTouch(pointTouch: PointF): Int {
        var resultThumbIndex = THUMB_INDEX_NONE
        var priority = THUMB_INDEX_LEFT
        val distanceTouchToThumbLeft = abs(pointTouch.x - rectThumbLeft.centerX())
        val distanceTouchToThumbRight = abs(pointTouch.x - rectThumbRight.centerX())
        if (pointTouch.x > rectThumbRight.centerX()
            && distanceTouchToThumbRight < distanceTouchToThumbLeft
        ) {
            priority = THUMB_INDEX_RIGHT
        }
        val rectLeftExpand = RectF(
            rectThumbLeft.left - touchExpandSize,
            rectView.top,
            rectThumbLeft.right + touchExpandSize,
            rectView.height()
        )
        val rectRightExpand = RectF(
            rectThumbRight.left - touchExpandSize,
            rectView.top,
            rectThumbRight.right + touchExpandSize,
            rectView.height()
        )
        if (rectLeftExpand.contains(pointTouch.x, pointTouch.y)) {
            resultThumbIndex = THUMB_INDEX_LEFT
        }
        if (rectRightExpand.contains(pointTouch.x, pointTouch.y)) {
            if (priority == THUMB_INDEX_RIGHT || resultThumbIndex == THUMB_INDEX_NONE)
                resultThumbIndex = THUMB_INDEX_RIGHT
        }
        return resultThumbIndex
    }
}