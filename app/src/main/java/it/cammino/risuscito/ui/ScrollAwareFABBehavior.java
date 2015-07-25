package it.cammino.risuscito.ui;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;

import com.github.alexkolpa.fabtoolbar.ViewGroupUtils;

import java.util.List;

/**
 * Created by marcello.battain on 20/07/2015.
 */
public class ScrollAwareFABBehavior extends android.support.design.widget.CoordinatorLayout.Behavior<FloatingActionButton> {
    private static final boolean SNACKBAR_BEHAVIOR_ENABLED;
    private Rect mTmpRect;
    private float mTranslationY;

    public ScrollAwareFABBehavior(Context context, AttributeSet set) {
        super();
    }

    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        if(dependency instanceof Snackbar.SnackbarLayout) {
            this.updateFabTranslationForSnackbar(parent, child, dependency);
        } else if(dependency instanceof AppBarLayout) {
            this.updateFabVisibility(parent, (AppBarLayout)dependency, child);
        }

        return false;
    }

    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return (SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout)
                || dependency instanceof AppBarLayout;
    }

    public void onDependentViewRemoved(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        if(dependency instanceof Snackbar.SnackbarLayout) {
            ViewCompat.animate(child).translationY(0.0F).setInterpolator(new FastOutSlowInInterpolator()).setListener((ViewPropertyAnimatorListener)null);
        }
    }

    private boolean updateFabVisibility(CoordinatorLayout parent, AppBarLayout appBarLayout, FloatingActionButton child) {
//            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
//            if(lp.getAnchorId() != appBarLayout.getId()) {
//                return false;
//            } else {
//                if(this.mTmpRect == null) {
//                    this.mTmpRect = new Rect();
//                }
//
//                Rect rect = this.mTmpRect;
//                ViewGroupUtils.getDescendantRect(parent, appBarLayout, rect);
//                if(rect.bottom <= appBarLayout.getMinimumHeightForVisibleOverlappingContent()) {
//                    child.hide();
//                } else {
//                    child.show();
//                }
//
//                return true;
//            }
        if(this.mTmpRect == null) {
            this.mTmpRect = new Rect();
        }
        int rect_bottom = this.mTmpRect.bottom;
//                Log.i(getClass().toString(), "this.mTmpRect prima: " + this.mTmpRect.bottom);
//                Log.i(getClass().toString(), "rect.bottom prima: " + rect_bottom);
        ViewGroupUtils.getDescendantRect(parent, appBarLayout, this.mTmpRect);
//                Log.i(getClass().toString(), "this.mTmpRect dopo: " + this.mTmpRect.bottom);
//                Log.i(getClass().toString(), "rect.bottom dopo: " + rect_bottom);
        if(rect_bottom > mTmpRect.bottom)
            child.hide();
        else
            child.show();

        return false;
    }

    private void updateFabTranslationForSnackbar(CoordinatorLayout parent, FloatingActionButton fab, View snackbar) {
//            if(fab.getVisibility() == 0) {
        if(fab.getVisibility() == View.VISIBLE) {
            float translationY = this.getFabTranslationYForSnackbar(parent, fab);
            if(translationY != this.mTranslationY) {
                ViewCompat.animate(fab).cancel();
                ViewCompat.setTranslationY(fab, translationY);
                this.mTranslationY = translationY;
            }

        }
    }

    private float getFabTranslationYForSnackbar(CoordinatorLayout parent, FloatingActionButton fab) {
        float minOffset = 0.0F;
        List dependencies = parent.getDependencies(fab);
        int i = 0;

        for(int z = dependencies.size(); i < z; ++i) {
            View view = (View)dependencies.get(i);
            if(view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(fab, view)) {
                minOffset = Math.min(minOffset, ViewCompat.getTranslationY(view) - (float)view.getHeight());
            }
        }

        return minOffset;
    }

//        public boolean onLayoutChild(CoordinatorLayout parent, FabToolbar child, int layoutDirection) {
//            List dependencies = parent.getDependencies(child);
//            int i = 0;
//
//            for(int count = dependencies.size(); i < count; ++i) {
//                View dependency = (View)dependencies.get(i);
//                if(dependency instanceof AppBarLayout && this.updateFabVisibility(parent, (AppBarLayout)dependency, child)) {
//                    break;
//                }
//            }
//
//            parent.onLayoutChild(child, layoutDirection);
////            this.offsetIfNeeded(parent, child);
//            return true;
//        }

//        private void offsetIfNeeded(CoordinatorLayout parent, FloatingActionButton fab) {
//            Rect padding = fab.mShadowPadding;
//            if(padding != null && padding.centerX() > 0 && padding.centerY() > 0) {
//                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)fab.getLayoutParams();
//                int offsetTB = 0;
//                int offsetLR = 0;
//                if(fab.getRight() >= parent.getWidth() - lp.rightMargin) {
//                    offsetLR = padding.right;
//                } else if(fab.getLeft() <= lp.leftMargin) {
//                    offsetLR = -padding.left;
//                }
//
//                if(fab.getBottom() >= parent.getBottom() - lp.bottomMargin) {
//                    offsetTB = padding.bottom;
//                } else if(fab.getTop() <= lp.topMargin) {
//                    offsetTB = -padding.top;
//                }
//
//                fab.offsetTopAndBottom(offsetTB);
//                fab.offsetLeftAndRight(offsetLR);
//            }
//
//        }

    static {
        SNACKBAR_BEHAVIOR_ENABLED = Build.VERSION.SDK_INT >= 11;
    }
}

