package it.cammino.risuscito.items

import android.content.pm.ResolveInfo
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.IItemVHFactory
import com.mikepenz.fastadapter.items.BaseItem
import com.mikepenz.fastadapter.items.BaseItemFactory
import it.cammino.risuscito.R
import kotlinx.android.synthetic.main.bottom_item.view.*

class BottomSheetItem : BaseItem<BottomSheetItem.ViewHolder>() {

    override val type: Int
        get() = R.id.fastadapter_bottom_item_id

    override val factory: IItemVHFactory<ViewHolder> = BottomSheetItemFactory

    var item: ResolveInfo? = null
        private set

    fun withItem(item: ResolveInfo): BottomSheetItem {
        this.item = item
        return this
    }

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

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var mIcon: ImageView? = null
        internal var mLabel: TextView? = null

        init {
            mIcon = view.app_icon
            mLabel = view.app_label
        }
    }
}

object BottomSheetItemFactory : BaseItemFactory<BottomSheetItem.ViewHolder>() {

    override val type: Int
        get() = R.id.fastadapter_bottom_item_id

    override val layoutRes: Int
        get() = R.layout.bottom_item

    override fun getViewHolder(v: View): BottomSheetItem.ViewHolder {
        return BottomSheetItem.ViewHolder(v)
    }

}