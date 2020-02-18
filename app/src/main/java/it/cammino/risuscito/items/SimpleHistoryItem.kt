package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.IItemVHFactory
import com.mikepenz.fastadapter.items.BaseItem
import com.mikepenz.fastadapter.items.BaseItemFactory
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

class SimpleHistoryItem : BaseItem<SimpleHistoryItem.ViewHolder>() {

    override val type: Int
        get() = R.id.fastadapter_history_item_id

    override val factory: IItemVHFactory<ViewHolder> = SimpleHistoryItemFactory

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

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        // get the context
        val ctx = holder.itemView.context

        // set the text for the name
        StringHolder.applyTo(title, holder.mTitle)
        // set the text for the description or hide
        StringHolder.applyToOrHide(page, holder.mPage)

        @Suppress("DEPRECATION")
        holder.view.background = FastAdapterUIUtils.getSelectableBackground(
                ctx,
                ctx.themeColor(R.attr.colorSecondaryLight),
                true)

        val bgShape = holder.mPage?.background as? GradientDrawable
        bgShape?.setColor(color?.colorInt ?: Color.WHITE)
        holder.mPage?.isInvisible = isSelected
        holder.mPageSelected?.isVisible = isSelected
        val bgShapeSelected = holder.mPageSelected?.background as? GradientDrawable
        bgShapeSelected?.setColor(ctx.themeColor(R.attr.colorSecondary))

        holder.mId?.text = id.toString()

        if (timestamp != null) {
            // FORMATTO LA DATA IN BASE ALLA LOCALIZZAZIONE
            val df = DateFormat.getDateTimeInstance(
                    DateFormat.SHORT, DateFormat.MEDIUM, getSystemLocale(ctx.resources))
            val tempTimestamp: String

            val dateTimestamp = Date(java.lang.Long.parseLong(timestamp?.getText(ctx).toString()))
            tempTimestamp = if (df is SimpleDateFormat) {
                val pattern = df.toPattern().replace("y+".toRegex(), "yyyy")
                df.applyPattern(pattern)
                df.format(dateTimestamp)
            } else
                df.format(dateTimestamp)
            holder.mTimestamp?.text = tempTimestamp
            holder.mTimestamp?.isVisible = true
        } else
            holder.mTimestamp?.isVisible = false
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.mTitle?.text = null
        holder.mPage?.text = null
        holder.mId?.text = null
        holder.mTimestamp?.text = null
    }

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

        var mTitle: TextView? = null
        var mPage: TextView? = null
        var mPageSelected: View? = null
        var mTimestamp: TextView? = null
        var mId: TextView? = null

        init {
            mTitle = view.text_title
            mPage = view.text_page
            mPageSelected = view.selected_mark
            mTimestamp = view.text_timestamp
            mId = view.text_id_canto
        }
    }
}

object SimpleHistoryItemFactory : BaseItemFactory<SimpleHistoryItem.ViewHolder>() {

    override val type: Int
        get() = R.id.fastadapter_history_item_id

    override val layoutRes: Int
        get() = R.layout.row_item_history

    override fun getViewHolder(v: View): SimpleHistoryItem.ViewHolder {
        return SimpleHistoryItem.ViewHolder(v)
    }

}
