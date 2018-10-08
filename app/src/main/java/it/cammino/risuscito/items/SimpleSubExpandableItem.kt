package it.cammino.risuscito.items

import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mikepenz.fastadapter.IExpandable
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem
import com.mikepenz.fastadapter.listeners.OnClickListener
import com.mikepenz.materialdrawer.holder.StringHolder
import it.cammino.risuscito.R
import kotlinx.android.synthetic.main.list_group_item.view.*

@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA", "unused")
class SimpleSubExpandableItem<Parent, SubItem> : AbstractExpandableItem<SimpleSubExpandableItem<Parent, SubItem>, SimpleSubExpandableItem.ViewHolder, SubItem>() where Parent : IItem<*, *>, Parent : IExpandable<*, *>, SubItem : IItem<*, *>, SubItem : ISubItem<*, *> {

    var title: StringHolder? = null
        private set
    private var subTitle: StringHolder? = null

    private var mOnClickListener: OnClickListener<SimpleSubExpandableItem<Parent, SubItem>>? = null
    // we define a clickListener in here so we can directly animate
    private val onClickListener = OnClickListener<SimpleSubExpandableItem<Parent, SubItem>> { v, adapter, item, position ->
        if (item.subItems != null) {
            if (!item.isExpanded) {
                ViewCompat.animate(v!!.group_indicator).rotation(180f).start()
            } else {
                ViewCompat.animate(v!!.group_indicator).rotation(0f).start()
            }

            return@OnClickListener mOnClickListener == null || mOnClickListener!!.onClick(v, adapter, item, position)
        }

        mOnClickListener != null && mOnClickListener!!.onClick(v, adapter, item, position)
    }

    fun withTitle(title: String): SimpleSubExpandableItem<Parent, SubItem> {
        this.title = StringHolder(title)
        return this
    }

    fun withTitle(@StringRes titleRes: Int): SimpleSubExpandableItem<Parent, SubItem> {
        this.title = StringHolder(titleRes)
        return this
    }

    fun withSubTitle(subTitle: String): SimpleSubExpandableItem<Parent, SubItem> {
        this.subTitle = StringHolder(subTitle)
        return this
    }

    fun withSubTitle(@StringRes subTitleRes: Int): SimpleSubExpandableItem<Parent, SubItem> {
        this.subTitle = StringHolder(subTitleRes)
        return this
    }

    fun getOnClickListener(): OnClickListener<SimpleSubExpandableItem<Parent, SubItem>>? {
        return mOnClickListener
    }

    fun withOnClickListener(
            mOnClickListener: OnClickListener<SimpleSubExpandableItem<Parent, SubItem>>): SimpleSubExpandableItem<Parent, SubItem> {
        this.mOnClickListener = mOnClickListener
        return this
    }

    /**
     * we overwrite the item specific click listener so we can automatically animate within the item
     *
     * @return the FastAdapter.OnClickListener
     */
    override fun getOnItemClickListener(): OnClickListener<SimpleSubExpandableItem<Parent, SubItem>> {
        return onClickListener
    }

    override fun isSelectable(): Boolean {
        // this might not be true for your application
        return subItems == null
    }

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    override fun getType(): Int {
        return R.id.fastadapter_expandable_item_id
    }

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override fun getLayoutRes(): Int {
        return R.layout.list_group_item
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

        // set the background for the item
        ViewCompat.setBackground(
                viewHolder.view,
                FastAdapterUIUtils.getRippleDrawable(
                        //            color.getColorInt(), ContextCompat.getColor(ctx, R.color.ripple_color),
                        // 10));
                        ContextCompat.getColor(ctx, R.color.floating_background),
                        ContextCompat.getColor(ctx, R.color.ripple_color),
                        10))
        // set the text for the name
        StringHolder.applyTo(title, viewHolder.mTitle)
        StringHolder.applyToOrHide(subTitle, viewHolder.mSubTitle)

        if (isExpanded)
            viewHolder.mIndicator!!.rotation = 0f
        else
            viewHolder.mIndicator!!.rotation = 180f
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.mTitle!!.text = null
        holder.mSubTitle!!.text = null
        // make sure all animations are stopped
        holder.mIndicator!!.clearAnimation()
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
