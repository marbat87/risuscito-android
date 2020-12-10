package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.color.MaterialColors
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import com.mikepenz.fastadapter.ui.utils.StringHolder
import com.mikepenz.materialdrawer.holder.ColorHolder
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.Utility.helperSetColor
import it.cammino.risuscito.Utility.helperSetString
import it.cammino.risuscito.databinding.SimpleRowItemBinding
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale

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

    var color: ColorHolder? = null
        private set
    var setColor: Any? = null
        set(value) {
            color = helperSetColor(value)
        }

    var numSalmo: Int = 0
        private set
    var setNumSalmo: String? = null
        set(value) {
            var numeroTemp = 0
            try {
                numeroTemp = Integer.valueOf(value?.substring(0, 3) ?: "")
            } catch (e: NumberFormatException) {
                Log.e(javaClass.name, e.localizedMessage, e)
            } catch (e: IndexOutOfBoundsException) {
                Log.e(javaClass.name, e.localizedMessage, e)
            }
            numSalmo = numeroTemp
            field = value
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
        StringHolder.applyToOrHide(page, binding.textPage)
        binding.root.background = FastAdapterUIUtils.getSelectableBackground(
                ctx,
                ContextCompat.getColor(ctx, R.color.selected_bg_color),
                true)

        val bgShape = binding.textPage.background as? GradientDrawable
        bgShape?.setColor(color?.colorInt ?: Color.WHITE)
        binding.textPage.isInvisible = isSelected
        binding.selectedMark.isVisible = isSelected
        val bgShapeSelected = binding.selectedMark.background as? GradientDrawable
        bgShapeSelected?.setColor(MaterialColors.getColor(ctx, R.attr.colorSecondary, TAG))

    }

    override fun unbindView(binding: SimpleRowItemBinding) {
        binding.textTitle.text = null
        binding.textPage.text = null
    }

    companion object {
        private val TAG = SimpleItem::class.java.canonicalName
    }

}
