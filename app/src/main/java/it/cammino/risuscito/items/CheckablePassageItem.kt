package it.cammino.risuscito.items

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.risuscito.R
import it.cammino.risuscito.databinding.CheckablePassageItemBinding
import it.cammino.risuscito.utils.Utility.helperSetString
import it.cammino.risuscito.utils.extension.setSelectableRippleBackground


fun checkablePassageItem(block: CheckablePassageItem.() -> Unit): CheckablePassageItem =
    CheckablePassageItem().apply(block)

class CheckablePassageItem : AbstractBindingItem<CheckablePassageItemBinding>() {

    var title: StringHolder? = null
        private set
    var setTitle: Any? = null
        set(value) {
            title = helperSetString(value)
        }
    var id: Int = 0
        set(value) {
            identifier = value.toLong()
            field = value
        }

    var filter: String? = null

    override val type: Int
        get() = R.id.fastadapter_checkable_passage_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): CheckablePassageItemBinding {
        return CheckablePassageItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: CheckablePassageItemBinding, payloads: List<Any>) {

        binding.passageContainer.setSelectableRippleBackground(com.google.android.material.R.attr.colorSecondaryContainer)

        binding.passageContainer.isSelected = isSelected
        binding.checkBox.isChecked = isSelected

        // set the text for the description or hide
        StringHolder.applyTo(title, binding.textTitle)
    }

    override fun unbindView(binding: CheckablePassageItemBinding) {
        binding.textTitle.text = null
    }

}
