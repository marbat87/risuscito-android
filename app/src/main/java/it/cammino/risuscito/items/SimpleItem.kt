package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
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
import kotlinx.android.synthetic.main.simple_row_item.view.*

fun simpleItem(block: SimpleItem.() -> Unit): SimpleItem = SimpleItem().apply(block)

@Suppress("unused")
class SimpleItem : AbstractItem<SimpleItem.ViewHolder>() {

    var title: StringHolder? = null
        private set
    var page: StringHolder? = null
        private set
    var source: StringHolder? = null
        private set
    var undecodedSource: String? = null
        private set
    var color: ColorHolder? = null
        private set
    var numSalmo: Int = 0
        private set
    private var selectedColor: ColorHolder? = null
    private var filter: String? = null
    var id: Int = 0
        private set

    fun withTitle(title: String): SimpleItem {
        this.title = StringHolder(title)
        return this
    }

    fun withTitle(@StringRes titleRes: Int): SimpleItem {
        this.title = StringHolder(titleRes)
        return this
    }

    fun withPage(page: String): SimpleItem {
        this.page = StringHolder(page)
        return this
    }

    fun withPage(@StringRes pageRes: Int): SimpleItem {
        this.page = StringHolder(pageRes)
        return this
    }

    fun withSource(src: String): SimpleItem {
        this.source = StringHolder(src)
        return this
    }

    fun withSource(@StringRes srcRes: Int): SimpleItem {
        this.source = StringHolder(srcRes)
        return this
    }

    fun withUndecodedSource(undecodedSource: String): SimpleItem {
        this.undecodedSource = undecodedSource
        return this
    }

    fun withColor(color: String): SimpleItem {
        this.color = ColorHolder.fromColor(Color.parseColor(color))
        return this
    }

    fun withColor(@ColorRes colorRes: Int): SimpleItem {
        this.color = ColorHolder.fromColorRes(colorRes)
        return this
    }

    fun withSelectedColor(selectedColor: String): SimpleItem {
        this.selectedColor = ColorHolder.fromColor(Color.parseColor(selectedColor))
        return this
    }

    fun withSelectedColor(@ColorInt selectedColor: Int): SimpleItem {
        this.selectedColor = ColorHolder.fromColor(selectedColor)
        return this
    }

    fun withSelectedColorRes(@ColorRes selectedColorRes: Int): SimpleItem {
        this.selectedColor = ColorHolder.fromColorRes(selectedColorRes)
        return this
    }

    fun withFilter(filter: String): SimpleItem {
        this.filter = filter
        return this
    }

    fun withId(id: Int): SimpleItem {
        this.id = id
        identifier = id.toLong()
        return this
    }

    fun withNumSalmo(numSalmo: String): SimpleItem {
        var numeroTemp = 0
        try {
            numeroTemp = Integer.valueOf(numSalmo.substring(0, 3))
        } catch (e: NumberFormatException) {
            Log.e(javaClass.name, e.localizedMessage, e)
        } catch (e: IndexOutOfBoundsException) {
            Log.e(javaClass.name, e.localizedMessage, e)
        }

        this.numSalmo = numeroTemp
        return this
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
            }
        } ?: StringHolder.applyTo(title, holder.mTitle)
        // set the text for the description or hide
        StringHolder.applyToOrHide(page, holder.mPage)
        ViewCompat.setBackground(
                holder.view,
                FastAdapterUIUtils.getSelectableBackground(
                        ctx,
                        ContextCompat.getColor(holder.itemView.context, R.color.ripple_color),
                        true))

        if (isSelected) {
            holder.mPage?.visibility = View.INVISIBLE
            holder.mPageSelected?.visibility = View.VISIBLE
            val bgShape = holder.mPageSelected?.background as? GradientDrawable
            bgShape?.setColor(selectedColor?.colorInt ?: Color.WHITE)
        } else {
            val bgShape = holder.mPage?.background as? GradientDrawable
            bgShape?.setColor(color?.colorInt ?: Color.WHITE)
            holder.mPage?.visibility = View.VISIBLE
            holder.mPageSelected?.visibility = View.INVISIBLE
        }

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
