package it.cammino.risuscito.ui

import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import com.gordonwong.materialsheetfab.AnimatedFab

class FabSheet : FloatingActionButton, AnimatedFab {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun show(translationX: Float, translationY: Float) {
        show()
    }

}