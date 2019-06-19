package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import com.mikepenz.materialize.holder.ColorHolder
import com.mikepenz.materialize.holder.StringHolder
import com.mikepenz.materialize.util.UIUtils
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

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    override val type: Int
        get() = R.id.fastadapter_history_item_id

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override val layoutRes: Int
        get() = R.layout.row_item_history

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param holder the viewHolder of this item
     */
    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        // get the context
        val ctx = holder.itemView.context

        // set the text for the name
        StringHolder.applyTo(title, holder.mTitle)
        // set the text for the description or hide
        StringHolder.applyToOrHide(page, holder.mPage)

        @Suppress("DEPRECATION")
        UIUtils.setBackground(
                holder.view,
                FastAdapterUIUtils.getSelectableBackground(
                        ctx,
                        ctx.themeColor(R.attr.colorSecondaryLight),
                        true))

        val bgShape = holder.mPage?.background as? GradientDrawable
        bgShape?.setColor(color?.colorInt ?: Color.WHITE)
        holder.mPage?.visibility = if (isSelected) View.INVISIBLE else View.VISIBLE
        holder.mPageSelected?.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        val bgShapeSelected = holder.mPageSelected?.background as? GradientDrawable
        bgShapeSelected?.setColor(ctx.themeColor(R.attr.colorSecondary))

        holder.mId?.text = id.toString()

        if (timestamp != null) {
            // FORMATTO LA DATA IN BASE ALLA LOCALIZZAZIONE
            val df = DateFormat.getDateTimeInstance(
                    DateFormat.SHORT, DateFormat.MEDIUM, getSystemLocale(ctx.resources))
            val tempTimestamp: String

            val dateTimestamp = Date(java.lang.Long.parseLong(timestamp?.text.toString()))
            tempTimestamp = if (df is SimpleDateFormat) {
                val pattern = df.toPattern().replace("y+".toRegex(), "yyyy")
                df.applyPattern(pattern)
                df.format(dateTimestamp)
            } else
                df.format(dateTimestamp)
            holder.mTimestamp?.text = tempTimestamp
            holder.mTimestamp?.visibility = View.VISIBLE
        } else
            holder.mTimestamp?.visibility = View.GONE
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.mTitle?.text = null
        holder.mPage?.text = null
        holder.mId?.text = null
        holder.mTimestamp?.text = null
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    /** our ViewHolder  */
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
