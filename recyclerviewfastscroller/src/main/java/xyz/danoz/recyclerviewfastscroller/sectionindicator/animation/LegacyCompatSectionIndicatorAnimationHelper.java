package xyz.danoz.recyclerviewfastscroller.sectionindicator.animation;

import android.view.View;

/**
 * This class is intended to use for API 10 or older devices which do not support *alpha* property of views.
 */
public class LegacyCompatSectionIndicatorAnimationHelper implements SectionIndicatorAnimationHelper {

    private final View mSectionIndicatorView;

    public LegacyCompatSectionIndicatorAnimationHelper(View sectionIndicatorView) {
        mSectionIndicatorView = sectionIndicatorView;
        mSectionIndicatorView.setVisibility(View.GONE);
    }

    public void showWithAnimation(){
        mSectionIndicatorView.setVisibility(View.VISIBLE);
    }

    public void hideWithAnimation(){
        mSectionIndicatorView.setVisibility(View.GONE);
    }
}
