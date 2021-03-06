package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import com.mikepenz.materialdrawer.holder.ColorHolder
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.Utility.helperSetColor
import it.cammino.risuscito.Utility.helperSetString
import it.cammino.risuscito.databinding.RowItemToInsertBinding
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale

fun insertItem(block: InsertItem.() -> Unit): InsertItem = InsertItem().apply(block)

class InsertItem : AbstractBindingItem<RowItemToInsertBinding>() {

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
    var filter: String? = null
    var id: Int = 0
        set(value) {
            identifier = value.toLong()
            field = value
        }
    var consegnato: Int = 0

    override val type: Int
        get() = R.id.fastadapter_insert_item_id

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): RowItemToInsertBinding {
        return RowItemToInsertBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: RowItemToInsertBinding, payloads: List<Any>) {
        // get the context
        val ctx = binding.root.context

        //set the text for the name
        filter?.let {
            if (it.isNotEmpty()) {
                val normalizedTitle = Utility.removeAccents(title?.getText(ctx)
                        ?: "")
                val mPosition = normalizedTitle.lowercase(getSystemLocale(ctx.resources)).indexOf(it)
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

        //set the text for the description or hide
        StringHolder.applyToOrHide(page, binding.textPage)
        val bgShape = binding.textPage.background as? GradientDrawable
        bgShape?.setColor(color?.colorInt ?: Color.WHITE)
    }

    override fun unbindView(binding: RowItemToInsertBinding) {
        binding.textTitle.text = null
        binding.textPage.text = null
    }

}