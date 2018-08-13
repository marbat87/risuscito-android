package it.cammino.risuscito.items

import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.TextView
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter_extensions.drag.IExtendedDraggable
import com.mikepenz.fastadapter_extensions.swipe.ISwipeable
import com.mikepenz.fastadapter_extensions.utilities.DragDropUtil
import com.mikepenz.materialdrawer.holder.StringHolder
import it.cammino.risuscito.R
import kotlinx.android.synthetic.main.swipeable_item.view.*

@Suppress("unused")
class SwipeableItem : AbstractItem<SwipeableItem, SwipeableItem.ViewHolder>(), ISwipeable<SwipeableItem, IItem<*, *>>, IExtendedDraggable<Any, RecyclerView.ViewHolder, IItem<*, *>> {
    lateinit var name: StringHolder

    private var swipedDirection: Int = 0
    private var swipedAction: Runnable? = null
    private var swipeable = true
    private var mHelper: ItemTouchHelper? = null

    fun withName(Name: String): SwipeableItem {
        this.name = StringHolder(Name)
        return this
    }

    fun withName(@StringRes NameRes: Int): SwipeableItem {
        this.name = StringHolder(NameRes)
        return this
    }

    override fun isSwipeable(): Boolean {
        return swipeable
    }

    override fun withIsSwipeable(swipeable: Boolean): SwipeableItem {
        this.swipeable = swipeable
        return this
    }

    fun setSwipedDirection(swipedDirection: Int) {
        this.swipedDirection = swipedDirection
    }

    fun setSwipedAction(action: Runnable) {
        this.swipedAction = action
    }

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    override fun getType(): Int {
        return R.id.fastadapter_swipable_item_id
    }

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override fun getLayoutRes(): Int {
        return R.layout.swipeable_item
    }

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param viewHolder the viewHolder of this item
     */
    override fun bindView(viewHolder: ViewHolder, payloads: List<Any>) {
        super.bindView(viewHolder, payloads)

        //set the text for the name
        StringHolder.applyTo(name, viewHolder.name)
        //set the text for the description or hide

        viewHolder.swipeResultContent!!.visibility = if (swipedDirection != 0) View.VISIBLE else View.GONE
        viewHolder.itemContent!!.visibility = if (swipedDirection != 0) View.GONE else View.VISIBLE

        var swipedAction: CharSequence? = null
        var swipedText: CharSequence? = null
        if (swipedDirection != 0) {
            swipedAction = viewHolder.itemView.context.getString(android.R.string.cancel)
            swipedText = viewHolder.itemView.context.getString(R.string.generic_removed, name.text)
            viewHolder.swipeResultContent!!.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.context, if (swipedDirection == ItemTouchHelper.LEFT) R.color.md_red_900 else R.color.md_red_900))
        }
        viewHolder.swipedAction!!.text = swipedAction ?: ""
        viewHolder.swipedText!!.text = swipedText ?: ""
        viewHolder.swipedActionRunnable = this.swipedAction

        DragDropUtil.bindDragHandle(viewHolder, this)
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.name!!.text = null
        holder.swipedAction!!.text = null
        holder.swipedText!!.text = null
        holder.swipedActionRunnable = null
        holder.mDragHandler = null
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    override fun withTouchHelper(itemTouchHelper: ItemTouchHelper): SwipeableItem {
        mHelper = itemTouchHelper
        return this
    }

    override fun getTouchHelper(): ItemTouchHelper? {
        return mHelper
    }

    override fun getDragView(viewHolder: RecyclerView.ViewHolder): View? {
        return (viewHolder as ViewHolder).mDragHandler
    }

    override fun isDraggable(): Boolean {
        return true
    }

    override fun withIsDraggable(draggable: Boolean): Any? {
        return null
    }

    /**
     * our ViewHolder
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var name: TextView? = null
        internal var swipeResultContent: View? = null
        internal var itemContent: View? = null
        internal var swipedText: TextView? = null
        internal var swipedAction: TextView? = null
        internal var mDragHandler: View? = null

        internal var swipedActionRunnable: Runnable? = null

        init {
            name = view.swipeable_text1
            swipeResultContent = view.swipe_result_content
            itemContent = view.container
            swipedText = view.swiped_text
            swipedAction = view.swiped_action
            mDragHandler = view.drag_image
            swipedAction!!.setOnClickListener {
                if (swipedActionRunnable != null) {
                    swipedActionRunnable!!.run()
                }
            }
        }
    }
}