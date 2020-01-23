package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
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

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    override val type: Int
        get() = R.id.fastadapter_checkable_item_id

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override val layoutRes: Int
        get() = R.layout.checkable_row_item

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param holder the viewHolder of this item
     */
    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        // get the context
        val ctx = holder.itemView.context

        holder.checkBox?.isChecked = isSelected
        ViewCompat.setBackground(
                holder.view,
                FastAdapterUIUtils.getSelectableBackground(
                        ctx,
                        ctx.themeColor(R.attr.colorSecondaryLight),
                        true))
        // set the text for the name
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

        // set the text for the description or hide
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

    /** our ViewHolder  */
    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

        var mTitle: TextView? = null
        var mPage: TextView? = null
        var checkBox: CheckBox? = null

        init {
            mTitle = view.text_title
            mPage = view.text_page
            checkBox = view.check_box
        }
    }

}
