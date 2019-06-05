package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.materialize.holder.ColorHolder
import com.mikepenz.materialize.holder.StringHolder
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.Utility.helperSetColor
import it.cammino.risuscito.Utility.helperSetString
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

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    override val type: Int
        get() = R.id.fastadapter_insert_item_id

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override val layoutRes: Int
        get() = R.layout.row_item_to_insert

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param holder the viewHolder of this item
     */
    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        //set the text for the name
        filter?.let {
            if (it.isNotEmpty()) {
                val normalizedTitle = Utility.removeAccents(title?.getText(holder.itemView.context)
                        ?: "")
                val mPosition = normalizedTitle.toLowerCase().indexOf(it)
                if (mPosition >= 0) {
                    val stringTitle = title?.getText(holder.itemView.context)
                    val highlighted = StringBuilder(if (mPosition > 0) (stringTitle?.substring(0, mPosition)
                            ?: "") else "")
                            .append("<b>")
                            .append(stringTitle?.substring(mPosition, mPosition + it.length))
                            .append("</b>")
                            .append(stringTitle?.substring(mPosition + it.length))
                    holder.mTitle?.text = LUtils.fromHtmlWrapper(highlighted.toString())
                } else
                    StringHolder.applyTo(title, holder.mTitle)
            } else
                StringHolder.applyTo(title, holder.mTitle)
        } ?: StringHolder.applyTo(title, holder.mTitle)

        //set the text for the description or hide
        StringHolder.applyToOrHide(page, holder.mPage)
        val bgShape = holder.mPage?.background as? GradientDrawable
        bgShape?.setColor(color?.colorInt ?: Color.WHITE)

    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.mTitle?.text = null
        holder.mPage?.text = null
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    /**
     * our ViewHolder
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mTitle: TextView? = null
        var mPage: TextView? = null
        var mPreview: View? = null

        init {
            mTitle = view.text_title
            mPage = view.text_page
            mPreview = view.preview
        }
    }

}