package it.cammino.risuscito.items

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

fun listaPersonalizzataItem(block: ListaPersonalizzataItem.() -> Unit): ListaPersonalizzataItem =
    ListaPersonalizzataItem().apply(block)

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
    var editNoteClickListener: NoteClickListener? = null
    var createLongClickListener: View.OnLongClickListener? = null

    override val type: Int
        get() = R.id.fastadapter_listapers_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): GenericListItemBinding {
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
                    val itemViewBinding =
                        GenericCardItemBinding.inflate(inflater, binding.genericList, false)

                    val cantoView = itemViewBinding.cantoGenericoContainer

                    StringHolder.applyTo(canto.title, itemViewBinding.textTitle)
                    StringHolder.applyTo(canto.page, itemViewBinding.textPage)
                    StringHolder.applyTo(canto.source, itemViewBinding.textSourceCanto)
                    StringHolder.applyTo(canto.timestamp, itemViewBinding.textTimestamp)

                    itemViewBinding.editNote.isVisible =
                        canto.nota.isEmpty() && !canto.ismSelected()
                    itemViewBinding.editNoteFilled.isVisible =
                        canto.nota.isNotEmpty() && !canto.ismSelected()

                    itemViewBinding.textIdCantoCard.text = canto.idCanto.toString()
                    itemViewBinding.textNotaCanto.text = canto.nota
                    itemViewBinding.itemTag.text = i.toString()
                    cantoView.background = FastAdapterUIUtils.getSelectableBackground(
                        context,
                        MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondaryContainer, TAG),
                        true
                    )

                    itemViewBinding.textPage.isVisible = !canto.ismSelected()
                    itemViewBinding.selectedMark.isVisible = canto.ismSelected()
                    cantoView.isSelected = canto.ismSelected()

                    val bgShape =
                        if (canto.ismSelected()) itemViewBinding.selectedMark.background as? GradientDrawable else itemViewBinding.textPage.background as? GradientDrawable
                    bgShape?.setColor(
                        if (canto.ismSelected())
                            MaterialColors.getColor(
                                context,
                                androidx.appcompat.R.attr.colorPrimary,
                                TAG
                            )
                        else canto.color
                    )

                    createClickListener?.let { cantoView.setOnClickListener(it) }
                    createLongClickListener?.let { cantoView.setOnLongClickListener(it) }
                    editNoteClickListener?.let {
                        itemViewBinding.editNote.setOnClickListener { _ ->
                            it.onclick(titleItem?.idPosizione ?: 0, canto.nota, canto.idCanto)
                        }
                        itemViewBinding.editNoteFilled.setOnClickListener { _ ->
                            it.onclick(titleItem?.idPosizione ?: 0, canto.nota, canto.idCanto)
                        }
                    }
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

    interface NoteClickListener {
        fun onclick(idPosizione: Int, nota: String, idCanto: Int)
    }

}
