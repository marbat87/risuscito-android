package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import com.mikepenz.materialize.holder.ColorHolder
import com.mikepenz.materialize.holder.StringHolder
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.Utility.helperSetColor
import it.cammino.risuscito.Utility.helperSetString
import it.cammino.risuscito.utils.themeColor
import kotlinx.android.synthetic.main.simple_row_item.view.*

fun simpleItem(block: SimpleItem.() -> Unit): SimpleItem = SimpleItem().apply(block)

class SimpleItem : AbstractItem<SimpleItem.ViewHolder>() {

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

    var numSalmo: Int = 0
        private set
    var setNumSalmo: String? = null
        set(value) {
            var numeroTemp = 0
            try {
                numeroTemp = Integer.valueOf(value?.substring(0, 3) ?: "")
            } catch (e: NumberFormatException) {
                Log.e(javaClass.name, e.localizedMessage, e)
            } catch (e: IndexOutOfBoundsException) {
                Log.e(javaClass.name, e.localizedMessage, e)
            }
            numSalmo = numeroTemp
            field = value
        }

    var filter: String? = null

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
        get() = R.id.fastadapter_simple_item_id

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override val layoutRes: Int
        get() = R.layout.simple_row_item

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
        filter?.let {
            if (it.isNotEmpty()) {
                val normalizedTitle = Utility.removeAccents(title?.getText(ctx)
                        ?: "")
                val mPosition = normalizedTitle.toLowerCase().indexOf(it)
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
            }
        } ?: StringHolder.applyTo(title, holder.mTitle)
        // set the text for the description or hide
        StringHolder.applyToOrHide(page, holder.mPage)
        ViewCompat.setBackground(
                holder.view,
                FastAdapterUIUtils.getSelectableBackground(
                        ctx,
                        ContextCompat.getColor(ctx, R.color.ripple_color),
                        true))

        val bgShape = holder.mPage?.background as? GradientDrawable
        bgShape?.setColor(color?.colorInt ?: Color.WHITE)
        holder.mPage?.visibility = if (isSelected) View.INVISIBLE else View.VISIBLE
        holder.mPageSelected?.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        val bgShapeSelected = holder.mPageSelected?.background as? GradientDrawable
        bgShapeSelected?.setColor(ctx.themeColor(R.attr.colorSecondary))

        holder.mId?.text = id.toString()

        holder.itemView.setTag(com.mikepenz.fastadapter.R.id.fastadapter_item, id)
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.mTitle?.text = null
        holder.mPage?.text = null
        holder.mId?.text = null
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    /** our ViewHolder  */
    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

        var mTitle: TextView? = null
        var mPage: TextView? = null
        var mPageSelected: View? = null
        var mId: TextView? = null

        init {
            mTitle = view.text_title
            mPage = view.text_page
            mPageSelected = view.selected_mark
            mId = view.text_id_canto
        }
    }
}
