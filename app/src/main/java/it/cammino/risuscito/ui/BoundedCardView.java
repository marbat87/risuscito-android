package it.cammino.risuscito.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;

import it.cammino.risuscito.R;

public class BoundedCardView extends CardView {

    private int boundedWidth;
    private int boundedHeight;

    public BoundedCardView(Context context) {
        super(context);
        boundedWidth = 0;
        boundedHeight = 0;
    }

    public BoundedCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BoundedView);
        boundedWidth = a.getDimensionPixelSize(R.styleable.BoundedView_bounded_width, 0);
        boundedHeight = a.getDimensionPixelSize(R.styleable.BoundedView_bounded_height, 0);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Adjust width as necessary
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        Log.d(getClass().getName(), "onMeasure: boundedWidth " + boundedWidth);
        Log.d(getClass().getName(), "onMeasure: measuredWidth " + measuredWidth);
        if (boundedWidth > 0 && boundedWidth < measuredWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(boundedWidth, measureMode);
        }
        // Adjust height as necessary
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (boundedHeight > 0 && boundedHeight < measuredHeight) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(boundedHeight, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}