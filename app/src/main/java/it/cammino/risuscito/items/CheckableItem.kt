package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import com.mikepenz.fastadapter.ui.utils.StringHolder
import com.mikepenz.materialdrawer.holder.ColorHolder
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.Utility.helperSetColor
import it.cammino.risuscito.Utility.helperSetString
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.utils.themeColor
import kotlinx.android.synthetic.main.checkable_row_item.view.*

fun checkableItem(block: CheckableItem.() -> Unit): CheckableItem = CheckableItem().apply(block)

class CheckableItem : AbstractItem<CheckableItem.ViewHolder>() {

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
    var color: ColorHolder? = null
        private set
    var setColor: Any? = null
        set(value) {
            color = helperSetColor(value)
        }
    var id: Int = 0
        set(value) {
            identifier = value.toLong()
            field = value
        }

    var filter: String? = null

    override val type: Int
        get() = R.id.fastadapter_checkable_item_id

    override val layoutRes: Int
        get() = R.layout.checkable_row_item

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(private var view: View) : FastAdapter.ViewHolder<CheckableItem>(view) {

        var mTitle: TextView? = null
        private var mPage: TextView? = null
        var checkBox: CheckBox? = null

        override fun bindView(item: CheckableItem, payloads: List<Any>) {
            // get the context
            val ctx = itemView.context

            checkBox?.isChecked = item.isSelected
            view.background = FastAdapterUIUtils.getSelectableBackground(
                    ctx,
                    ctx.themeColor(R.attr.colorSecondaryLight),
                    true)
            // set the text for the name
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

            // set the text for the description or hide
            StringHolder.applyToOrHide(item.page, mPage)

            val bgShape = mPage?.background as? GradientDrawable
            bgShape?.setColor(item.color?.colorInt ?: Color.WHITE)
        }

        override fun unbindView(item: CheckableItem) {
            mTitle?.text = null
            mPage?.text = null
        }

        init {
            mTitle = view.text_title
            mPage = view.text_page
            checkBox = view.check_box
        }
    }

}
