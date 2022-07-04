package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.mikepenz.fastadapter.FastAdapter
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

    var isHasDivider = false

    override val type: Int
        get() = R.id.fastadapter_sub_item_id

    override val layoutRes: Int
        get() = R.layout.simple_row_item

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<SimpleSubItem>(view) {

        private var mTitle: TextView? = null
        private var mPage: TextView? = null
        private var mPageSelected: View? = null
        private var mId: TextView? = null

        override fun bindView(item: SimpleSubItem, payloads: List<Any>) {

            StringHolder.applyTo(item.title, mTitle)
            StringHolder.applyToOrHide(item.page, mPage)

            val bgShape = mPage?.background as? GradientDrawable
            bgShape?.setColor(item.color)
            mPage?.isVisible = true
            mPageSelected?.isVisible = false

            mId?.text = item.id.toString()

        }

        override fun unbindView(item: SimpleSubItem) {
            mTitle?.text = null
            mPage?.text = null
            mId?.text = null
        }

        init {
            mTitle = view.findViewById(R.id.text_title)
            mPage = view.findViewById(R.id.text_page)
            mPageSelected = view.findViewById(R.id.selected_mark)
            mId = view.findViewById(R.id.text_id_canto)
        }
    }

}
