package xyz.danoz.recyclerviewfastscroller.utils;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;

public class ViewUtils {
    static abstract class Impl {
        public abstract void setActivated(View view, boolean activated);
        public abstract void setTranslationX(View view, float translationX);
        public abstract float getTranslationX(View view);
        public abstract void setTranslationY(View view, float translationY);
        public abstract float getTranslationY(View view);

        public abstract void addOnGlobalLayoutListener(ViewTreeObserver viewTreeObserver, ViewTreeObserver.OnGlobalLayoutListener listener);
        public abstract void removeOnGlobalLayoutListener(ViewTreeObserver viewTreeObserver, ViewTreeObserver.OnGlobalLayoutListener victim);
    }

    static final Impl IMPL;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            IMPL = new ViewUtilsImplJB();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            IMPL = new ViewUtilsImplHoneycomb();
        } else {
            IMPL = new ViewUtilsImplGB();
        }
    }

    private ViewUtils() {
    }

    /**
     * Sets activated state for the view. This method does not perform any operations for API level 10 or earlier.
     * @param view target view
     * @param activated activated
     */
    public static void setActivated(View view, boolean activated) {
        IMPL.setActivated(view, activated);
    }

    /**
     * Compatibility version of the View.setTranslationX() method.
     * @param view target view
     * @param translationX x translation amount
     */
    public static void setTranslationX(View view, float translationX) {
        IMPL.setTranslationX(view, translationX);
    }

    /**
     * Compatibility version of the View.getTranslationX() method.
     * @param view target view
     * @return x translation amount
     */
    public static float getTranslationX(View view) {
        return IMPL.getTranslationX(view);
    }

    /**
     * Compatibility version of the View.setTranslationY() method.
     * @param view target view
     * @param translationY y translation amount
     */
    public static void setTranslationY(View view, float translationY) {
        IMPL.setTranslationY(view, translationY);
    }

    /**
     * Compatibility version of the View.getTranslationX() method.
     * @param view target view
     * @return y translation amount
     */
    public static float getTranslationY(View view) {
        return IMPL.getTranslationY(view);
    }

    /**
     * Compatibility version of the View.getViewTreeObserver().addOnGlobalLayoutListener() method.
     * @param view target view
     * @return listener global layout listener
     */
    public static void viewTreeObserverAddOnGlobalLayoutListener(
            View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();

        if (viewTreeObserver.isAlive()) {
            IMPL.addOnGlobalLayoutListener(viewTreeObserver, listener);
        }
    }

    /**
     * Compatibility version of the View.getViewTreeObserver().removeOnGlobalLayoutListener() method.
     * @param view target view
     * @return victim global layout listener
     */
    public static void viewTreeObserverRemoveOnGlobalLayoutListener(
            View view, ViewTreeObserver.OnGlobalLayoutListener victim) {
        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();

        if (viewTreeObserver.isAlive()) {
            IMPL.removeOnGlobalLayoutListener(viewTreeObserver, victim);
        }
    }
}
