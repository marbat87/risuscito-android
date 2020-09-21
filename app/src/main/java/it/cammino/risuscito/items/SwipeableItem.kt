package it.cammino.risuscito.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.binding.BindingViewHolder
import com.mikepenz.fastadapter.drag.IExtendedDraggable
import com.mikepenz.fastadapter.swipe.ISwipeable
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import com.mikepenz.fastadapter.ui.utils.StringHolder
import com.mikepenz.fastadapter.utils.DragDropUtil
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility.helperSetString
import it.cammino.risuscito.databinding.SwipeableItemBinding

fun swipeableItem(block: SwipeableItem.() -> Unit): SwipeableItem = SwipeableItem().apply(block)

class SwipeableItem : AbstractBindingItem<SwipeableItemBinding>(), ISwipeable, IExtendedDraggable<RecyclerView.ViewHolder> {

    var name: StringHolder? = null
    var setName: Any? = null
        set(value) {
            name = helperSetString(value)
        }

    var idCanto: String = ""

    var swipedDirection: Int = 0

    override var touchHelper: ItemTouchHelper? = null

    override val type: Int
        get() = R.id.fastadapter_swipable_item_id

    override fun getDragView(viewHolder: RecyclerView.ViewHolder): View? {
        @Suppress("UNCHECKED_CAST")
        return (viewHolder as? BindingViewHolder<SwipeableItemBinding>)?.binding?.dragImage
    }

    override var isSwipeable = true
    override var isDraggable = true

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): SwipeableItemBinding {
        return SwipeableItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: SwipeableItemBinding, payloads: List<Any>) {
        val ctx = binding.root.context
        StringHolder.applyTo(name, binding.swipeableText1)
        binding.swipeableText1.background = FastAdapterUIUtils.getRippleDrawable(
                ContextCompat.getColor(ctx, android.R.color.transparent),
                ContextCompat.getColor(ctx, R.color.ripple_color),
                10)
    }

    override fun bindView(holder: BindingViewHolder<SwipeableItemBinding>, payloads: List<Any>) {
        super.bindView(holder, payloads)
        DragDropUtil.bindDragHandle(holder, this)
    }

    override fun unbindView(binding: SwipeableItemBinding) {
        binding.swipeableText1.text = null
    }

}