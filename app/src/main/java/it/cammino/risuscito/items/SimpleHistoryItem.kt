package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import com.mikepenz.fastadapter.ui.utils.StringHolder
import com.mikepenz.materialdrawer.holder.ColorHolder
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility.helperSetColor
import it.cammino.risuscito.Utility.helperSetString
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.utils.themeColor
import kotlinx.android.synthetic.main.row_item_history.view.*
import java.sql.Date
import java.text.DateFormat
import java.text.SimpleDateFormat

fun simpleHistoryItem(block: SimpleHistoryItem.() -> Unit): SimpleHistoryItem = SimpleHistoryItem().apply(block)

class SimpleHistoryItem : AbstractItem<SimpleHistoryItem.ViewHolder>() {

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

    var timestamp: StringHolder? = null
        private set
    var setTimestamp: Any? = null
        set(value) {
            timestamp = helperSetString(value)
        }

    var source: StringHolder? = null
        private set
    var setSource: Any? = null
        set(value) {
            source = helperSetString(value)
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

    override val type: Int
        get() = R.id.fastadapter_history_item_id

    override val layoutRes: Int
        get() = R.layout.row_item_history

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(var view: View) : FastAdapter.ViewHolder<SimpleHistoryItem>(view) {

        var mTitle: TextView? = null
        private var mPage: TextView? = null
        private var mPageSelected: View? = null
        private var mTimestamp: TextView? = null
        private var mId: TextView? = null

        override fun bindView(item: SimpleHistoryItem, payloads: List<Any>) {
            val ctx = itemView.context

            StringHolder.applyTo(item.title, mTitle)
            StringHolder.applyToOrHide(item.page, mPage)

            view.background = FastAdapterUIUtils.getSelectableBackground(
                    ctx,
                    ctx.themeColor(R.attr.colorSecondaryLight),
                    true)

            val bgShape = mPage?.background as? GradientDrawable
            bgShape?.setColor(item.color?.colorInt ?: Color.WHITE)
            mPage?.isInvisible = item.isSelected
            mPageSelected?.isVisible = item.isSelected
            val bgShapeSelected = mPageSelected?.background as? GradientDrawable
            bgShapeSelected?.setColor(ctx.themeColor(R.attr.colorSecondary))

            mId?.text = item.id.toString()

            if (item.timestamp != null) {
                // FORMATTO LA DATA IN BASE ALLA LOCALIZZAZIONE
                val df = DateFormat.getDateTimeInstance(
                        DateFormat.SHORT, DateFormat.MEDIUM, getSystemLocale(ctx.resources))
                val tempTimestamp: String

                val dateTimestamp = Date(java.lang.Long.parseLong(item.timestamp?.getText(ctx).toString()))
                tempTimestamp = if (df is SimpleDateFormat) {
                    val pattern = df.toPattern().replace("y+".toRegex(), "yyyy")
                    df.applyPattern(pattern)
                    df.format(dateTimestamp)
                } else
                    df.format(dateTimestamp)
                mTimestamp?.text = tempTimestamp
                mTimestamp?.isVisible = true
            } else
                mTimestamp?.isVisible = false
        }

        override fun unbindView(item: SimpleHistoryItem) {
            mTitle?.text = null
            mPage?.text = null
            mId?.text = null
            mTimestamp?.text = null
        }

        init {
            mTitle = view.text_title
            mPage = view.text_page
            mPageSelected = view.selected_mark
            mTimestamp = view.text_timestamp
            mId = view.text_id_canto
        }
    }
}
