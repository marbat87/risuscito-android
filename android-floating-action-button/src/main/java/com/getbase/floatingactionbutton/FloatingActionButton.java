package com.getbase.floatingactionbutton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.ShapeDrawable.ShaderFactory;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

@CoordinatorLayout.DefaultBehavior(FloatingActionButton.Behavior.class)
public class FloatingActionButton extends ImageButton {

    public static final int SIZE_NORMAL = 0;
    public static final int SIZE_MINI = 1;
    private static final int TRANSLATE_DURATION_MILLIS = 300;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SIZE_NORMAL, SIZE_MINI})
    public @interface FAB_SIZE {
    }

    int mColorNormal;
    int mColorPressed;
    int mColorDisabled;
    String mTitle;
    @DrawableRes
    private int mIcon;
    private Drawable mIconDrawable;
    private int mSize;

    private float mCircleSize;
    private float mShadowRadius;
    private float mShadowOffset;
    private int mDrawableSize;
    boolean mStrokeVisible;
    private boolean mVisible;
    private boolean mIgnoreLayoutChanges;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    void init(Context context, AttributeSet attributeSet) {
        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionButton, 0, 0);
        mColorNormal = attr.getColor(R.styleable.FloatingActionButton_fab_colorNormal, getColor(android.R.color.holo_blue_dark));
        mColorPressed = attr.getColor(R.styleable.FloatingActionButton_fab_colorPressed, getColor(android.R.color.holo_blue_light));
        mColorDisabled = attr.getColor(R.styleable.FloatingActionButton_fab_colorDisabled, getColor(android.R.color.darker_gray));
        mSize = attr.getInt(R.styleable.FloatingActionButton_fab_size, SIZE_NORMAL);
        mIcon = attr.getResourceId(R.styleable.FloatingActionButton_fab_icon, 0);
        mTitle = attr.getString(R.styleable.FloatingActionButton_fab_title);
        mStrokeVisible = attr.getBoolean(R.styleable.FloatingActionButton_fab_stroke_visible, true);
        attr.recycle();

        updateCircleSize();
        mShadowRadius = getDimension(R.dimen.fab_shadow_radius);
        mShadowOffset = getDimension(R.dimen.fab_shadow_offset);
        updateDrawableSize();

        updateBackground();
        mVisible = true;
        mIgnoreLayoutChanges = false;
    }

    public void show() {
        show(true);
    }

    public void show(boolean animate) {
//        toggleShown(true, animate, false);
        toggle(true, animate, false);
    }

    public void hide() {
        hide(true);
    }

    public void hide(boolean animate) {
//        toggleShown(false, animate, false);
        toggle(false, animate, false);
    }

    private void toggleShown(final boolean visible, final boolean animate, boolean force) {
        if (mVisible != visible || force) {
            mVisible = visible;
            int height = getHeight();
            if (height == 0 && !force) {
                ViewTreeObserver vto = getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }
                            toggleShown(visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }
            int translationY = visible ? 0 : height + getMarginBottom();
            if (animate) {
                AnimatorSet set = new AnimatorSet().setDuration(TRANSLATE_DURATION_MILLIS);
                ObjectAnimator animator = new ObjectAnimator();
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setPropertyName("translationY");
                animator.setFloatValues(0, translationY);
                animator.setTarget(this);
                set.play(animator);
//                animate().setInterpolator(new AccelerateDecelerateInterpolator())
//                        .setDuration(TRANSLATE_DURATION_MILLIS)
//                        .translationY(translationY);
            } else {
//                this.setTranslationY(translationY);
                ViewHelper.setTranslationY(this, translationY);
            }

            // On pre-Honeycomb a translated view is still clickable, so we need to disable clicks manually
            if (Build.VERSION.SDK_INT <= VERSION_CODES.HONEYCOMB) {
                setClickable(visible);
            }
        }
    }

    private void toggle(final boolean visible, final boolean animate, boolean force) {
        if(this.mVisible != visible || force) {
            this.mVisible = visible;
            int height = this.getHeight();
            if(height == 0 && !force) {
                ViewTreeObserver translationY = this.getViewTreeObserver();
                if(translationY.isAlive()) {
                    translationY.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = getViewTreeObserver();
                            if(currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }

                            toggle(visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }

            int translationY1 = visible?0:height + this.getMarginBottom();
            if(animate) {
                ViewPropertyAnimator.animate(this).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(TRANSLATE_DURATION_MILLIS).translationY((float)translationY1);
            } else {
                ViewHelper.setTranslationY(this, (float)translationY1);
            }

            if(!this.hasHoneycombApi()) {
                this.setClickable(visible);
            }
        }

    }

    private int getMarginBottom() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }

    private void updateDrawableSize() {
        mDrawableSize = (int) (mCircleSize + 2 * mShadowRadius);
    }

    private void updateCircleSize() {
        mCircleSize = getDimension(mSize == SIZE_NORMAL ? R.dimen.fab_size_normal : R.dimen.fab_size_mini);
    }

    public void setSize(@FAB_SIZE int size) {
        if (size != SIZE_MINI && size != SIZE_NORMAL) {
            throw new IllegalArgumentException("Use @FAB_SIZE constants only!");
        }

        if (mSize != size) {
            mSize = size;
            updateCircleSize();
            updateDrawableSize();
            updateBackground();
        }
    }

    @FAB_SIZE
    public int getSize() {
        return mSize;
    }

    public void setIcon(@DrawableRes int icon) {
        if (mIcon != icon) {
            mIcon = icon;
            mIconDrawable = null;
            updateBackground();
        }
    }

    public void setIconDrawable(@NonNull Drawable iconDrawable) {
        if (mIconDrawable != iconDrawable) {
            mIcon = 0;
            mIconDrawable = iconDrawable;
            updateBackground();
        }
    }

    /**
     * @return the current Color for normal state.
     */
    public int getColorNormal() {
        return mColorNormal;
    }

    public void setColorNormalResId(@ColorRes int colorNormal) {
        setColorNormal(getColor(colorNormal));
    }

    public void setColorNormal(int color) {
        if (mColorNormal != color) {
            mColorNormal = color;
            updateBackground();
        }
    }

    /**
     * @return the current color for pressed state.
     */
    public int getColorPressed() {
        return mColorPressed;
    }

    public void setColorPressedResId(@ColorRes int colorPressed) {
        setColorPressed(getColor(colorPressed));
    }

    public void setColorPressed(int color) {
        if (mColorPressed != color) {
            mColorPressed = color;
            updateBackground();
        }
    }

    /**
     * @return the current color for disabled state.
     */
    public int getColorDisabled() {
        return mColorDisabled;
    }

    public void setColorDisabledResId(@ColorRes int colorDisabled) {
        setColorDisabled(getColor(colorDisabled));
    }

    public void setColorDisabled(int color) {
        if (mColorDisabled != color) {
            mColorDisabled = color;
            updateBackground();
        }
    }

    public void setStrokeVisible(boolean visible) {
        if (mStrokeVisible != visible) {
            mStrokeVisible = visible;
            updateBackground();
        }
    }

    public boolean isStrokeVisible() {
        return mStrokeVisible;
    }

    int getColor(@ColorRes int id) {
        return getResources().getColor(id);
    }

    float getDimension(@DimenRes int id) {
        return getResources().getDimension(id);
    }

    public void setTitle(String title) {
        mTitle = title;
        TextView label = getLabelView();
        if (label != null) {
            label.setText(title);
        }
    }

    TextView getLabelView() {
        return (TextView) getTag(R.id.fab_label);
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean ismIgnoreLayoutChanges() {
        return mIgnoreLayoutChanges;
    }

    public void setmIgnoreLayoutChanges(boolean mIgnoreLayoutChanges) {
        this.mIgnoreLayoutChanges = mIgnoreLayoutChanges;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mDrawableSize, mDrawableSize);
    }

    void updateBackground() {
        final float strokeWidth = getDimension(R.dimen.fab_stroke_width);
        final float halfStrokeWidth = strokeWidth / 2f;

        LayerDrawable layerDrawable = new LayerDrawable(
                new Drawable[]{
                        getResources().getDrawable(mSize == SIZE_NORMAL ? R.drawable.fab_bg_normal : R.drawable.fab_bg_mini),
                        createFillDrawable(strokeWidth),
                        createOuterStrokeDrawable(strokeWidth),
                        getIconDrawable()
                });

        int iconOffset = (int) (mCircleSize - getDimension(R.dimen.fab_icon_size)) / 2;

        int circleInsetHorizontal = (int) (mShadowRadius);
        int circleInsetTop = (int) (mShadowRadius - mShadowOffset);
        int circleInsetBottom = (int) (mShadowRadius + mShadowOffset);

        layerDrawable.setLayerInset(1,
                circleInsetHorizontal,
                circleInsetTop,
                circleInsetHorizontal,
                circleInsetBottom);

        layerDrawable.setLayerInset(2,
                (int) (circleInsetHorizontal - halfStrokeWidth),
                (int) (circleInsetTop - halfStrokeWidth),
                (int) (circleInsetHorizontal - halfStrokeWidth),
                (int) (circleInsetBottom - halfStrokeWidth));

        layerDrawable.setLayerInset(3,
                circleInsetHorizontal + iconOffset,
                circleInsetTop + iconOffset,
                circleInsetHorizontal + iconOffset,
                circleInsetBottom + iconOffset);

        setBackgroundCompat(layerDrawable);
    }

    Drawable getIconDrawable() {
        if (mIconDrawable != null) {
            return mIconDrawable;
        } else if (mIcon != 0) {
            return getResources().getDrawable(mIcon);
        } else {
            return new ColorDrawable(Color.TRANSPARENT);
        }
    }

    private StateListDrawable createFillDrawable(float strokeWidth) {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{-android.R.attr.state_enabled}, createCircleDrawable(mColorDisabled, strokeWidth));
        drawable.addState(new int[]{android.R.attr.state_pressed}, createCircleDrawable(mColorPressed, strokeWidth));
        drawable.addState(new int[]{}, createCircleDrawable(mColorNormal, strokeWidth));
        return drawable;
    }

    private Drawable createCircleDrawable(int color, float strokeWidth) {
        int alpha = Color.alpha(color);
        int opaqueColor = opaque(color);

        ShapeDrawable fillDrawable = new ShapeDrawable(new OvalShape());

        final Paint paint = fillDrawable.getPaint();
        paint.setAntiAlias(true);
        paint.setColor(opaqueColor);

        Drawable[] layers = {
                fillDrawable,
                createInnerStrokesDrawable(opaqueColor, strokeWidth)
        };

        LayerDrawable drawable = alpha == 255 || !mStrokeVisible
                ? new LayerDrawable(layers)
                : new TranslucentLayerDrawable(alpha, layers);

        int halfStrokeWidth = (int) (strokeWidth / 2f);
        drawable.setLayerInset(1, halfStrokeWidth, halfStrokeWidth, halfStrokeWidth, halfStrokeWidth);

        return drawable;
    }

    private static class TranslucentLayerDrawable extends LayerDrawable {
        private final int mAlpha;

        public TranslucentLayerDrawable(int alpha, Drawable... layers) {
            super(layers);
            mAlpha = alpha;
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            canvas.saveLayerAlpha(bounds.left, bounds.top, bounds.right, bounds.bottom, mAlpha, Canvas.ALL_SAVE_FLAG);
            super.draw(canvas);
            canvas.restore();
        }
    }

    private Drawable createOuterStrokeDrawable(float strokeWidth) {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());

        final Paint paint = shapeDrawable.getPaint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setAlpha(opacityToAlpha(0.02f));

        return shapeDrawable;
    }

    private int opacityToAlpha(float opacity) {
        return (int) (255f * opacity);
    }

    private int darkenColor(int argb) {
        return adjustColorBrightness(argb, 0.9f);
    }

    private int lightenColor(int argb) {
        return adjustColorBrightness(argb, 1.1f);
    }

    private int adjustColorBrightness(int argb, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(argb, hsv);

        hsv[2] = Math.min(hsv[2] * factor, 1f);

        return Color.HSVToColor(Color.alpha(argb), hsv);
    }

    private int halfTransparent(int argb) {
        return Color.argb(
                Color.alpha(argb) / 2,
                Color.red(argb),
                Color.green(argb),
                Color.blue(argb)
        );
    }

    private int opaque(int argb) {
        return Color.rgb(
                Color.red(argb),
                Color.green(argb),
                Color.blue(argb)
        );
    }

    private Drawable createInnerStrokesDrawable(final int color, float strokeWidth) {
        if (!mStrokeVisible) {
            return new ColorDrawable(Color.TRANSPARENT);
        }

        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());

        final int bottomStrokeColor = darkenColor(color);
        final int bottomStrokeColorHalfTransparent = halfTransparent(bottomStrokeColor);
        final int topStrokeColor = lightenColor(color);
        final int topStrokeColorHalfTransparent = halfTransparent(topStrokeColor);

        final Paint paint = shapeDrawable.getPaint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Style.STROKE);
        shapeDrawable.setShaderFactory(new ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                return new LinearGradient(width / 2, 0, width / 2, height,
                        new int[]{topStrokeColor, topStrokeColorHalfTransparent, color, bottomStrokeColorHalfTransparent, bottomStrokeColor},
                        new float[]{0f, 0.2f, 0.5f, 0.8f, 1f},
                        TileMode.CLAMP
                );
            }
        });

        return shapeDrawable;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void setBackgroundCompat(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        TextView label = getLabelView();
        if (label != null) {
            label.setVisibility(visibility);
        }

        super.setVisibility(visibility);
    }

    public static class Behavior extends android.support.design.widget.CoordinatorLayout.Behavior<FloatingActionButton> {
        private static final boolean SNACKBAR_BEHAVIOR_ENABLED;
        private Rect mTmpRect;
        private boolean mIsAnimatingOut;
        private float mTranslationY;

        public Behavior() {
        }

        public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
            return (SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout)
                    || (dependency instanceof AppBarLayout && !child.mIgnoreLayoutChanges);
        }

        public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
