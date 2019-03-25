package it.cammino.risuscito.items

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.drag.IExtendedDraggable
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.swipe.ISwipeable
import com.mikepenz.fastadapter.utils.DragDropUtil
import com.mikepenz.materialdrawer.holder.StringHolder
import it.cammino.risuscito.R
import kotlinx.android.synthetic.main.swipeable_item.view.*

@Suppress("unused")
class SwipeableItem : AbstractItem<SwipeableItem.ViewHolder>(), ISwipeable, IExtendedDraggable<RecyclerView.ViewHolder> {
    lateinit var name: StringHolder

    private var swipedDirection: Int = 0
    private var swipedAction: Runnable? = null
    private var swipeable = true
    override var touchHelper: ItemTouchHelper? = null

    fun withName(Name: String): SwipeableItem {
        this.name = StringHolder(Name)
        return this
    }

    fun withName(@StringRes NameRes: Int): SwipeableItem {
        this.name = StringHolder(NameRes)
        return this
    }

    fun withIsSwipeable(swipeable: Boolean): SwipeableItem {
        this.swipeable = swipeable
        return this
    }

    fun setSwipedDirection(swipedDirection: Int) {
        this.swipedDirection = swipedDirection
    }

    fun setSwipedAction(action: Runnable?) {
        this.swipedAction = action
    }

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    override val type: Int
        get() = R.id.fastadapter_swipable_item_id

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override val layoutRes: Int
        get() = R.layout.swipeable_item

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param holder the viewHolder of this item
     */
    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        //set the text for the name
        StringHolder.applyTo(name, holder.name)
        //set the text for the description or hide

        holder.swipeResultContent!!.visibility = if (swipedDirection != 0) View.VISIBLE else View.GONE
        holder.itemContent!!.visibility = if (swipedDirection != 0) View.GONE else View.VISIBLE

        var swipedAction: CharSequence? = null
        var swipedText: CharSequence? = null
        if (swipedDirection != 0) {
            swipedAction = holder.itemView.context.getString(android.R.string.cancel)
            swipedText = holder.itemView.context.getString(R.string.generic_removed, name.text)
            holder.swipeResultContent!!.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, if (swipedDirection == ItemTouchHelper.LEFT) R.color.md_red_900 else R.color.md_red_900))
        }
        holder.swipedAction!!.text = swipedAction ?: ""
        holder.swipedText!!.text = swipedText ?: ""
        holder.swipedActionRunnable = this.swipedAction

        DragDropUtil.bindDragHandle(holder, this)
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.name!!.text = null
        holder.swipedAction!!.text = null
        holder.swipedText!!.text = null
        holder.swipedActionRunnable = null
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    override fun getDragView(viewHolder: RecyclerView.ViewHolder): View? {
        return (viewHolder as ViewHolder).mDragHandler
    }

    override var isSwipeable = true
    override var isDraggable = true

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
                swipedActionRunnable?.run()
            }
        }
    }
}