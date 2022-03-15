package it.cammino.risuscito.adapters

import android.content.Context
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.turingtechnologies.materialscrollbar.ICustomAdapter
import it.cammino.risuscito.items.SimpleItem
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale

class FastScrollIndicatorAdapter(private val mIndicator: Int, private val mContext: Context) : FastItemAdapter<SimpleItem>(), ICustomAdapter {

    override fun getCustomStringForElement(position: Int): String {
        val item = getItem(position)
        return when (mIndicator) {
            0 -> item?.title?.getText(mContext)?.substring(0, 1)?.uppercase(getSystemLocale(mContext.resources))
                    ?: ""
            1 -> item?.page?.getText(mContext) ?: ""
            else -> item?.title?.getText(mContext)?.substring(0, 1)?.uppercase(getSystemLocale(mContext.resources))
                    ?: ""
        }
    }

}
