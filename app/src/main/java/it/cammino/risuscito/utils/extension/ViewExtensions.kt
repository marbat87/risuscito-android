package it.cammino.risuscito.utils.extension

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.ColorStateList
import android.view.View
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.material.color.MaterialColors

// Same animation that FloatingActionButton.Behavior uses to show the FAB when the AppBarLayout
// enters
fun View.animateIn() {
    isVisible = true
    animate().translationY(0f)
        .setInterpolator(LinearOutSlowInInterpolator()).setDuration(225L)
        .setListener(null).start()
}

internal fun View.animateOut() {
    animate().translationY(height.toFloat())
        .setInterpolator(FastOutLinearInInterpolator()).setDuration(175L)
        .setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isVisible = false
                }
            }
        ).start()
}

fun View.createCheckedList(normalColorAttr: Int, checkedColorAttr: Int): ColorStateList {
    return ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(
            MaterialColors.getColor(
                this,
                checkedColorAttr
            ),
            MaterialColors.getColor(this, normalColorAttr)
        )
    )
}