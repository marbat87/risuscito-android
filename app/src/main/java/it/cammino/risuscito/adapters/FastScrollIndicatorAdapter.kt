package it.cammino.risuscito.adapters

import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.turingtechnologies.materialscrollbar.ICustomAdapter
import it.cammino.risuscito.items.SimpleItem

class FastScrollIndicatorAdapter(private val mIndicator: Int) : FastItemAdapter<SimpleItem>(), ICustomAdapter {

    override fun getCustomStringForElement(position: Int): String {
        val item = getItem(position)
        return when (mIndicator) {
            0 -> item!!.title!!.text.toString().substring(0, 1).toUpperCase()
            1 -> item!!.page!!.text.toString()
            2 -> item!!.numSalmo.toString()
            else -> item!!.title!!.text.toString().substring(0, 1).toUpperCase()
        }
    }

}
