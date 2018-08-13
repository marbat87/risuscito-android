package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.materialize.holder.ColorHolder
import com.mikepenz.materialize.holder.StringHolder
import it.cammino.risuscito.R
import kotlinx.android.synthetic.main.checkable_row_item.view.*

class CheckableItem : AbstractItem<CheckableItem, CheckableItem.ViewHolder>() {

    var title: StringHolder? = null
        private set
    var page: StringHolder? = null
        private set
    var color: ColorHolder? = null
        private set
    var id: Int = 0
        private set

    fun withTitle(title: String): CheckableItem {
        this.title = StringHolder(title)
        return this
    }

    fun withTitle(@StringRes titleRes: Int): CheckableItem {
        this.title = StringHolder(titleRes)
        return this
    }

    fun withPage(page: String): CheckableItem {
        this.page = StringHolder(page)
        return this
    }

    fun withPage(@StringRes pageRes: Int): CheckableItem {
        this.page = StringHolder(pageRes)
        return this
    }

    fun withColor(color: String): CheckableItem {
        this.color = ColorHolder.fromColor(Color.parseColor(color))
        return this
    }

    fun withColor(@ColorRes colorRes: Int): CheckableItem {
        this.color = ColorHolder.fromColorRes(colorRes)
        return this
    }

    fun withId(id: Int): CheckableItem {
        this.id = id
        super.withIdentifier(id.toLong())
        return this
    }

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    override fun getType(): Int {
        return R.id.fastadapter_checkable_item_id
    }

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override fun getLayoutRes(): Int {
        return R.layout.checkable_row_item
    }

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param viewHolder the viewHolder of this item
     */
    override fun bindView(viewHolder: ViewHolder, payloads: List<Any>) {
        super.bindView(viewHolder, payloads)

        viewHolder.checkBox!!.isChecked = isSelected

        // set the text for the name
        StringHolder.applyTo(title, viewHolder.mTitle)
        // set the text for the description or hide
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

    /** our ViewHolder  */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var mTitle: TextView? = null
        var mPage: TextView? = null
        var checkBox: CheckBox? = null

        init {
            mTitle = view.text_title
            mPage = view.text_page
            checkBox = view.check_box
        }
    }

    class CheckBoxClickEvent : ClickEventHook<CheckableItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return (viewHolder as? CheckableItem.ViewHolder)?.checkBox
        }

        override fun onClick(
                v: View, position: Int, fastAdapter: FastAdapter<CheckableItem>, item: CheckableItem) {
            fastAdapter.getExtension<SelectExtension<CheckableItem>>(SelectExtension::class.java)!!.toggleSelection(position)
        }
    }
}
