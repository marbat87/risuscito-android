package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import com.mikepenz.materialdrawer.holder.ColorHolder
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.Utility.helperSetColor
import it.cammino.risuscito.Utility.helperSetString
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import kotlinx.android.synthetic.main.row_item_to_insert.view.*

fun insertItem(block: InsertItem.() -> Unit): InsertItem = InsertItem().apply(block)

class InsertItem : AbstractItem<InsertItem.ViewHolder>() {

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
    var undecodedSource: String? = null
    var color: ColorHolder? = null
        private set
    var setColor: Any? = null
        set(value) {
            color = helperSetColor(value)
        }
    var filter: String? = null
    var id: Int = 0
        set(value) {
            identifier = value.toLong()
            field = value
        }
    var consegnato: Int = 0

    override val type: Int
        get() = R.id.fastadapter_insert_item_id

    override val layoutRes: Int
        get() = R.layout.row_item_to_insert

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<InsertItem>(view) {
        var mTitle: TextView? = null
        var mPage: TextView? = null
        var mPreview: View? = null

        override fun bindView(item: InsertItem, payloads: List<Any>) {
            // get the context
            val ctx = itemView.context

            //set the text for the name
            item.filter?.let {
                if (it.isNotEmpty()) {
                    val normalizedTitle = Utility.removeAccents(item.title?.getText(ctx)
                            ?: "")
                    val mPosition = normalizedTitle.toLowerCase(getSystemLocale(ctx.resources)).indexOf(it)
                    if (mPosition >= 0) {
                        val stringTitle = item.title?.getText(ctx)
                        val highlighted = StringBuilder(if (mPosition > 0) (stringTitle?.substring(0, mPosition)
                                ?: "") else "")
                                .append("<b>")
                                .append(stringTitle?.substring(mPosition, mPosition + it.length))
                                .append("</b>")
                                .append(stringTitle?.substring(mPosition + it.length))
                        mTitle?.text = LUtils.fromHtmlWrapper(highlighted.toString())
                    } else
                        StringHolder.applyTo(item.title, mTitle)
                } else
                    StringHolder.applyTo(item.title, mTitle)
            } ?: StringHolder.applyTo(item.title, mTitle)

            //set the text for the description or hide
            StringHolder.applyToOrHide(item.page, mPage)
            val bgShape = mPage?.background as? GradientDrawable
            bgShape?.setColor(item.color?.colorInt ?: Color.WHITE)
        }

        override fun unbindView(item: InsertItem) {
            mTitle?.text = null
            mPage?.text = null
        }

        init {
            mTitle = view.text_title
            mPage = view.text_page
            mPreview = view.preview
        }
    }

}