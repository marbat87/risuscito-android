package xyz.danoz.recyclerviewfastscroller.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class ViewUtilsImplJB extends ViewUtilsImplHoneycomb {
    @Override
    public void setActivated(View view, boolean activated) {
        view.setActivated(activated);
    }

    @Override
    public void setTranslationX(View view, float translationX) {
        view.setTranslationX(translationX);
    }

    @Override
    public float getTranslationX(View view) {
        return view.getTranslationX();
    }

    @Override
    public void setTranslationY(View view, float translationY) {
        view.setTranslationY(translationY);
    }

    @Override
    public float getTranslationY(View view) {
        return view.getTranslationY();
    }

    @Override
    public void removeOnGlobalLayoutListener(ViewTreeObserver viewTreeObserver, ViewTreeObserver.OnGlobalLayoutListener victim) {
        viewTreeObserver.removeOnGlobalLayoutListener(victim);
    }
}
