package it.cammino.risuscito.items

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.expandable.ExpandableExtension
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.risuscito.R
import it.cammino.risuscito.utils.Utility.helperSetString

fun simpleSubExpandableItem(block: SimpleSubExpandableItem.() -> Unit): SimpleSubExpandableItem =
    SimpleSubExpandableItem().apply(block)

class SimpleSubExpandableItem : AbstractExpandableItem<SimpleSubExpandableItem.ViewHolder>(),
    ISubItem<SimpleSubExpandableItem.ViewHolder> {

    var title: StringHolder? = null
        private set
    var setTitle: Any? = null
        set(value) {
            title = helperSetString(value)
        }

    var totItems: Int = 0

    var position: Int = 0

    var id: Int = 0
        set(value) {
            identifier = value.toLong()
            field = value
        }

    override val type: Int
        get() = R.id.fastadapter_expandable_item_id

    override val layoutRes: Int
        get() = R.layout.list_group_item

    override fun bindView(holder: ViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)

        val p = payloads.mapNotNull { it as? String }.lastOrNull()
        if (p != null) {
            // Check if this was an expanding or collapsing action by checking the payload.
            // If it is we need to animate the changes
            if (p == ExpandableExtension.PAYLOAD_EXPAND) {
                ViewCompat.animate(holder.mIndicator).rotation(0f).start()
                return
            } else if (p == ExpandableExtension.PAYLOAD_COLLAPSE) {
                ViewCompat.animate(holder.mIndicator).rotation(180f).start()
                return
            }
        }

        //get the context
        val ctx = holder.itemView.context

        //set the background for the item
        holder.view.clearAnimation()
        holder.mTitle.text = "${title?.getText(ctx)} (${totItems})"

        holder.mIndicator.rotation = if (isExpanded) 0f else 180f
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.mTitle.text = null
        //make sure all animations are stopped
        holder.mIndicator.clearAnimation()
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var mTitle: TextView = view.findViewById(R.id.group_title)
        var mIndicator: ImageView = view.findViewById(R.id.group_indicator)
    }

}
