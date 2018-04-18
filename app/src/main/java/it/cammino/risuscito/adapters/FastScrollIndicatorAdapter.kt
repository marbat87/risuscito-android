package it.cammino.risuscito.adapters

import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.turingtechnologies.materialscrollbar.ICustomAdapter
import it.cammino.risuscito.items.SimpleItem

class FastScrollIndicatorAdapter<Item : IItem<*, *>>(private val mIndicator: Int) : FastItemAdapter<Item>(), ICustomAdapter {

//    companion object {
//        private val TAG = FastScrollIndicatorAdapter::class.java.canonicalName
//    }

    override fun getCustomStringForElement(position: Int): String {
//        Log.d(TAG, "getSectionName: position $position")
        val item = getAdapterItem(position)
//        Log.d(TAG, "getSectionName: " + (item as SimpleItem).title!!.text)
        return when (mIndicator) {
            0 -> (item as SimpleItem).title!!.text.toString().substring(0, 1).toUpperCase()
            1 -> (item as SimpleItem).page!!.text.toString()
            2 -> (item as SimpleItem).numSalmo.toString()
            else -> (item as SimpleItem).title!!.text.toString().substring(0, 1).toUpperCase()
        }
    }
}
