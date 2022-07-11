package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.IExpandable
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.risuscito.R
import it.cammino.risuscito.utils.Utility.helperSetColor
import it.cammino.risuscito.utils.Utility.helperSetString

fun simpleSubItem(block: SimpleSubItem.() -> Unit): SimpleSubItem = SimpleSubItem().apply(block)

class SimpleSubItem : AbstractExpandableItem<SimpleSubItem.ViewHolder>(),
    IExpandable<SimpleSubItem.ViewHolder> {

    var title: StringHolder? = null
        private set
    var setTitle: Any? = null
        set(value) {
            title = helperSetString(value)
        }

    var page: StringHolder? = null
        private set
    var setPage: Any? = null
        set(value) {
            page = helperSetString(value)
        }

    var source: StringHolder? = null
        private set
    var setSource: Any? = null
        set(value) {
            source = helperSetString(value)
        }

    var color: Int = Color.WHITE
        private set
    var setColor: String? = null
        set(value) {
            color = helperSetColor(value)
        }

    var id: Int = 0

    override val type: Int
        get() = R.id.fastadapter_sub_item_id

    override val layoutRes: Int
        get() = R.layout.simple_row_item

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param holder the viewHolder of this item
     */
    override fun bindView(holder: ViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)

        //set the background for the item
        holder.view.clearAnimation()
        StringHolder.applyTo(title, holder.mTitle)
        StringHolder.applyToOrHide(page, holder.mPage)

        val bgShape = holder.mPage.background as? GradientDrawable
        bgShape?.setColor(color)
        holder.mPage.isVisible = true
        holder.mPageSelected.isVisible = false

        holder.mId.text = id.toString()
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.mTitle.text = null
        holder.mPage.text = null
        holder.mId.text = null
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var mTitle: TextView = view.findViewById(R.id.text_title)
        var mPage: TextView = view.findViewById(R.id.text_page)
        var mPageSelected: View = view.findViewById(R.id.selected_mark)
        var mId: TextView = view.findViewById(R.id.text_id_canto)
    }

}
