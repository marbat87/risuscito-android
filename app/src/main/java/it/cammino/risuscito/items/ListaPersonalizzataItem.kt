package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.color.MaterialColors
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.risuscito.R
import it.cammino.risuscito.databinding.GenericCardItemBinding
import it.cammino.risuscito.databinding.GenericListItemBinding
import it.cammino.risuscito.objects.PosizioneItem
import it.cammino.risuscito.objects.PosizioneTitleItem

fun listaPersonalizzataItem(block: ListaPersonalizzataItem.() -> Unit): ListaPersonalizzataItem = ListaPersonalizzataItem().apply(block)

fun ListaPersonalizzataItem.posizioneTitleItem(block: PosizioneTitleItem.() -> Unit) {
    titleItem = PosizioneTitleItem().apply(block)
}

class ListaPersonalizzataItem : AbstractBindingItem<GenericListItemBinding>() {

    var titleItem: PosizioneTitleItem? = null
    var listItem: List<PosizioneItem>? = null
    var id: Int = 0
        set(value) {
            identifier = value.toLong()
            field = value
        }

    var createClickListener: View.OnClickListener? = null
    var createLongClickListener: View.OnLongClickListener? = null

    override val type: Int
        get() = R.id.fastadapter_listapers_item_id

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): GenericListItemBinding {
        return GenericListItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: GenericListItemBinding, payloads: List<Any>) {
        val context = binding.root.context

        binding.genericList.removeAllViews()
        val inflater = LayoutInflater.from(context)

        listItem?.let { itemList ->
            if (itemList.isNotEmpty()) {
                if (titleItem?.isMultiple == true) {
                    binding.addCantoGenerico.isVisible = true
                    createClickListener?.let { binding.addCantoGenerico.setOnClickListener(it) }
                } else
                    binding.addCantoGenerico.isVisible = false
                for (i in itemList.indices) {
                    val canto = itemList[i]
                    val itemViewBinding = GenericCardItemBinding.inflate(inflater, binding.genericList, false)

                    val cantoView = itemViewBinding.cantoGenericoContainer

                    StringHolder.applyTo(canto.title, itemViewBinding.sipleRowItem.textTitle)
                    StringHolder.applyTo(canto.page, itemViewBinding.sipleRowItem.textPage)
                    StringHolder.applyTo(canto.source, itemViewBinding.textSourceCanto)
                    StringHolder.applyTo(canto.timestamp, itemViewBinding.textTimestamp)
                    itemViewBinding.textIdCantoCard.text = canto.idCanto.toString()
                    itemViewBinding.itemTag.text = i.toString()
                    cantoView.background = FastAdapterUIUtils.getSelectableBackground(
                            context,
                            ContextCompat.getColor(context, R.color.selected_bg_color),
                            true)
                    if (canto.ismSelected()) {
                        val bgShape = itemViewBinding.sipleRowItem.selectedMark.background as? GradientDrawable
                        bgShape?.setColor(MaterialColors.getColor(context, R.attr.colorSecondary, TAG))
                        itemViewBinding.sipleRowItem.textPage.isVisible = false
                        itemViewBinding.sipleRowItem.selectedMark.isVisible = true
                        cantoView.isSelected = true
                    } else {
                        val bgShape = itemViewBinding.sipleRowItem.textPage.background as? GradientDrawable
                        bgShape?.setColor(canto.color?.colorInt ?: Color.WHITE)
                        itemViewBinding.sipleRowItem.textPage.isVisible = true
                        itemViewBinding.sipleRowItem.selectedMark.isVisible = false
                        cantoView.isSelected = false
                    }

                    createClickListener?.let { cantoView.setOnClickListener(it) }
                    createLongClickListener?.let { cantoView.setOnLongClickListener(it) }
                    binding.genericList.addView(itemViewBinding.root)
                }
            } else {
                binding.addCantoGenerico.isVisible = true
                createClickListener?.let { binding.addCantoGenerico.setOnClickListener(it) }
            }
        }

        binding.textIdPosizione.text = titleItem?.idPosizione.toString()
        binding.titoloPosizioneGenerica.text = titleItem?.titoloPosizione
        binding.genericTag.text = titleItem?.tagPosizione.toString()
    }

    override fun unbindView(binding: GenericListItemBinding) {
        binding.textIdPosizione.text = null
        binding.titoloPosizioneGenerica.text = null
        binding.genericTag.text = null
    }

    companion object {
        private val TAG = ListaPersonalizzataItem::class.java.canonicalName
    }

}
