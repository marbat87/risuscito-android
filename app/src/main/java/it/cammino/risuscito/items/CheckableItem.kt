package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import com.mikepenz.fastadapter.ui.utils.StringHolder
import com.mikepenz.materialdrawer.holder.ColorHolder
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.Utility.helperSetColor
import it.cammino.risuscito.Utility.helperSetString
import it.cammino.risuscito.databinding.CheckableRowItemBinding
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale

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
    var color: ColorHolder? = null
        private set
    var setColor: Any? = null
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

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): CheckableRowItemBinding {
        return CheckableRowItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: CheckableRowItemBinding, payloads: List<Any>) {
        // get the context
        val ctx = binding.root.context

        binding.checkBox.isChecked = isSelected
        binding.root.background = FastAdapterUIUtils.getSelectableBackground(
                ctx,
                ContextCompat.getColor(ctx, R.color.selected_bg_color),
                false)
        // set the text for the name
        filter?.let {
            if (it.isNotEmpty()) {
                val normalizedTitle = Utility.removeAccents(title?.getText(ctx)
                        ?: "")
                val mPosition = normalizedTitle.toLowerCase(getSystemLocale(ctx.resources)).indexOf(it)
                if (mPosition >= 0) {
                    val stringTitle = title?.getText(ctx)
                    val highlighted = StringBuilder(if (mPosition > 0) (stringTitle?.substring(0, mPosition)
                            ?: "") else "")
                            .append("<b>")
                            .append(stringTitle?.substring(mPosition, mPosition + it.length))
                            .append("</b>")
                            .append(stringTitle?.substring(mPosition + it.length))
                    binding.textTitle.text = LUtils.fromHtmlWrapper(highlighted.toString())
                } else
                    StringHolder.applyTo(title, binding.textTitle)
            } else
                StringHolder.applyTo(title, binding.textTitle)
        } ?: StringHolder.applyTo(title, binding.textTitle)

        // set the text for the description or hide
        StringHolder.applyToOrHide(page, binding.textPage)

        val bgShape = binding.textPage.background as? GradientDrawable
        bgShape?.setColor(color?.colorInt ?: Color.WHITE)
    }

    override fun unbindView(binding: CheckableRowItemBinding) {
        binding.textTitle.text = null
        binding.textPage.text = null
    }

}
