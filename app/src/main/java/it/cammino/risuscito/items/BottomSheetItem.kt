package it.cammino.risuscito.items

import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import it.cammino.risuscito.R
import it.cammino.risuscito.databinding.BottomItemBinding

class BottomSheetItem : AbstractBindingItem<BottomItemBinding>() {

    var infoItem: ResolveInfo? = null
        private set

    fun withItem(item: ResolveInfo): BottomSheetItem {
        this.infoItem = item
        return this
    }

    override val type: Int
        get() = R.id.fastadapter_bottom_item_id

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): BottomItemBinding {
        return BottomItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: BottomItemBinding, payloads: List<Any>) {
        val pm = binding.root.context.packageManager
        binding.appIcon.setImageDrawable(infoItem?.loadIcon(pm))
        binding.appLabel.text = infoItem?.loadLabel(pm)
    }

    override fun unbindView(binding: BottomItemBinding) {
        binding.appLabel.text = null
        binding.appIcon.setImageDrawable(null)
    }

}