package it.cammino.risuscito.items

import android.content.pm.ResolveInfo
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mikepenz.fastadapter.items.AbstractItem
import it.cammino.risuscito.R
import kotlinx.android.synthetic.main.bottom_item.view.*

class BottomSheetItem : AbstractItem<BottomSheetItem, BottomSheetItem.ViewHolder>() {

    var item: ResolveInfo? = null
        private set

    fun withItem(item: ResolveInfo): BottomSheetItem {
        this.item = item
        return this
    }

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    override fun getType(): Int {
        return R.id.fastadapter_bottom_item_id
    }

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override fun getLayoutRes(): Int {
        return R.layout.bottom_item
    }

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param viewHolder the viewHolder of this item
     */
    override fun bindView(viewHolder: ViewHolder, payloads: List<Any>) {
        super.bindView(viewHolder, payloads)

        val pm = viewHolder.itemView.context.packageManager
        viewHolder.mIcon!!.setImageDrawable(item!!.loadIcon(pm))
        viewHolder.mLabel!!.text = item!!.loadLabel(pm)

    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.mLabel!!.text = null
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    /**
     * our ViewHolder
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var mIcon: ImageView? = null
        internal var mLabel: TextView? = null

        init {
            mIcon = view.app_icon
            mLabel = view.app_label
        }
    }
}