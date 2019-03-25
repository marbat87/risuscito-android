package it.cammino.risuscito.items

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.IExpandable
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import com.mikepenz.materialize.holder.ColorHolder
import com.mikepenz.materialize.holder.StringHolder
import it.cammino.risuscito.R
import kotlinx.android.synthetic.main.simple_sub_item.view.*

class SimpleSubItem : AbstractExpandableItem<SimpleSubItem.ViewHolder>(), IExpandable<SimpleSubItem.ViewHolder> {

    var title: StringHolder? = null
        private set
    var page: StringHolder? = null
        private set
    var source: StringHolder? = null
        private set
    var color: ColorHolder? = null
        private set
    var id: Int = 0
        private set
    private var isHasDivider = false

    private var createContextMenuListener: View.OnCreateContextMenuListener? = null

    fun withTitle(title: String): SimpleSubItem {
        this.title = StringHolder(title)
        return this
    }

    fun withTitle(@StringRes titleRes: Int): SimpleSubItem {
        this.title = StringHolder(titleRes)
        return this
    }

    fun withPage(page: String): SimpleSubItem {
        this.page = StringHolder(page)
        return this
    }

    fun withPage(@StringRes pageRes: Int): SimpleSubItem {
        this.page = StringHolder(pageRes)
        return this
    }

    fun withSource(src: String): SimpleSubItem {
        this.source = StringHolder(src)
        return this
    }

    fun withSource(@StringRes srcRes: Int): SimpleSubItem {
        this.source = StringHolder(srcRes)
        return this
    }

    fun withColor(color: String): SimpleSubItem {
        this.color = ColorHolder.fromColor(Color.parseColor(color))
        return this
    }

    fun withColor(@ColorRes colorRes: Int): SimpleSubItem {
        this.color = ColorHolder.fromColorRes(colorRes)
        return this
    }

    fun withId(id: Int): SimpleSubItem {
        this.id = id
        return this
    }

    fun withContextMenuListener(listener: View.OnCreateContextMenuListener): SimpleSubItem {
        this.createContextMenuListener = listener
        return this
    }

    fun withHasDivider(hasDivider: Boolean): SimpleSubItem {
        this.isHasDivider = hasDivider
        return this
    }

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    override val type: Int
        get() = R.id.fastadapter_sub_item_id

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override val layoutRes: Int
        get() = R.layout.simple_sub_item

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

        ViewCompat.setBackground(
                holder.view,
                FastAdapterUIUtils.getSelectableBackground(
                        ctx,
                        ContextCompat.getColor(holder.itemView.context, R.color.ripple_color),
                        true))

        if (isSelected) {
            holder.mPage!!.visibility = View.INVISIBLE
            holder.mPageSelected!!.visibility = View.VISIBLE
        } else {
            val bgShape = holder.mPage!!.background as GradientDrawable
            bgShape.setColor(color!!.colorInt)
            holder.mPage!!.visibility = View.VISIBLE
            holder.mPageSelected!!.visibility = View.INVISIBLE
        }

        holder.mId!!.text = id.toString()

        holder.mItemDivider!!.visibility = if (isHasDivider) View.VISIBLE else View.INVISIBLE

        if (createContextMenuListener != null) {
            (holder.itemView.context as Activity).registerForContextMenu(holder.itemView)
            holder.itemView.setOnCreateContextMenuListener(createContextMenuListener)
        }
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.mTitle!!.text = null
        holder.mPage!!.text = null
        holder.mId!!.text = null
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    /**
     * our ViewHolder
     */
    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

        internal var mTitle: TextView? = null
        internal var mPage: TextView? = null
        internal var mPageSelected: View? = null
        internal var mId: TextView? = null
        internal var mItemDivider: View? = null

        init {
            mTitle = view.text_title
            mPage = view.text_page
            mPageSelected = view.selected_mark
            mId = view.text_id_canto
            mItemDivider = view.item_divider
        }
    }
}
