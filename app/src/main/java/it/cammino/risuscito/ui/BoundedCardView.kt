package it.cammino.risuscito.ui

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import it.cammino.risuscito.R

class BoundedCardView : CardView {

    private var boundedWidth: Int = 0
    private var boundedHeight: Int = 0

    constructor(context: Context) : super(context) {
        boundedWidth = 0
        boundedHeight = 0
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BoundedCardView)
        boundedWidth = a.getDimensionPixelSize(R.styleable.BoundedCardView_bounded_width, 0)
        boundedHeight = a.getDimensionPixelSize(R.styleable.BoundedCardView_bounded_height, 0)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var mWidthMeasureSpec = widthMeasureSpec
        var mHeightMeasureSpec = heightMeasureSpec
        // Adjust width as necessary
        val measuredWidth = MeasureSpec.getSize(mWidthMeasureSpec)
        //        Log.d(getClass().getName(), "onMeasure: boundedWidth " + boundedWidth);
        //        Log.d(getClass().getName(), "onMeasure: measuredWidth " + measuredWidth);
        if (boundedWidth in 1..(measuredWidth - 1)) {
            val measureMode = MeasureSpec.getMode(mWidthMeasureSpec)
            mWidthMeasureSpec = MeasureSpec.makeMeasureSpec(boundedWidth, measureMode)
        }
        // Adjust height as necessary
        val measuredHeight = MeasureSpec.getSize(mHeightMeasureSpec)
        if (boundedHeight in 1..(measuredHeight - 1)) {
            val measureMode = MeasureSpec.getMode(mHeightMeasureSpec)
            mHeightMeasureSpec = MeasureSpec.makeMeasureSpec(boundedHeight, measureMode)
        }
        super.onMeasure(mWidthMeasureSpec, mHeightMeasureSpec)
    }
}