package it.cammino.risuscito.items

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.mikepenz.fastadapter.IClickable
import com.mikepenz.fastadapter.IExpandable
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem
import com.mikepenz.materialize.holder.ColorHolder
import com.mikepenz.materialize.holder.StringHolder
import it.cammino.risuscito.R
import kotlinx.android.synthetic.main.simple_sub_item.view.*

@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")
class SimpleSubItem<Parent> : AbstractExpandableItem<Parent, SimpleSubItem.ViewHolder, SimpleSubItem<Parent>>() where Parent : IItem<*, *>, Parent : IExpandable<*, *>, Parent : ISubItem<*, *>, Parent : IClickable<*> {

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

    fun withTitle(title: String): SimpleSubItem<Parent> {
        this.title = StringHolder(title)
        return this
    }

    fun withTitle(@StringRes titleRes: Int): SimpleSubItem<Parent> {
        this.title = StringHolder(titleRes)
        return this
    }

    fun withPage(page: String): SimpleSubItem<Parent> {
        this.page = StringHolder(page)
        return this
    }

    fun withPage(@StringRes pageRes: Int): SimpleSubItem<Parent> {
        this.page = StringHolder(pageRes)
        return this
    }

    fun withSource(src: String): SimpleSubItem<Parent> {
        this.source = StringHolder(src)
        return this
    }

    fun withSource(@StringRes srcRes: Int): SimpleSubItem<Parent> {
        this.source = StringHolder(srcRes)
        return this
    }

    fun withColor(color: String): SimpleSubItem<Parent> {
        this.color = ColorHolder.fromColor(Color.parseColor(color))
        return this
    }

    fun withColor(@ColorRes colorRes: Int): SimpleSubItem<Parent> {
        this.color = ColorHolder.fromColorRes(colorRes)
        return this
    }

    fun withId(id: Int): SimpleSubItem<Parent> {
        this.id = id
        return this
    }

    fun withContextMenuListener(listener: View.OnCreateContextMenuListener): SimpleSubItem<Parent> {
        this.createContextMenuListener = listener
        return this
    }

    fun withHasDivider(hasDivider: Boolean): SimpleSubItem<Parent> {
        this.isHasDivider = hasDivider
        return this
    }

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    override fun getType(): Int {
        return R.id.fastadapter_sub_item_id
    }

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override fun getLayoutRes(): Int {
        return R.layout.simple_sub_item
    }

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param viewHolder the viewHolder of this item
     */
    override fun bindView(viewHolder: ViewHolder, payloads: List<Any>) {
        super.bindView(viewHolder, payloads)

        // get the context
        val ctx = viewHolder.itemView.context

        // set the text for the name
        StringHolder.applyTo(title, viewHolder.mTitle)
        // set the text for the description or hide
        StringHolder.applyToOrHide(page, viewHolder.mPage)

        ViewCompat.setBackground(
                viewHolder.view,
                FastAdapterUIUtils.getSelectableBackground(
                        ctx,
                        ContextCompat.getColor(viewHolder.itemView.context, R.color.ripple_color),
                        true))

        if (isSelected) {
            viewHolder.mPage!!.visibility = View.INVISIBLE
            viewHolder.mPageSelected!!.visibility = View.VISIBLE
        } else {
            val bgShape = viewHolder.mPage!!.background as GradientDrawable
            bgShape.setColor(color!!.colorInt)
            viewHolder.mPage!!.visibility = View.VISIBLE
            viewHolder.mPageSelected!!.visibility = View.INVISIBLE
        }

        viewHolder.mId!!.text = id.toString()

        viewHolder.mItemDivider!!.visibility = if (isHasDivider) View.VISIBLE else View.INVISIBLE

        if (createContextMenuListener != null) {
            (viewHolder.itemView.context as Activity).registerForContextMenu(viewHolder.itemView)
            viewHolder.itemView.setOnCreateContextMenuListener(createContextMenuListener)
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
