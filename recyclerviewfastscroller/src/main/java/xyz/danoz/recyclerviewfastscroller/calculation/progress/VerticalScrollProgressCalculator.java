package xyz.danoz.recyclerviewfastscroller.calculation.progress;

import android.view.MotionEvent;

import xyz.danoz.recyclerviewfastscroller.calculation.VerticalScrollBoundsProvider;

/**
 * Basic scroll progress calculator used to calculate vertical scroll progress from a touch event
 */
public abstract class VerticalScrollProgressCalculator implements TouchableScrollProgressCalculator {

    private final VerticalScrollBoundsProvider mScrollBoundsProvider;

    public VerticalScrollProgressCalculator(VerticalScrollBoundsProvider scrollBoundsProvider) {
        mScrollBoundsProvider = scrollBoundsProvider;
    }

    @Override
    public float calculateScrollProgress(MotionEvent event) {
        final float y = event.getY();
        final float min = mScrollBoundsProvider.getMinimumScrollY();
        final float max = mScrollBoundsProvider.getMaximumScrollY();

        if (y <= min) {
            return 0;
        } else if (y >= max) {
            return 1;
        } else {
            return (y - min) / (max - min);
        }
    }
}
