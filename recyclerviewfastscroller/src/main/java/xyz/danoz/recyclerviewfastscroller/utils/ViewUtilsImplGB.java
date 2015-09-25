package xyz.danoz.recyclerviewfastscroller.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

class ViewUtilsImplGB extends ViewUtils.Impl {
    @Override
    public void setActivated(View view, boolean activated) {
        // no compatible operations
    }

    @Override
    public void setTranslationX(View view, float translationX) {
        final ViewGroup.MarginLayoutParams lp = getMarginLayoutParams(view);
        lp.leftMargin = (int) (translationX + 0.5f);
        view.setLayoutParams(lp);
    }

    @Override
    public float getTranslationX(View view) {
        final ViewGroup.MarginLayoutParams lp = getMarginLayoutParams(view);
        return lp.leftMargin;
    }

    @Override
    public void setTranslationY(View view, float translationY) {
        final ViewGroup.MarginLayoutParams lp = getMarginLayoutParams(view);
        lp.topMargin = (int) (translationY + 0.5f);
        view.setLayoutParams(lp);
    }

    @Override
    public float getTranslationY(View view) {
        final ViewGroup.MarginLayoutParams lp = getMarginLayoutParams(view);
        return lp.topMargin;
    }

    @Override
    public void addOnGlobalLayoutListener(ViewTreeObserver viewTreeObserver, ViewTreeObserver.OnGlobalLayoutListener listener) {
        viewTreeObserver.addOnGlobalLayoutListener(listener);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeOnGlobalLayoutListener(ViewTreeObserver viewTreeObserver, ViewTreeObserver.OnGlobalLayoutListener victim) {
        viewTreeObserver.removeGlobalOnLayoutListener(victim);
    }

    private static ViewGroup.MarginLayoutParams getMarginLayoutParams(View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return (ViewGroup.MarginLayoutParams) lp;
        } else {
            throw new IllegalStateException("Parent does not support MarginLayoutParams");
        }
    }
}
