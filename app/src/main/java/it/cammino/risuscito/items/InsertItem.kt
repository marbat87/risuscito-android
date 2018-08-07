package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.TextView
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.materialize.holder.ColorHolder
import com.mikepenz.materialize.holder.StringHolder
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import kotlinx.android.synthetic.main.row_item_to_insert.view.*

@Suppress("unused")
class InsertItem : AbstractItem<InsertItem, InsertItem.ViewHolder>() {

    var title: StringHolder? = null
        private set
    var page: StringHolder? = null
        private set
    var source: StringHolder? = null
        private set
    var color: ColorHolder? = null
        private set
    private var numSalmo: Int = 0
        private set
    private var normalizedTitle: String? = null
    private var filter: String? = null
    var id: Int = 0
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

    fun withColor(color: String): InsertItem {
        this.color = ColorHolder.fromColor(Color.parseColor(color))
        return this
    }

    fun withColor(@ColorRes colorRes: Int): InsertItem {
        this.color = ColorHolder.fromColorRes(colorRes)
        return this
    }

    fun withId(id: Int): InsertItem {
        this.id = id
        super.withIdentifier(id.toLong())
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

    fun withNormalizedTitle(normTitle: String): InsertItem {
        this.normalizedTitle = normTitle
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
    override fun getType(): Int {
        return R.id.fastadapter_insert_item_id
    }

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override fun getLayoutRes(): Int {
        return R.layout.row_item_to_insert
    }

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param viewHolder the viewHolder of this item
     */
    override fun bindView(viewHolder: ViewHolder, payloads: List<Any>) {
        super.bindView(viewHolder, payloads)

        //set the text for the name
        if (filter != null && !filter!!.isEmpty()) {
            val mPosition = normalizedTitle!!.indexOf(filter!!)
            if (mPosition >= 0) {
                val highlighted = title!!.text.toString().replace(("(?i)(" + title!!.text.toString().substring(mPosition, mPosition + filter!!.length) + ")").toRegex(), "<b>$1</b>")
                viewHolder.mTitle!!.text = LUtils.fromHtmlWrapper(highlighted)
            } else
                StringHolder.applyTo(title, viewHolder.mTitle)
        } else
            StringHolder.applyTo(title, viewHolder.mTitle)
        //set the text for the description or hide
        StringHolder.applyToOrHide(page, viewHolder.mPage)
        val bgShape = viewHolder.mPage!!.background as GradientDrawable
        bgShape.setColor(color!!.colorInt)

    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.mTitle!!.text = null
        holder.mPage!!.text = null
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