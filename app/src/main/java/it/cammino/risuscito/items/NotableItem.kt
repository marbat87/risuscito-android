package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.risuscito.R
import it.cammino.risuscito.utils.Utility.helperSetColor
import it.cammino.risuscito.utils.Utility.helperSetString
import it.cammino.risuscito.databinding.RowItemNotableBinding

fun notableItem(block: NotableItem.() -> Unit): NotableItem = NotableItem().apply(block)

class NotableItem : AbstractBindingItem<RowItemNotableBinding>() {

    var title: StringHolder? = null
        private set
    var setTitle: Any? = null
        set(value) {
            title = helperSetString(value)
        }
    var page: StringHolder? = null
        private set
    var setPage: Any? = null
        set(value) {
            page = helperSetString(value)
        }
    var source: StringHolder? = null
        private set
    var setSource: Any? = null
        set(value) {
            source = helperSetString(value)
        }
    var color: Int = Color.WHITE
        private set
    var setColor: String? = null
        set(value) {
            color = helperSetColor(value)
        }
    var filter: String? = null
    var id: Int = 0
        set(value) {
            identifier = value.toLong()
            field = value
        }

    var idConsegnato: Int = 0

    var numPassaggio: Int = -1

    override val type: Int
        get() = R.id.fastadapter_notable_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): RowItemNotableBinding {
        return RowItemNotableBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: RowItemNotableBinding, payloads: List<Any>) {

        StringHolder.applyTo(title, binding.textTitle)

        StringHolder.applyToOrHide(page, binding.textPage)

        val bgShape = binding.textPage.background as? GradientDrawable
        bgShape?.setColor(color)

        binding.editNote.isGone = (numPassaggio != -1)
        binding.editNoteFilled.isGone = (numPassaggio == -1)
    }

    override fun unbindView(binding: RowItemNotableBinding) {
        binding.textTitle.text = null
        binding.textPage.text = null
    }

}