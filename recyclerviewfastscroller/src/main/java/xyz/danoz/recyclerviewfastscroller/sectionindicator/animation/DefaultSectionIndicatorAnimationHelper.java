package xyz.danoz.recyclerviewfastscroller.sectionindicator.animation;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.View;

/**
 * Default implementation of the {@link SectionIndicatorAnimationHelper}
 */
public class DefaultSectionIndicatorAnimationHelper implements SectionIndicatorAnimationHelper {

    private static final int SHOW_ANIMATION_DURATION = 100;
    private static final int HIDE_ANIMATION_DURATION = 500;

    private final View mSectionIndicatorView;
    private boolean mShown;

    public DefaultSectionIndicatorAnimationHelper(View sectionIndicatorView) {
        mSectionIndicatorView = sectionIndicatorView;
        mShown = false;
        mSectionIndicatorView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showWithAnimation() {
        if (mShown) {
            return;
        }

        mShown = true;

        ViewPropertyAnimatorCompat animator = ViewCompat.animate(mSectionIndicatorView);

        animator.cancel();

        ViewCompat.setAlpha(mSectionIndicatorView, 0.0f);
        animator.alpha(1.0f);
        animator.setDuration(SHOW_ANIMATION_DURATION);
        animator.setListener(new ViewPropertyAnimatorListener() {
            @Override
            public void onAnimationStart(View view) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(View view) {
            }

            @Override
            public void onAnimationCancel(View view) {
            }
        });
        animator.start();
    }

    @Override
    public void hideWithAnimation() {
        if (!mShown) {
            return;
        }

        mShown = false;

        ViewPropertyAnimatorCompat animator = ViewCompat.animate(mSectionIndicatorView);

        animator.cancel();

        animator.alpha(0.0f);
        animator.setDuration(HIDE_ANIMATION_DURATION);
        animator.setListener(new ViewPropertyAnimatorListener() {
            @Override
            public void onAnimationStart(View view) {
            }

            @Override
            public void onAnimationEnd(View view) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(View view) {
            }
        });
        animator.start();
    }
}
