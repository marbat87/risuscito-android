package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.google.android.material.color.MaterialColors
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.materialdrawer.holder.ColorHolder
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility.helperSetColor
import it.cammino.risuscito.Utility.helperSetString
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
        val ctx = binding.root.context

        StringHolder.applyTo(title, binding.textTitle)

        StringHolder.applyToOrHide(page, binding.textPage)

        val bgShape = binding.textPage.background as? GradientDrawable
        bgShape?.setColor(color?.colorInt ?: Color.WHITE)

        binding.editNote.isGone = (numPassaggio != -1)
        binding.editNoteFilled.isGone = (numPassaggio == -1)
//        binding.editNote.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(ctx, if (numPassaggio == -1) R.drawable.baseline_sell_24 else R.drawable.baseline_sell_24), null, null, null)


//        val icon = IconicsDrawable(ctx, if (numPassaggio == -1)
//            CommunityMaterial.Icon3.cmd_tag_plus
//        else
//            CommunityMaterial.Icon3.cmd_tag_text_outline).apply {
//            colorInt = MaterialColors.getColor(ctx, if (numPassaggio == -1) R.attr.colorOnSurface else R.attr.colorTertiary, TAG)
//            sizeDp = 24
//            paddingDp = 2
//        }
//        binding.editNoteImage.setImageDrawable(icon)
    }

    override fun unbindView(binding: RowItemNotableBinding) {
        binding.textTitle.text = null
        binding.textPage.text = null
//        binding.editNoteImage.setImageDrawable(null)
    }

    companion object {
        private val TAG = NotableItem::class.java.canonicalName
    }

}