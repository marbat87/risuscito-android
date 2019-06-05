package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.materialize.holder.ColorHolder
import com.mikepenz.materialize.holder.StringHolder
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import kotlinx.android.synthetic.main.row_item_to_insert.view.*

fun insertItem(block: InsertItem.() -> Unit): InsertItem = InsertItem().apply(block)

@Suppress("unused")
class InsertItem : AbstractItem<InsertItem.ViewHolder>() {

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
    private var numSalmo: Int = 0
    private var filter: String? = null
    var id: Int = 0
        private set
    var consegnato: Int = 0
        private set

    fun withTitle(title: String): InsertItem {
        this.title = StringHolder(title)
        return this
    }

    fun withTitle(@StringRes titleRes: Int): InsertItem {
        this.title = StringHolder(titleRes)
        return this
    }

    fun withPage(page: String): InsertItem {
        this.page = StringHolder(page)
        return this
    }

    fun withPage(@StringRes pageRes: Int): InsertItem {
        this.page = StringHolder(pageRes)
        return this
    }

    fun withSource(src: String): InsertItem {
        this.source = StringHolder(src)
        return this
    }

    fun withSource(@StringRes srcRes: Int): InsertItem {
        this.source = StringHolder(srcRes)
        return this
    }

    fun withUndecodedSource(undecodedSource: String): InsertItem {
        this.undecodedSource = undecodedSource
        return this
    }

    fun withColor(color: String): InsertItem {
        this.color = ColorHolder.fromColor(Color.parseColor(color))
        return this
    }

    fun withColor(@ColorRes colorRes: Int): InsertItem {
        this.color = ColorHolder.fromColorRes(colorRes)
        return this
    }

    fun withConsegnato(consegnato: Int): InsertItem {
        this.consegnato = consegnato
        return this
    }

    fun withId(id: Int): InsertItem {
        this.id = id
        identifier = id.toLong()
        return this
    }

    fun withNumSalmo(numSalmo: String): InsertItem {
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

    fun withFilter(filter: String): InsertItem {
        this.filter = filter
        return this
    }

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