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
import it.cammino.risuscito.databinding.RowItemHistoryBinding
import it.cammino.risuscito.utils.Utility.helperSetColor
import it.cammino.risuscito.utils.Utility.helperSetString
import it.cammino.risuscito.utils.extension.setSelectableRippleBackground
import it.cammino.risuscito.utils.extension.systemLocale
import java.sql.Date
import java.text.DateFormat
import java.text.SimpleDateFormat

fun simpleHistoryItem(block: SimpleHistoryItem.() -> Unit): SimpleHistoryItem =
    SimpleHistoryItem().apply(block)

class SimpleHistoryItem : AbstractBindingItem<RowItemHistoryBinding>() {

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

    var timestamp: StringHolder? = null
        private set
    var setTimestamp: Any? = null
        set(value) {
            timestamp = helperSetString(value)
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

    var id: Int = 0
        set(value) {
            identifier = value.toLong()
            field = value
        }

    override val type: Int
        get() = R.id.fastadapter_history_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): RowItemHistoryBinding {
        return RowItemHistoryBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: RowItemHistoryBinding, payloads: List<Any>) {
        val ctx = binding.root.context

        StringHolder.applyTo(title, binding.textTitle)
        StringHolder.applyToOrHide(page, binding.textPage)
        binding.listViewItemContainer.isSelected = isSelected

        binding.listViewItemContainer.setSelectableRippleBackground(R.attr.colorSecondaryContainer)

        val bgShape = binding.textPage.background as? GradientDrawable
        bgShape?.setColor(color)
        binding.textPage.isInvisible = isSelected
        binding.selectedMark.isVisible = isSelected
        val bgShapeSelected = binding.selectedMark.background as? GradientDrawable
        bgShapeSelected?.setColor(MaterialColors.getColor(ctx, R.attr.colorPrimary, TAG))

        if (timestamp != null) {
            // FORMATTO LA DATA IN BASE ALLA LOCALIZZAZIONE
            val df = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.MEDIUM, ctx.resources.systemLocale
            )
            val tempTimestamp: String

            val dateTimestamp = Date(java.lang.Long.parseLong(timestamp?.getText(ctx).toString()))
            tempTimestamp = if (df is SimpleDateFormat) {
                val pattern = df.toPattern().replace("y+".toRegex(), "yyyy")
                df.applyPattern(pattern)
                df.format(dateTimestamp)
            } else
                df.format(dateTimestamp)
            binding.textTimestamp.text = tempTimestamp
            binding.textTimestamp.isVisible = true
        } else
            binding.textTimestamp.isVisible = false
    }

    override fun unbindView(binding: RowItemHistoryBinding) {
        binding.textTitle.text = null
        binding.textPage.text = null
        binding.textTimestamp.text = null
    }

    companion object {
        private val TAG = SimpleHistoryItem::class.java.canonicalName
    }

}
