package it.cammino.risuscito.items

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.expandable.ExpandableExtension
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.risuscito.R
import it.cammino.risuscito.utils.Utility.helperSetString
import it.cammino.risuscito.utils.extension.setSelectableRippleBackground

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

    var group: Int = 0

    override val type: Int
        get() = R.id.fastadapter_expandable_item_id

    override val layoutRes: Int
        get() = R.layout.list_group_item

    @SuppressLint("SetTextI18n")
    override fun bindView(holder: ViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)

        //get the context
        val ctx = holder.itemView.context

        val p = payloads.mapNotNull { it as? String }.lastOrNull()
        if (p != null) {
            // Check if this was an expanding or collapsing action by checking the payload.
            // If it is we need to animate the changes
            if (p == ExpandableExtension.PAYLOAD_EXPAND) {
                holder.mIndicator.animate().rotation(0f).start()
                holder.mIndicator.setColorFilter(
                    MaterialColors.getColor(
                        ctx,
                        androidx.appcompat.R.attr.colorPrimary,
                        TAG
                    )
                )
                holder.mTitle.setTextColor(
                    MaterialColors.getColor(
                        ctx,
                        androidx.appcompat.R.attr.colorPrimary,
                        TAG
                    )
                )
                return
            } else if (p == ExpandableExtension.PAYLOAD_COLLAPSE) {
                holder.mIndicator.animate().rotation(180f).start()
                holder.mIndicator.setColorFilter(
                    MaterialColors.getColor(
                        ctx,
                        com.google.android.material.R.attr.colorOnSurface,
                        TAG
                    )
                )
                holder.mTitle.setTextColor(
                    MaterialColors.getColor(
                        ctx,
                        com.google.android.material.R.attr.colorOnSurface,
                        TAG
                    )
                )
                return
            }
        }

        holder.mContainer.setSelectableRippleBackground(com.google.android.material.R.attr.colorSecondaryContainer)

        //set the background for the item
        holder.view.clearAnimation()
        holder.mTitle.text = "${title?.getText(ctx)} (${totItems})"

        holder.mIndicator.rotation = if (isExpanded) 0f else 180f
        holder.mIndicator.setColorFilter(
            MaterialColors.getColor(
                ctx,
                if (isExpanded) androidx.appcompat.R.attr.colorPrimary else com.google.android.material.R.attr.colorOnSurface,
                TAG
            )
        )
        holder.mTitle.setTextColor(
            MaterialColors.getColor(
                ctx,
                if (isExpanded) androidx.appcompat.R.attr.colorPrimary else com.google.android.material.R.attr.colorOnSurface,
                TAG
            )
        )
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
        var mContainer: View = view.findViewById(R.id.list_view_item_container)
    }

    companion object {
        private val TAG = SimpleSubExpandableItem::class.java.canonicalName
    }

}
