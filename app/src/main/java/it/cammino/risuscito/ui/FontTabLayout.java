package it.cammino.risuscito.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.cammino.risuscito.R;

/**
 * Simple helper class which extends a TabLayout to allow us to customize the font of the tab.
 */
public final class FontTabLayout extends TabLayout {
    private static final String FONT_PATH = "fonts/Roboto-Medium.ttf";

    private Typeface mTypeface;

    public FontTabLayout(Context context) {
        super(context);
        init(context);
    }

    public FontTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FontTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context) {
        mTypeface = Typeface.createFromAsset(context.getAssets(), FONT_PATH);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.FontTabLayout,
                0, 0);
        try {
//            mTypeface = Typeface.createFromAsset(context.getAssets(), FONT_PATH);
            String fontPath = a.getString(R.styleable.FontTabLayout_fontPath);
            if (fontPath != null)
                Log.d("FontTabLayout", "init: " + fontPath);
            else
                Log.d("FontTabLayout", "init: NULL");
            mTypeface = Typeface.createFromAsset(context.getAssets(), fontPath != null&& !fontPath.isEmpty() ? fontPath : FONT_PATH);
        }
        finally {
            a.recycle();
        }
    }

    @Override
    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        super.addTab(tab, position, setSelected);

        ViewGroup mainView = (ViewGroup) getChildAt(0);
        ViewGroup tabView = (ViewGroup) mainView.getChildAt(tab.getPosition());
        int tabChildCount = tabView.getChildCount();
        for (int i = 0; i < tabChildCount; i++) {
            View tabViewChild = tabView.getChildAt(i);
            if (tabViewChild instanceof TextView) {
                ((TextView) tabViewChild).setTypeface(mTypeface, Typeface.NORMAL);
            }
        }
    }
}