package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.risuscito.R
import it.cammino.risuscito.databinding.CheckableRowItemBinding
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.Utility.helperSetColor
import it.cammino.risuscito.utils.Utility.helperSetString
import it.cammino.risuscito.utils.extension.spannedFromHtml
import it.cammino.risuscito.utils.extension.systemLocale

fun checkableItem(block: CheckableItem.() -> Unit): CheckableItem = CheckableItem().apply(block)

class CheckableItem : AbstractBindingItem<CheckableRowItemBinding>() {

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
    var color: Int = Color.WHITE
        private set
    var setColor: String? = null
        set(value) {
            color = helperSetColor(value)
        }
    var id: Int = 0
        set(value) {
            identifier = value.toLong()
            field = value
        }

    var filter: String? = null

    override val type: Int
        get() = R.id.fastadapter_checkable_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): CheckableRowItemBinding {
        return CheckableRowItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: CheckableRowItemBinding, payloads: List<Any>) {
        // get the context
        val ctx = binding.root.context

        binding.checkBox.isChecked = isSelected
        binding.listViewItemContainer.isChecked = isSelected
        // set the text for the name
        filter?.let {
            if (it.isNotEmpty()) {
                val normalizedTitle = Utility.removeAccents(
                    title?.getText(ctx).orEmpty()
                )
                val mPosition =
                    normalizedTitle.lowercase(ctx.resources.systemLocale).indexOf(it)
                if (mPosition >= 0) {
                    val stringTitle = title?.getText(ctx)
                    val highlighted = StringBuilder(
                        if (mPosition > 0) (stringTitle?.substring(0, mPosition)
                            .orEmpty()) else StringUtils.EMPTY
                    )
                        .append("<b>")
                        .append(stringTitle?.substring(mPosition, mPosition + it.length))
                        .append("</b>")
                        .append(stringTitle?.substring(mPosition + it.length))
                    binding.textTitle.text = highlighted.toString().spannedFromHtml
                } else
                    StringHolder.applyTo(title, binding.textTitle)
            } else
                StringHolder.applyTo(title, binding.textTitle)
        } ?: StringHolder.applyTo(title, binding.textTitle)

        // set the text for the description or hide
        StringHolder.applyToOrHide(page, binding.textPage)

        val bgShape = binding.textPage.background as? GradientDrawable
        bgShape?.setColor(color)
    }

    override fun unbindView(binding: CheckableRowItemBinding) {
        binding.textTitle.text = null
        binding.textPage.text = null
    }

}
