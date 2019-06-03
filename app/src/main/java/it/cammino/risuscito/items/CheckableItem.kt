package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.materialize.holder.ColorHolder
import com.mikepenz.materialize.holder.StringHolder
import it.cammino.risuscito.R
import kotlinx.android.synthetic.main.checkable_row_item.view.*

class CheckableItem : AbstractItem<CheckableItem.ViewHolder>() {

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
        identifier = id.toLong()
        return this
    }

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

        holder.checkBox?.isChecked = isSelected

        // set the text for the name
        StringHolder.applyTo(title, holder.mTitle)
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

}
