/*
 * Copyright (c) 2026 Auxio Project
 * SleepTimerRingView.kt is part of Auxio.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.oxycblt.auxio.playback.timer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.R as AR
import com.google.android.material.R as MR
import com.google.android.material.color.MaterialColors
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import org.oxycblt.auxio.R
import org.oxycblt.auxio.util.getDimenPixels

/**
 * A radial one-hour dial for picking a sleep timer duration. Dragging (or tapping) along the ring
 * selects a duration between 1 and 60 minutes, similar to setting an analog kitchen timer.
 *
 * Durations above an hour cannot be selected here; the ring simply renders as full when such a
 * duration is set externally.
 *
 * @author Alexander Capehart (OxygenCobalt)
 */
class SleepTimerRingView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {
    private val trackPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = context.getDimenPixels(R.dimen.spacing_small).toFloat()
            color = MaterialColors.getColor(this@SleepTimerRingView, MR.attr.colorSurfaceContainerHighest)
        }
    private val progressPaint =
        Paint(trackPaint).apply {
            color = MaterialColors.getColor(this@SleepTimerRingView, AR.attr.colorPrimary)
        }
    private val thumbPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = MaterialColors.getColor(this@SleepTimerRingView, AR.attr.colorPrimary)
        }
    private val thumbRadius = context.getDimenPixels(R.dimen.spacing_mid_medium).toFloat()
    private val arcBounds = RectF()

    /**
     * The currently selected duration, in minutes. Values above [MAX_MINUTES] are kept as-is but
     * render as a full ring.
     */
    var minutes: Int = MAX_MINUTES / 2
        set(value) {
            field = value.coerceAtLeast(1)
            invalidate()
        }

    /** Called whenever the *user* changes [minutes] by interacting with the ring. */
    var onMinutesChanged: ((Int) -> Unit)? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Always square, sized by the smaller available dimension.
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = min(measuredWidth, measuredHeight)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) - thumbRadius
        arcBounds.set(cx - radius, cy - radius, cx + radius, cy + radius)

        canvas.drawCircle(cx, cy, radius, trackPaint)

        val sweep = (minutes.coerceAtMost(MAX_MINUTES) / MAX_MINUTES.toFloat()) * 360f
        canvas.drawArc(arcBounds, -90f, sweep, false, progressPaint)

        val thumbAngle = Math.toRadians((sweep - 90f).toDouble())
        canvas.drawCircle(
            cx + radius * cos(thumbAngle).toFloat(),
            cy + radius * sin(thumbAngle).toFloat(),
            thumbRadius,
            thumbPaint,
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                // Don't let ancestors (ex. the dialog's scroll view or the bottom sheet)
                // hijack the drag gesture.
                parent?.requestDisallowInterceptTouchEvent(true)
                updateFromTouch(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateFromTouch(x: Float, y: Float) {
        val angle = Math.toDegrees(atan2(y - height / 2.0, x - width / 2.0)) + 90.0
        val degrees = (angle + 360.0) % 360.0
        // 6 degrees per minute. Both 0 and 60 minutes land on the top of the ring, and a
        // timer of 0 minutes makes no sense, so resolve that position to a full hour.
        var newMinutes = (degrees / 6.0).roundToInt()
        if (newMinutes == 0) {
            newMinutes = MAX_MINUTES
        }
        if (newMinutes != minutes) {
            minutes = newMinutes
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            onMinutesChanged?.invoke(newMinutes)
        }
    }

    companion object {
        /** The largest duration selectable with the ring, in minutes. */
        const val MAX_MINUTES = 60
    }
}
