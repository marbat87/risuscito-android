package it.cammino.risuscito.items

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.drag.IExtendedDraggable
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.swipe.ISwipeable
import com.mikepenz.fastadapter.ui.utils.StringHolder
import com.mikepenz.fastadapter.utils.DragDropUtil
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility.helperSetString
import kotlinx.android.synthetic.main.swipeable_item.view.*

fun swipeableItem(block: SwipeableItem.() -> Unit): SwipeableItem = SwipeableItem().apply(block)

class SwipeableItem : AbstractItem<SwipeableItem.ViewHolder>(), ISwipeable, IExtendedDraggable<RecyclerView.ViewHolder> {

    var name: StringHolder? = null
    var setName: Any? = null
        set(value) {
            name = helperSetString(value)
        }

    var idCanto: String = ""

    var swipedDirection: Int = 0
    var swipedAction: Runnable? = null
    override var touchHelper: ItemTouchHelper? = null

    override val type: Int
        get() = R.id.fastadapter_swipable_item_id

    override val layoutRes: Int
        get() = R.layout.swipeable_item

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    override fun getDragView(viewHolder: RecyclerView.ViewHolder): View? {
        return (viewHolder as? ViewHolder)?.mDragHandler
    }

    override var isSwipeable = true
    override var isDraggable = true

    class ViewHolder(view: View) : FastAdapter.ViewHolder<SwipeableItem>(view) {
        internal var name: TextView? = null
        internal var swipeResultContent: View? = null
        internal var itemContent: View? = null
        internal var swipedText: TextView? = null
        internal var swipedAction: TextView? = null
        internal var mDragHandler: View? = null

        internal var swipedActionRunnable: Runnable? = null

        override fun bindView(item: SwipeableItem, payloads: List<Any>) {
            val ctx = itemView.context

            StringHolder.applyTo(item.name, name)

            swipeResultContent?.isVisible = item.swipedDirection != 0
            itemContent?.isInvisible = item.swipedDirection != 0

            var mSwipedAction: CharSequence? = null
            var mSwipedText: CharSequence? = null
            if (item.swipedDirection != 0) {
                mSwipedAction = ctx.getString(R.string.cancel)
                mSwipedText = ctx.getString(R.string.generic_removed, item.name?.getText(ctx))
                swipeResultContent?.setBackgroundColor(ContextCompat.getColor(ctx, if (item.swipedDirection == ItemTouchHelper.LEFT) R.color.md_red_900 else R.color.md_red_900))
            }
            swipedAction?.text = mSwipedAction ?: ""
            swipedText?.text = mSwipedText ?: ""
            swipedActionRunnable = item.swipedAction

            DragDropUtil.bindDragHandle(this, item)
        }

        override fun unbindView(item: SwipeableItem) {
            name?.text = null
            swipedAction?.text = null
            swipedText?.text = null
            swipedActionRunnable = null
        }
        
        init {
            name = view.swipeable_text1
            swipeResultContent = view.swipe_result_content
            itemContent = view.container
            swipedText = view.swiped_text
            swipedAction = view.swiped_action
            mDragHandler = view.drag_image
            swipedAction?.setOnClickListener {
                swipedActionRunnable?.run()
            }
        }
    }
}