package com.dertefter.ficus

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout

class CustomScrollingViewBehavior : AppBarLayout.Behavior {

    constructor() : super()
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    var shouldScroll = true

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return shouldScroll && when (target) {
            is NestedScrollView, is ScrollView, is RecyclerView -> {
                return target.canScrollVertically(1) || target.canScrollVertically(-1)
            }
            else -> super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
        }
    }
}