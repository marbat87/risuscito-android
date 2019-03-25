package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import com.mikepenz.materialize.holder.ColorHolder
import com.mikepenz.materialize.util.UIUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.objects.PosizioneItem
import it.cammino.risuscito.objects.PosizioneTitleItem
import kotlinx.android.synthetic.main.generic_card_item.view.*
import kotlinx.android.synthetic.main.generic_list_item.view.*
import kotlinx.android.synthetic.main.simple_row_item.view.*

@Suppress("unused")
class ListaPersonalizzataItem : AbstractItem<ListaPersonalizzataItem.ViewHolder>() {

    private var titleItem: PosizioneTitleItem? = null
    var listItem: List<PosizioneItem>? = null
        private set
    var id: Int = 0
        private set

    private var selectedColor: ColorHolder? = null
    private var createClickListener: View.OnClickListener? = null
    private var createLongClickListener: View.OnLongClickListener? = null

    fun withTitleItem(mTitleItem: PosizioneTitleItem): ListaPersonalizzataItem {
        this.titleItem = mTitleItem
        return this
    }

    fun withListItem(mListItem: List<PosizioneItem>): ListaPersonalizzataItem {
        this.listItem = mListItem
        return this
    }

    fun withId(id: Int): ListaPersonalizzataItem {
        this.id = id
        identifier = id.toLong()
        return this
    }

    fun withSelectedColor(selectedColor: String): ListaPersonalizzataItem {
        this.selectedColor = ColorHolder.fromColor(Color.parseColor(selectedColor))
        return this
    }

    fun withSelectedColor(@ColorInt selectedColor: Int): ListaPersonalizzataItem {
        this.selectedColor = ColorHolder.fromColor(selectedColor)
        return this
    }

    fun withSelectedColorRes(@ColorRes selectedColorRes: Int): ListaPersonalizzataItem {
        this.selectedColor = ColorHolder.fromColorRes(selectedColorRes)
        return this
    }

    fun withClickListener(listener: View.OnClickListener): ListaPersonalizzataItem {
        this.createClickListener = listener
        return this
    }

    fun withLongClickListener(listener: View.OnLongClickListener): ListaPersonalizzataItem {
        this.createLongClickListener = listener
        return this
    }

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    override val type: Int
        get() = R.id.fastadapter_listapers_item_id

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    override val layoutRes: Int
        get() = R.layout.generic_list_item

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param holder the viewHolder of this item
     */
    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        // get the context
        val context = holder.itemView.context

        holder.list!!.removeAllViews()
        val inflater = LayoutInflater.from(context)
        var itemView: View

        if (listItem!!.isNotEmpty()) {
            if (titleItem!!.isMultiple) {
                holder.addCanto!!.visibility = View.VISIBLE
                if (createClickListener != null) holder.addCanto!!.setOnClickListener(createClickListener)
            } else
                holder.addCanto!!.visibility = View.GONE
            for (i in listItem!!.indices) {
                val canto = listItem!![i]
                itemView = inflater.inflate(R.layout.generic_card_item, holder.list, false)

                val cantoView = itemView.cantoGenericoContainer

                itemView.text_title.text = canto.titolo
                itemView.text_page.text = canto.pagina
                itemView.text_id_canto_card.text = canto.idCanto.toString()
                itemView.text_source_canto.text = canto.source
                itemView.text_timestamp.text = canto.timestamp
                itemView.item_tag.text = i.toString()
                @Suppress("DEPRECATION")
                UIUtils.setBackground(
                        cantoView,
                        FastAdapterUIUtils.getSelectableBackground(
                                context,
                                ContextCompat.getColor(holder.itemView.context, R.color.ripple_color),
                                true))
                if (canto.ismSelected()) {
                    itemView.text_page.visibility = View.INVISIBLE
                    itemView.selected_mark.visibility = View.VISIBLE
                    val bgShape = itemView.selected_mark.background as GradientDrawable
                    bgShape.setColor(selectedColor!!.colorInt)
                    cantoView.isSelected = true
                } else {
                    val bgShape = itemView.text_page.background as GradientDrawable
                    bgShape.setColor(Color.parseColor(canto.colore))
                    itemView.text_page.visibility = View.VISIBLE
                    itemView.selected_mark.visibility = View.INVISIBLE
                    cantoView.isSelected = false
                }

                if (createClickListener != null) cantoView.setOnClickListener(createClickListener)
                if (createLongClickListener != null) cantoView.setOnLongClickListener(createLongClickListener)
                holder.list!!.addView(itemView)
            }
        } else {
            holder.addCanto!!.visibility = View.VISIBLE
            if (createClickListener != null) holder.addCanto!!.setOnClickListener(createClickListener)
        }

        holder.idLista!!.text = titleItem!!.idLista.toString()
        holder.idPosizione!!.text = titleItem!!.idPosizione.toString()
        holder.nomePosizione!!.text = titleItem!!.titoloPosizione
        holder.tag!!.text = titleItem!!.tag.toString()
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.idLista!!.text = null
        holder.idPosizione!!.text = null
        holder.nomePosizione!!.text = null
        holder.tag!!.text = null
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    /** our ViewHolder  */
    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

        var idLista: TextView? = null
        var idPosizione: TextView? = null
        var nomePosizione: TextView? = null
        var addCanto: View? = null
        var tag: TextView? = null
        var list: LinearLayout? = null

        init {
            idLista = itemView.text_id_lista
            idPosizione = itemView.text_id_posizione
            nomePosizione = itemView.titoloPosizioneGenerica
            addCanto = itemView.addCantoGenerico
            tag = itemView.generic_tag
            list = itemView.generic_list
        }
    }
}
