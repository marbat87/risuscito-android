package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.color.MaterialColors
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.risuscito.R
import it.cammino.risuscito.databinding.SimpleRowItemBinding
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.utils.Utility.helperSetColor
import it.cammino.risuscito.utils.Utility.helperSetString
import it.cammino.risuscito.utils.extension.spannedFromHtml
import it.cammino.risuscito.utils.extension.systemLocale

fun simpleItem(block: SimpleItem.() -> Unit): SimpleItem = SimpleItem().apply(block)

class SimpleItem : AbstractBindingItem<SimpleRowItemBinding>() {

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

    var undecodedSource: String? = null

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

    override val type: Int
        get() = R.id.fastadapter_simple_item_id

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): SimpleRowItemBinding {
        return SimpleRowItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: SimpleRowItemBinding, payloads: List<Any>) {
        val ctx = binding.root.context

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
        StringHolder.applyToOrHide(page, binding.textPage)
        binding.listViewItemContainer.isChecked = isSelected

        val bgShape = binding.textPage.background as? GradientDrawable
        bgShape?.setColor(color)
        binding.textPage.isInvisible = isSelected
        binding.selectedMark.isVisible = isSelected
        val bgShapeSelected = binding.selectedMark.background as? GradientDrawable
        bgShapeSelected?.setColor(MaterialColors.getColor(ctx, R.attr.colorPrimary, TAG))

    }

    override fun unbindView(binding: SimpleRowItemBinding) {
        binding.textTitle.text = null
        binding.textPage.text = null
    }

    companion object {
        private val TAG = SimpleItem::class.java.canonicalName
    }

}