//            Log.i(getClass().toString(), "ENTRO");
            if(dependency instanceof Snackbar.SnackbarLayout) {
                this.updateFabTranslationForSnackbar(parent, child, dependency);
            } else if(dependency instanceof AppBarLayout) {
//                AppBarLayout appBarLayout = (AppBarLayout)dependency;
                if(this.mTmpRect == null) {
                    this.mTmpRect = new Rect();
                }

                int rect_bottom = this.mTmpRect.bottom;
//                Log.i(getClass().toString(), "this.mTmpRect prima: " + this.mTmpRect.bottom);
//                Log.i(getClass().toString(), "rect.bottom prima: " + rect_bottom);
                ButtonGroupUtils.getDescendantRect(parent, dependency, this.mTmpRect);
//                Log.i(getClass().toString(), "this.mTmpRect dopo: " + this.mTmpRect.bottom);
//                Log.i(getClass().toString(), "rect.bottom dopo: " + rect_bottom);
//                int topInset = this.mLastInsets != null?this.mLastInsets.getSystemWindowInsetTop():0;
//                int topInset = 0;
//                int result;
//                int minHeight = ViewCompat.getMinimumHeight(appBarLayout);
//                Log.i(getClass().toString(), "minHeight: " + minHeight);
//                if(minHeight != 0) {
//                    result = minHeight * 2 + topInset;
//                } else {
//                    int childCount = appBarLayout.getChildCount();
//                    result = childCount >= 1?ViewCompat.getMinimumHeight(appBarLayout.getChildAt(childCount - 1)) * 2 + topInset:0;
//                }
//                if(rect.bottom <= appBarLayout.getMinimumHeightForVisibleOverlappingContent()) {
//                    if(!this.mIsAnimatingOut && child.getVisibility() == VISIBLE) {
//                        this.animateOut(child);
//                    }
//                } else if(child.getVisibility() != VISIBLE) {
//                    this.animateIn(child);
//                }
                if(rect_bottom > mTmpRect.bottom) {
                    if(child.mVisible) {
                        child.hide();
                    }
                } else if(!child.mVisible) {
                    child.show();
                }
            }

            return false;
        }

        private void updateFabTranslationForSnackbar(CoordinatorLayout parent, FloatingActionButton fab, View snackbar) {
            float translationY = this.getFabTranslationYForSnackbar(parent, fab);
            if(translationY != this.mTranslationY) {
                ViewCompat.animate(fab).cancel();
                if(Math.abs(translationY - this.mTranslationY) == (float)snackbar.getHeight()) {
                    ViewCompat.animate(fab).translationY(translationY).setInterpolator(new FastOutSlowInInterpolator()).setListener((ViewPropertyAnimatorListener)null);
                } else {
                    ViewCompat.setTranslationY(fab, translationY);
                }

                this.mTranslationY = translationY;
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

        private void animateIn(FloatingActionButton button) {
            button.setVisibility(VISIBLE);
            if(Build.VERSION.SDK_INT >= 14) {
                ViewCompat.animate(button).scaleX(1.0F).scaleY(1.0F).alpha(1.0F).setInterpolator(new FastOutSlowInInterpolator()).withLayer().setListener((ViewPropertyAnimatorListener)null).start();
            } else {
                Animation anim = android.view.animation.AnimationUtils.loadAnimation(button.getContext(), R.anim.fab_in);
                anim.setDuration(200L);
                anim.setInterpolator(new FastOutSlowInInterpolator());
                button.startAnimation(anim);
            }

        }

        private void animateOut(final FloatingActionButton button) {
            if(Build.VERSION.SDK_INT >= 14) {
                ViewCompat.animate(button).scaleX(0.0F).scaleY(0.0F).alpha(0.0F).setInterpolator(new FastOutSlowInInterpolator()).withLayer().setListener(new ViewPropertyAnimatorListener() {
                    public void onAnimationStart(View view) {
                        Behavior.this.mIsAnimatingOut = true;
                    }

                    public void onAnimationCancel(View view) {
                        Behavior.this.mIsAnimatingOut = false;
                    }

                    public void onAnimationEnd(View view) {
                        Behavior.this.mIsAnimatingOut = false;
                        view.setVisibility(GONE);
                    }
                }).start();
            } else {
                Animation anim = android.view.animation.AnimationUtils.loadAnimation(button.getContext(), R.anim.fab_out);
                anim.setInterpolator(new FastOutSlowInInterpolator());
                anim.setDuration(200L);
                anim.setAnimationListener(new AnimationListenerAdapter() {
                    public void onAnimationStart(Animation animation) {
                        Behavior.this.mIsAnimatingOut = true;
                    }

                    public void onAnimationEnd(Animation animation) {
                        Behavior.this.mIsAnimatingOut = false;
                        button.setVisibility(GONE);
                    }
                });
                button.startAnimation(anim);
            }

        }

        static {
            SNACKBAR_BEHAVIOR_ENABLED = Build.VERSION.SDK_INT >= 11;
        }
    }

    private boolean hasHoneycombApi() {
        return Build.VERSION.SDK_INT >= 11;
    }

    static class AnimationListenerAdapter implements Animation.AnimationListener {
        AnimationListenerAdapter() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }
}
