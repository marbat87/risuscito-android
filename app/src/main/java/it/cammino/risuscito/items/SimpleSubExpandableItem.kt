package it.cammino.risuscito.items

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.IClickable
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import com.mikepenz.materialdrawer.holder.StringHolder
import it.cammino.risuscito.R
import kotlinx.android.synthetic.main.list_group_item.view.*

@Suppress("unused")
class SimpleSubExpandableItem : AbstractExpandableItem<SimpleSubExpandableItem.ViewHolder>(), IClickable<SimpleSubExpandableItem>, ISubItem<SimpleSubExpandableItem.ViewHolder> {

    var title: StringHolder? = null
        private set
    private var subTitle: StringHolder? = null
    private var totItems: Int = 0
    var position: Int = 0
        private set

    private var mOnClickListener: ((v: View?, adapter: IAdapter<SimpleSubExpandableItem>, item: SimpleSubExpandableItem, position: Int) -> Boolean)? = null

    override var onItemClickListener: ((v: View?, adapter: IAdapter<SimpleSubExpandableItem>, item: SimpleSubExpandableItem, position: Int) -> Boolean)? = { v: View?, adapter: IAdapter<SimpleSubExpandableItem>, item: SimpleSubExpandableItem, position: Int ->
        v?.let {
            if (!item.isExpanded) {
                ViewCompat.animate(it.group_indicator).rotation(180f).start()
            } else {
                ViewCompat.animate(it.group_indicator).rotation(0f).start()
            }
        }
        mOnClickListener?.invoke(v, adapter, item, position) ?: true
    }
        set(onClickListener) {
            this.mOnClickListener = onClickListener // on purpose
            field = onClickListener
        }

    override var onPreItemClickListener: ((v: View?, adapter: IAdapter<SimpleSubExpandableItem>, item: SimpleSubExpandableItem, position: Int) -> Boolean)? = null

    fun withTitle(title: String): SimpleSubExpandableItem {
        this.title = StringHolder(title)
        return this
    }

    fun withTitle(@StringRes titleRes: Int): SimpleSubExpandableItem {
        this.title = StringHolder(titleRes)
        return this
    }

    fun withSubTitle(subTitle: String): SimpleSubExpandableItem {
        this.subTitle = StringHolder(subTitle)
        return this
    }

    fun withSubTitle(@StringRes subTitleRes: Int): SimpleSubExpandableItem {
        this.subTitle = StringHolder(subTitleRes)
        return this
    }

    fun withPosition(position: Int): SimpleSubExpandableItem {
        this.position = position
        return this
    }

    fun witTotItems(totItems: Int): SimpleSubExpandableItem {
        this.totItems = totItems
        return this
    }

    override//this might not be true for your application
    var isSelectable: Boolean
        get() = false
        set(value) {
            super.isSelectable = value
        }

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    override val type: Int
        get() = R.id.fastadapter_expandable_item_id

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override val layoutRes: Int
        get() = R.layout.list_group_item

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param holder the viewHolder of this item
     */
    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        // get the context
        val ctx = holder.itemView.context

        // set the background for the item
        ViewCompat.setBackground(
                holder.view,
                FastAdapterUIUtils.getRippleDrawable(
                        ContextCompat.getColor(ctx, R.color.floating_background),
                        ContextCompat.getColor(ctx, R.color.ripple_color),
                        10))
        // set the text for the name
        val newTitle = "${title?.getText(holder.view.context)} ($totItems)"
        holder.mTitle?.text = newTitle
        StringHolder.applyToOrHide(subTitle, holder.mSubTitle)

        if (isExpanded)
            holder.mIndicator?.rotation = 0f
        else
            holder.mIndicator?.rotation = 180f
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.mTitle?.text = null
        holder.mSubTitle?.text = null
        // make sure all animations are stopped
        holder.mIndicator?.clearAnimation()
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    /** our ViewHolder  */
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        var mTitle: TextView? = null
        var mSubTitle: TextView? = null
        var mIndicator: ImageView? = null

        init {
            mTitle = view.group_title
            mSubTitle = view.group_subtitle
            mIndicator = view.group_indicator
        }
    }
}
