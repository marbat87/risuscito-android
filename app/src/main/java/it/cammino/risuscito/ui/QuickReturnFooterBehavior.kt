package it.cammino.risuscito.ui


import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View

@Suppress("unused")
/**
 * CoordinatorLayout Behavior for a quick return footer
 *
 * When a nested ScrollView is scrolled down, the quick return view will disappear.
 * When the ScrollView is scrolled back up, the quick return view will reappear.
 *
 * @author bherbst
 */
class QuickReturnFooterBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<View>(context, attrs) {

    private var mDySinceDirectionChange: Int = 0
    private var mIsShowing: Boolean = false
    private var mIsHiding: Boolean = false

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
        return nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (dy > 0 && mDySinceDirectionChange < 0 || dy < 0 && mDySinceDirectionChange > 0) {
            // We detected a direction change- cancel existing animations and reset our cumulative delta Y
            ViewCompat.animate(child).cancel()
            mDySinceDirectionChange = 0
        }

        mDySinceDirectionChange += dy

        if (mDySinceDirectionChange > child.height
                && child.visibility == View.VISIBLE
                && !mIsHiding) {
            hide(child)
        } else if (mDySinceDirectionChange < 0
                && child.visibility == View.INVISIBLE
                && !mIsShowing) {
            show(child)
        }
    }

    /**
     * Hide the quick return view.
     *
     * Animates hiding the view, with the view sliding down and out of the screen.
     * After the view has disappeared, its visibility will change to GONE.
     *
     * @param view The quick return view
     */
    private fun hide(view: View) {
        mIsHiding = true
        val animator = ViewCompat.animate(view)
                .translationY(view.height.toFloat())
                .setInterpolator(INTERPOLATOR)
                .setDuration(200)

        animator.setListener(object : ViewPropertyAnimatorListener {
            override fun onAnimationStart(view: View) {}

            override fun onAnimationEnd(view: View) {
                // Prevent drawing the View after it is gone
                mIsHiding = false
                view.visibility = View.INVISIBLE
            }

            override fun onAnimationCancel(view: View) {
                // Canceling a hide should show the view
                mIsHiding = false
                if (!mIsShowing)
                    show(view)
            }
        })

        animator.start()
    }

    /**
     * Show the quick return view.
     *
     * Animates showing the view, with the view sliding up from the bottom of the screen.
     * After the view has reappeared, its visibility will change to VISIBLE.
     *
     * @param view The quick return view
     */
    private fun show(view: View) {
        mIsShowing = true
        val animator = ViewCompat.animate(view)
                .translationY(0f)
                .setInterpolator(INTERPOLATOR)
                .setDuration(200)

        animator.setListener(object : ViewPropertyAnimatorListener {
            override fun onAnimationStart(view: View) {
                view.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(view: View) {
                mIsShowing = false
            }

            override fun onAnimationCancel(view: View) {
                // Canceling a show should hide the view
                mIsShowing = false
                if (!mIsHiding)
                    hide(view)
            }
        })

        animator.start()
    }

    companion object {
        private val INTERPOLATOR = FastOutSlowInInterpolator()
    }
}
