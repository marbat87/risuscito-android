package xyz.danoz.recyclerviewfastscroller.sectionindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import xyz.danoz.recyclerviewfastscroller.R;
import xyz.danoz.recyclerviewfastscroller.calculation.VerticalScrollBoundsProvider;
import xyz.danoz.recyclerviewfastscroller.calculation.position.VerticalScreenPositionCalculator;
import xyz.danoz.recyclerviewfastscroller.sectionindicator.animation.DefaultSectionIndicatorAnimationHelper;
import xyz.danoz.recyclerviewfastscroller.sectionindicator.animation.LegacyCompatSectionIndicatorAnimationHelper;
import xyz.danoz.recyclerviewfastscroller.sectionindicator.animation.SectionIndicatorAnimationHelper;
import xyz.danoz.recyclerviewfastscroller.utils.ViewUtils;

/**
 * Abstract base implementation of a section indicator used to indicate the section of a list upon which the user is
 * currently fast scrolling.
 */
public abstract class AbsSectionIndicator<T> extends RelativeLayout implements SectionIndicator<T> {

    private static final int[] STYLEABLE = R.styleable.rvfs_AbsSectionIndicator;

    private VerticalScreenPositionCalculator mScreenPositionCalculator;
    private SectionIndicatorAnimationHelper mDefaultSectionIndicatorAnimationHelper;
    private float mIndicatorOffset;
    private int mUnspecifiedMeasuredHeight = -1;

    public AbsSectionIndicator(Context context) {
        this(context, null);
    }

    public AbsSectionIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbsSectionIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attributes = getContext().getTheme().obtainStyledAttributes(attrs, STYLEABLE, 0, 0);
        try {
            int layoutId = attributes.getResourceId(R.styleable.rvfs_AbsSectionIndicator_rvfs_section_indicator_layout, getDefaultLayoutId());
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(layoutId, this, true);
        } finally {
            attributes.recycle();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mDefaultSectionIndicatorAnimationHelper = new DefaultSectionIndicatorAnimationHelper(this);
        } else {
            mDefaultSectionIndicatorAnimationHelper = new LegacyCompatSectionIndicatorAnimationHelper(this);
        }
    }

    /**
     * @return the default layout for a given implementation of AbsSectionIndicator
     */
    protected abstract int getDefaultLayoutId();

    /**
     * @return the default background color to be used if not provided by client in XML
     * @see {@link #applyCustomBackgroundColorAttribute(int)}
     */
    protected abstract int getDefaultBackgroundColor();

    /**
     * Clients can provide a custom background color for a section indicator
     * @param color provided in XML via the {@link R.styleable#AbsSectionIndicator_backgroundColor} parameter. If not
     *              specified in XML, this defaults to that which is provided by {@link #getDefaultBackgroundColor()}
     */
    protected abstract void applyCustomBackgroundColorAttribute(int color);

    @Override
    public void onUpdateScrollBarBounds(Rect barBounds) {
        VerticalScrollBoundsProvider boundsProvider = new VerticalScrollBoundsProvider(0, barBounds.height());
        mIndicatorOffset = barBounds.top;
        mScreenPositionCalculator = new VerticalScreenPositionCalculator(boundsProvider);
    }

    @Override
    public void setProgress(float progress) {
        float posY = mScreenPositionCalculator.getYPositionFromScrollProgress(progress);

        // Do not use getHeight() method because getHeight() method may returns cropped value by parent view
        if (mUnspecifiedMeasuredHeight < 0) {
            measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            mUnspecifiedMeasuredHeight = getMeasuredHeight();
        }

        posY = Math.max(0, posY + mIndicatorOffset - mUnspecifiedMeasuredHeight);
        ViewUtils.setTranslationY(this, posY);
    }

    @Override
    public void showWithAnimation() {
        mDefaultSectionIndicatorAnimationHelper.showWithAnimation();
    }

    @Override
    public void hideWithAnimation() {
        mDefaultSectionIndicatorAnimationHelper.hideWithAnimation();
    }

    @Override
    public abstract void setSection(T object);
}
