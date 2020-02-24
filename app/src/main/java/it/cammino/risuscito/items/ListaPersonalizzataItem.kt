package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.risuscito.R
import it.cammino.risuscito.objects.PosizioneItem
import it.cammino.risuscito.objects.PosizioneTitleItem
import it.cammino.risuscito.utils.themeColor
import kotlinx.android.synthetic.main.generic_card_item.view.*
import kotlinx.android.synthetic.main.generic_list_item.view.*
import kotlinx.android.synthetic.main.simple_row_item.view.*

fun listaPersonalizzataItem(block: ListaPersonalizzataItem.() -> Unit): ListaPersonalizzataItem = ListaPersonalizzataItem().apply(block)

fun ListaPersonalizzataItem.posizioneTitleItem(block: PosizioneTitleItem.() -> Unit) {
    titleItem = PosizioneTitleItem().apply(block)
}

class ListaPersonalizzataItem : AbstractItem<ListaPersonalizzataItem.ViewHolder>() {

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

    override val layoutRes: Int
        get() = R.layout.generic_list_item

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(var view: View) : FastAdapter.ViewHolder<ListaPersonalizzataItem>(view) {

        private var idPosizione: TextView? = null
        private var nomePosizione: TextView? = null
        var addCanto: View? = null
        var tag: TextView? = null
        var list: LinearLayout? = null

        override fun bindView(item: ListaPersonalizzataItem, payloads: List<Any>) {
            // get the context
            val context = itemView.context

            list?.removeAllViews()
            val inflater = LayoutInflater.from(context)
            var itemView: View

            item.listItem?.let { itemList ->
                if (itemList.isNotEmpty()) {
                    if (item.titleItem?.isMultiple == true) {
                        addCanto?.isVisible = true
                        item.createClickListener?.let { addCanto?.setOnClickListener(it) }
                    } else
                        addCanto?.isVisible = false
                    for (i in itemList.indices) {
                        val canto = itemList[i]
                        itemView = inflater.inflate(R.layout.generic_card_item, list, false)

                        val cantoView = itemView.cantoGenericoContainer

                        StringHolder.applyTo(canto.title, itemView.text_title)
                        StringHolder.applyTo(canto.page, itemView.text_page)
                        StringHolder.applyTo(canto.source, itemView.text_source_canto)
                        StringHolder.applyTo(canto.timestamp, itemView.text_timestamp)
                        itemView.text_id_canto_card.text = canto.idCanto.toString()
                        itemView.item_tag.text = i.toString()
                        cantoView.background = FastAdapterUIUtils.getSelectableBackground(
                                context,
                                context.themeColor(R.attr.colorSecondaryLight),
                                true)
                        if (canto.ismSelected()) {
                            val bgShape = itemView.selected_mark.background as? GradientDrawable
                            bgShape?.setColor(context.themeColor(R.attr.colorSecondary))
                            itemView.text_page.isVisible = false
                            itemView.selected_mark.isVisible = true
                            cantoView.isSelected = true
                        } else {
                            val bgShape = itemView.text_page.background as? GradientDrawable
                            bgShape?.setColor(canto.color?.colorInt ?: Color.WHITE)
                            itemView.text_page.isVisible = true
                            itemView.selected_mark.isVisible = false
                            cantoView.isSelected = false
                        }

                        item.createClickListener?.let { cantoView.setOnClickListener(it) }
                        item.createLongClickListener?.let { cantoView.setOnLongClickListener(it) }
                        list?.addView(itemView)
                    }
                } else {
                    addCanto?.isVisible = true
                    item.createClickListener?.let { addCanto?.setOnClickListener(it) }
                }
            }

            idPosizione?.text = item.titleItem?.idPosizione.toString()
            nomePosizione?.text = item.titleItem?.titoloPosizione
            tag?.text = item.titleItem?.tagPosizione.toString()
        }

        override fun unbindView(item: ListaPersonalizzataItem) {
            idPosizione?.text = null
            nomePosizione?.text = null
            tag?.text = null
        }

        init {
            idPosizione = itemView.text_id_posizione
            nomePosizione = itemView.titoloPosizioneGenerica
            addCanto = itemView.addCantoGenerico
            tag = itemView.generic_tag
            list = itemView.generic_list
        }
    }
}
