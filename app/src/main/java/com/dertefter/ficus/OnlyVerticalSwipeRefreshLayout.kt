package com.dertefter.ficus

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.color.MaterialColors


class OnlyVerticalSwipeRefreshLayout(context: Context?, attrs: AttributeSet?) :
    SwipeRefreshLayout(context!!, attrs) {
    private val touchSlop: Int
    private var prevX = 0f
    private var declined = false

    init {
        touchSlop = ViewConfiguration.get(context!!).scaledTouchSlop
        val color = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceVariant, Color.WHITE)
        val color2 = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.BLACK)
        this.setColorSchemeColors(color2)
        this.setProgressBackgroundColorSchemeColor(color)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                prevX = MotionEvent.obtain(event).x
                declined = false // New action
            }

            MotionEvent.ACTION_MOVE -> {
                val eventX = event.x
                val xDiff = Math.abs(eventX - prevX)
                if (declined || xDiff > touchSlop) {
                    declined = true // Memorize
                    return false
                }
            }
        }
        return super.onInterceptTouchEvent(event)
    }
}