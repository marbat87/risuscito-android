package it.cammino.risuscito.ui

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.view.ViewCompat
import android.util.AttributeSet
import android.view.View

@Suppress("unused")
class FABAwareScrollingViewBehavior : AppBarLayout.ScrollingViewBehavior {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor() : super()

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return super.layoutDependsOn(parent, child, dependency) || dependency is FloatingActionButton
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View,
                                     directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
        // Ensure we react to vertical scrolling
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes, type)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: View,
                                target: View, dxConsumed: Int, dyConsumed: Int,
                                dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
        if (dyConsumed > 0) {
            // User scrolled down -> hide the FAB
            val dependencies = coordinatorLayout.getDependencies(child)
            for (view in dependencies) {
                (view as? FloatingActionButton)?.hide(object : FloatingActionButton.OnVisibilityChangedListener() {
                    @SuppressLint("RestrictedApi")
                    override fun onHidden(fab: FloatingActionButton?) {
                        super.onHidden(fab)
                        fab!!.visibility = View.INVISIBLE
                    }
                })
            }
        } else if (dyConsumed < 0) {
            // User scrolled up -> show the FAB
            val dependencies = coordinatorLayout.getDependencies(child)
            dependencies
                    .filterIsInstance<//TEST per non dover togliere il behavior quando si nasconde il FAB volutamente
                            FloatingActionButton>()
                    .filter { it.visibility != View.GONE }
                    .forEach { it.show() }
        }
    }
}
