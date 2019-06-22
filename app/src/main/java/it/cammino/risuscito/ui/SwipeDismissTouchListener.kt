package it.cammino.risuscito.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.ListView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * A [View.OnTouchListener] that makes any [View] dismissable when the
 * user swipes (drags her finger) horizontally across the view.
 *
 *
 * *For [ListView] list items that don't manage their own touch events
 * (i.e. you're using
 * [ListView.setOnItemClickListener]
 *
 * *
 * Example usage:
 *
 * <pre>
 * view.setOnTouchListener(new SwipeDismissTouchListener(
 * view,
 * null, // Optional token/cookie object
 * new SwipeDismissTouchListener.OnDismissCallback() {
 * public void onDismiss(View view, Object token) {
 * parent.removeView(view);
 * }
 * }));
</pre> *
 *
 *
 * This class Requires API level 12 or later due to use of [ ].
 *
 */
class SwipeDismissTouchListener
/**
 * Constructs a new swipe-to-dismiss touch listener for the given view.
 *
 * @param mView     The view to make dismissable.
 * @param mToken    An optional token/cookie object to be passed through to the callback.
 * @param mCallbacks The callback to trigger when the user has indicated that she would like to
 * dismiss this view.
 */
(// Fixed properties
        private val mView: View, private val mToken: Any?, private val mCallbacks: DismissCallbacks) : View.OnTouchListener {
    // Cached ViewConfiguration and system-wide constant values
    private val mSlop: Int
    private val mMinFlingVelocity: Int
    private val mMaxFlingVelocity: Int
    private val mAnimationTime: Long
    private var mViewWidth = 1 // 1 and not 0 to prevent dividing by zero

    // Transient properties
    private var mDownX: Float = 0.toFloat()
    private var mDownY: Float = 0.toFloat()
    private var mSwiping: Boolean = false
    private var mSwipingSlop: Int = 0
    private var mVelocityTracker: VelocityTracker? = null
    private var mTranslationX: Float = 0.toFloat()

    /**
     * The callback interface used by [SwipeDismissTouchListener] to inform its client
     * about a successful dismissal of the view for which it was created.
     */
    interface DismissCallbacks {
        /**
         * Called to determine whether the view can be dismissed.
         */
        fun canDismiss(token: Any?): Boolean

        /**
         * Called when the user has indicated they she would like to dismiss the view.
         *
         * @param view  The originating [View] to be dismissed.
         * @param token The optional token passed to this object's constructor.
         */
        fun onDismiss(view: View, token: Any?)
    }

    init {
        val vc = ViewConfiguration.get(mView.context)
        mSlop = vc.scaledTouchSlop
        mMinFlingVelocity = vc.scaledMinimumFlingVelocity * 16
        mMaxFlingVelocity = vc.scaledMaximumFlingVelocity
        mAnimationTime = mView.context.resources.getInteger(
                android.R.integer.config_shortAnimTime).toLong()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        // offset because the view is translated during swipe
        motionEvent.offsetLocation(mTranslationX, 0f)

        if (mViewWidth < 2) {
            mViewWidth = mView.width
        }

        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // TODO: ensure this is a finger, and set a flag
                mDownX = motionEvent.rawX
                mDownY = motionEvent.rawY
                return if (mCallbacks.canDismiss(mToken)) {
                    mVelocityTracker = VelocityTracker.obtain()
                    mVelocityTracker?.addMovement(motionEvent)
                    true
                } else
                    false
            }

            MotionEvent.ACTION_UP -> {
                if (mVelocityTracker == null) {
                    return false
                }

                val deltaX = motionEvent.rawX - mDownX
                mVelocityTracker?.addMovement(motionEvent)
                mVelocityTracker?.computeCurrentVelocity(1000)
                val velocityX = mVelocityTracker?.xVelocity ?: 0f
                val absVelocityX = abs(velocityX)
                val absVelocityY = abs(mVelocityTracker?.yVelocity ?: 0f)
                var dismiss = false
                var dismissRight = false
                if (abs(deltaX) > mViewWidth / 2 && mSwiping) {
                    dismiss = true
                    dismissRight = deltaX > 0
                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
                        && absVelocityY < absVelocityX
                        && absVelocityY < absVelocityX && mSwiping) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = velocityX < 0 == deltaX < 0
                    dismissRight = (mVelocityTracker?.xVelocity ?: 0f) > 0
                }
                if (dismiss) {
                    // dismiss
                    mView.animate()
                            .translationX((if (dismissRight) mViewWidth else -mViewWidth).toFloat())
                            .alpha(0f)
                            .setDuration(mAnimationTime)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    performDismiss()
                                }
                            })
                } else if (mSwiping) {
                    // cancel
                    mView.animate()
                            .translationX(0f)
                            .alpha(1f)
                            .setDuration(mAnimationTime)
                            .setListener(null)
                }
                mVelocityTracker?.recycle()
                mVelocityTracker = null
                mTranslationX = 0f
                mDownX = 0f
                mDownY = 0f
                mSwiping = false
            }

            MotionEvent.ACTION_CANCEL -> {
                if (mVelocityTracker == null) {
                    return false
                }

                mView.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(mAnimationTime)
                        .setListener(null)
                mVelocityTracker?.recycle()
                mVelocityTracker = null
                mTranslationX = 0f
                mDownX = 0f
                mDownY = 0f
                mSwiping = false
            }

            MotionEvent.ACTION_MOVE -> {
                if (mVelocityTracker == null) {
                    return false
                }

                mVelocityTracker?.addMovement(motionEvent)
                val deltaX = motionEvent.rawX - mDownX
                val deltaY = motionEvent.rawY - mDownY
                if (abs(deltaX) > mSlop && abs(deltaY) < abs(deltaX) / 2) {
                    mSwiping = true
                    mSwipingSlop = if (deltaX > 0) mSlop else -mSlop
                    mView.parent.requestDisallowInterceptTouchEvent(true)

                    // Cancel listview's touch
                    val cancelEvent = MotionEvent.obtain(motionEvent)
                    cancelEvent.action = MotionEvent.ACTION_CANCEL or (motionEvent.actionIndex shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                    mView.onTouchEvent(cancelEvent)
                    cancelEvent.recycle()
                }

                if (mSwiping) {
                    mTranslationX = deltaX
                    mView.translationX = deltaX - mSwipingSlop
                    // TODO: use an ease-out interpolator or such
                    mView.alpha = max(0f, min(1f, 1f - 2f * abs(deltaX) / mViewWidth))
                    return true
                }
            }
        }
        return false
    }

    private fun performDismiss() {
        // Animate the dismissed view to zero-height and then fire the dismiss callback.
        // This triggers layout on each animation frame; in the future we may want to do something
        // smarter and more performant.

        val lp = mView.layoutParams
        val originalHeight = mView.height

        val animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime)

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mCallbacks.onDismiss(mView, mToken)
                // Reset view presentation
                mView.alpha = 1f
                mView.translationX = 0f
                lp.height = originalHeight
                mView.layoutParams = lp
            }
        })

        animator.addUpdateListener { valueAnimator ->
            lp.height = valueAnimator.animatedValue as? Int ?: 0
            mView.layoutParams = lp
        }

        animator.start()
    }
}