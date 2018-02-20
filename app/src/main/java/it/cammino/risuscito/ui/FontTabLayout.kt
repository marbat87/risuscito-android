package it.cammino.risuscito.ui

import android.content.Context
import android.graphics.Typeface
import android.support.design.widget.TabLayout
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import it.cammino.risuscito.R

/**
 * Simple helper class which extends a TabLayout to allow us to customize the font of the tab.
 */
class FontTabLayout : TabLayout {

    private var mTypeface: Typeface? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context) {
        mTypeface = Typeface.createFromAsset(context.assets, FONT_PATH)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.FontTabLayout,
                0, 0)
        try {
            val fontPath = a.getString(R.styleable.FontTabLayout_fontPath)
            if (fontPath != null)
                Log.d("FontTabLayout", "init: " + fontPath)
            else
                Log.d("FontTabLayout", "init: NULL")
            mTypeface = Typeface.createFromAsset(context.assets, if (fontPath != null && !fontPath.isEmpty()) fontPath else FONT_PATH)
        } finally {
            a.recycle()
        }
    }

    override fun addTab(tab: TabLayout.Tab, position: Int, setSelected: Boolean) {
        super.addTab(tab, position, setSelected)

        val mainView = getChildAt(0) as ViewGroup
        val tabView = mainView.getChildAt(tab.position) as ViewGroup
        val tabChildCount = tabView.childCount
        (0 until tabChildCount)
                .map { tabView.getChildAt(it) }
                .forEach { (it as? TextView)?.setTypeface(mTypeface, Typeface.NORMAL) }
    }

    companion object {
        private val FONT_PATH = "fonts/Roboto-Medium.ttf"
    }
}