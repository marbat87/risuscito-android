package it.cammino.risuscito.items

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.risuscito.R
import it.cammino.risuscito.databinding.TitleItemBinding
import it.cammino.risuscito.utils.Utility.helperSetString


fun titleItem(block: TitleItem.() -> Unit): TitleItem = TitleItem().apply(block)

class TitleItem : AbstractBindingItem<TitleItemBinding>() {

    var title: StringHolder? = null
        private set
    var setTitle: Any? = null
        set(value) {
            title = helperSetString(value)
        }

    var filter: String? = null

    var id: Int = 0
        set(value) {
            identifier = value.toLong()
            field = value
        }

    override val type: Int
        get() = R.id.fastadapter_title_item_id

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): TitleItemBinding {
        return TitleItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: TitleItemBinding, payloads: List<Any>) {
        StringHolder.applyTo(title, binding.textTitle)
    }

    override fun unbindView(binding: TitleItemBinding) {
        binding.textTitle.text = null
    }

}
