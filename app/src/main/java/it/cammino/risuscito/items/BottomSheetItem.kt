package it.cammino.risuscito.items

import android.content.pm.ResolveInfo
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import it.cammino.risuscito.R
import kotlinx.android.synthetic.main.bottom_item.view.*

class BottomSheetItem : AbstractItem<BottomSheetItem.ViewHolder>() {

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
    override val type: Int
        get() = R.id.fastadapter_bottom_item_id

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override val layoutRes: Int
        get() = R.layout.bottom_item

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param holder the viewHolder of this item
     */
    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        val pm = holder.itemView.context.packageManager
        holder.mIcon?.setImageDrawable(item?.loadIcon(pm))
        holder.mLabel?.text = item?.loadLabel(pm)

    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.mLabel?.text = null
        holder.mIcon?.setImageDrawable(null)
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