package it.cammino.risuscito.items

import android.content.pm.ResolveInfo
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import it.cammino.risuscito.R
import kotlinx.android.synthetic.main.bottom_item.view.*

class BottomSheetItem : AbstractItem<BottomSheetItem.ViewHolder>() {

    var infoItem: ResolveInfo? = null
        private set

    fun withItem(item: ResolveInfo): BottomSheetItem {
        this.infoItem = item
        return this
    }

    override val type: Int
        get() = R.id.fastadapter_bottom_item_id

    override val layoutRes: Int
        get() = R.layout.bottom_item

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<BottomSheetItem>(view) {
        private var mIcon: ImageView? = null
        private var mLabel: TextView? = null

        override fun bindView(item: BottomSheetItem, payloads: List<Any>) {
            val pm = itemView.context.packageManager
            mIcon?.setImageDrawable(item.infoItem?.loadIcon(pm))
            mLabel?.text = item.infoItem?.loadLabel(pm)
        }

        override fun unbindView(item: BottomSheetItem) {
            mLabel?.text = null
            mIcon?.setImageDrawable(null)
        }

        init {
            mIcon = view.app_icon
            mLabel = view.app_label
        }
    }

}