package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.IItemVHFactory
import com.mikepenz.fastadapter.items.BaseItem
import com.mikepenz.fastadapter.items.BaseItemFactory
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

class InsertItem : BaseItem<InsertItem.ViewHolder>() {

    override val type: Int
        get() = R.id.fastadapter_insert_item_id

    override val factory: IItemVHFactory<ViewHolder> = InsertItemFactory

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

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        // get the context
        val ctx = holder.itemView.context

        //set the text for the name
        filter?.let {
            if (it.isNotEmpty()) {
                val normalizedTitle = Utility.removeAccents(title?.getText(ctx)
                        ?: "")
                val mPosition = normalizedTitle.toLowerCase(getSystemLocale(ctx.resources)).indexOf(it)
                if (mPosition >= 0) {
                    val stringTitle = title?.getText(ctx)
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

object InsertItemFactory : BaseItemFactory<InsertItem.ViewHolder>() {

    override val type: Int
        get() = R.id.fastadapter_insert_item_id

    override val layoutRes: Int
        get() = R.layout.row_item_to_insert

    override fun getViewHolder(v: View): InsertItem.ViewHolder {
        return InsertItem.ViewHolder(v)
    }

}