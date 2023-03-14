package it.cammino.risuscito.utils.extension

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import it.cammino.risuscito.R

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

fun View.createSelectedList(selectedColorAttr: Int): ColorStateList {
    return ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf()), intArrayOf(
            MaterialColors.getColor(
                this,
                selectedColorAttr
            ),
            FastAdapterUIUtils.getSelectableBackground(this.context)
        )
    )
}

fun View.setSelectableRippleBackground(selectedColorResId: Int) {
    val shapeAppearanceModel = ShapeAppearanceModel()
        .toBuilder()
        .setBottomLeftCorner(
            CornerFamily.ROUNDED,
            resources.getDimension(R.dimen.listItemBackgroundCornerSize)
        )
        .setTopLeftCorner(
            CornerFamily.ROUNDED,
            resources.getDimension(R.dimen.listItemBackgroundCornerSize)
        )
        .build()

    val backgroundDrawable = MaterialShapeDrawable()
    val shapeMask = MaterialShapeDrawable()
    backgroundDrawable.shapeAppearanceModel = shapeAppearanceModel
    shapeMask.shapeAppearanceModel = shapeAppearanceModel

    backgroundDrawable.fillColor = this.createSelectedList(
        selectedColorResId
    )

    val rippleColor = ContextCompat.getColorStateList(
        this.context,
        R.color.material_ripple
    )

    rippleColor?.let {
        // Now we created a mask Drawable which will be used for touch feedback.
        val touchFeedbackShape = RippleDrawable(it, backgroundDrawable, shapeMask)
        ViewCompat.setBackground(this, touchFeedbackShape)
    }
}