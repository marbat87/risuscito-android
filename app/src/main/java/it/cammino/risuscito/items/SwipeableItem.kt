package it.cammino.risuscito.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.binding.BindingViewHolder
import com.mikepenz.fastadapter.drag.IExtendedDraggable
import com.mikepenz.fastadapter.swipe.ISwipeable
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
    var swipedAction: Runnable? = null
    override var touchHelper: ItemTouchHelper? = null
    private var swipedActionRunnable: Runnable? = null

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

        binding.swipeResultContent.isVisible = swipedDirection != 0
        binding.container.isInvisible = swipedDirection != 0

        var mSwipedAction: CharSequence? = null
        var mSwipedText: CharSequence? = null
        if (swipedDirection != 0) {
            mSwipedAction = ctx.getString(R.string.cancel)
            mSwipedText = ctx.getString(R.string.generic_removed, name?.getText(ctx))
            binding.swipeResultContent.setBackgroundColor(ContextCompat.getColor(ctx, if (swipedDirection == ItemTouchHelper.LEFT) R.color.md_red_900 else R.color.md_red_900))
        }
        binding.swipedAction.text = mSwipedAction ?: ""
        binding.swipedText.text = mSwipedText ?: ""
        swipedActionRunnable = swipedAction
        binding.swipedAction.setOnClickListener {
            swipedActionRunnable?.run()
        }
    }

    override fun bindView(holder: BindingViewHolder<SwipeableItemBinding>, payloads: List<Any>) {
        super.bindView(holder, payloads)
        DragDropUtil.bindDragHandle(holder, this)
    }

    override fun unbindView(binding: SwipeableItemBinding) {
        binding.swipeableText1.text = null
        binding.swipedAction.text = null
        binding.swipedText.text = null
        swipedActionRunnable = null
    }

}