package it.cammino.risuscito.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class FABAwareScrollingViewBehavior extends AppBarLayout.ScrollingViewBehavior {
    public FABAwareScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FABAwareScrollingViewBehavior() {
        super();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return super.layoutDependsOn(parent, child, dependency) ||
                dependency instanceof FloatingActionButton;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull final CoordinatorLayout coordinatorLayout, @NonNull final View child,
                                       @NonNull final View directTargetChild, @NonNull final View target, final int nestedScrollAxes, int type) {
        // Ensure we react to vertical scrolling
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes, type);
    }

    @Override
    public void onNestedScroll(@NonNull final CoordinatorLayout coordinatorLayout, @NonNull final View child,
                               @NonNull final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        if (dyConsumed > 0) {
            // User scrolled down -> hide the FAB
            List<View> dependencies = coordinatorLayout.getDependencies(child);
            for (View view : dependencies) {
                if (view instanceof FloatingActionButton) {
                    //TEST per non dover togliere il behavior quando si nasconde il FAB volutamente
//                    ((FloatingActionButton) view).hide();
                    ((FloatingActionButton) view).hide(new FloatingActionButton.OnVisibilityChangedListener() {
                        @Override
                        public void onHidden(FloatingActionButton fab) {
                            super.onHidden(fab);
                            fab.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        } else if (dyConsumed < 0) {
            // User scrolled up -> show the FAB
            List<View> dependencies = coordinatorLayout.getDependencies(child);
            for (View view : dependencies) {
//                if (view instanceof FloatingActionButton) {
                //TEST per non dover togliere il behavior quando si nasconde il FAB volutamente
                if (view instanceof FloatingActionButton && view.getVisibility() != View.GONE) {
                    ((FloatingActionButton) view).show();
                }
            }
        }
    }
}
