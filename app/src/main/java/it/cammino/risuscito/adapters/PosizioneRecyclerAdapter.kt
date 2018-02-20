package it.cammino.risuscito.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v4.util.Pair
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils
import com.mikepenz.materialize.holder.ColorHolder
import com.mikepenz.materialize.util.UIUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.objects.PosizioneItem
import it.cammino.risuscito.objects.PosizioneTitleItem
import kotlinx.android.synthetic.main.generic_card_item.view.*
import kotlinx.android.synthetic.main.generic_list_item.view.*
import kotlinx.android.synthetic.main.simple_row_item.view.*

class PosizioneRecyclerAdapter// Adapter constructor 1
(
        @ColorInt selectedColor: Int,
        private val dataItems: List<Pair<PosizioneTitleItem, List<PosizioneItem>>>,
        private val clickListener: View.OnClickListener?,
        private val longClickListener: View.OnLongClickListener?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mColor: ColorHolder = ColorHolder.fromColor(selectedColor)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val layoutView = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.generic_list_item, viewGroup, false)
        return TitleViewHolder(layoutView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        // get the context
        val context = viewHolder.itemView.context

        val dataItem = dataItems[position].first

        // Casting the viewHolder to MyViewHolder so I could interact with the views
        val titleHolder = viewHolder as TitleViewHolder

        val listItems = dataItems[position].second
        titleHolder.list!!.removeAllViews()
        val inflater = LayoutInflater.from(context)
        var itemView: View

        if (listItems!!.isNotEmpty()) {
            if (dataItem!!.isMultiple) {
                titleHolder.addCanto!!.visibility = View.VISIBLE
                if (clickListener != null) titleHolder.addCanto!!.setOnClickListener(clickListener)
            } else
                titleHolder.addCanto!!.visibility = View.GONE
            for (i in listItems.indices) {
                val canto = listItems[i]
                itemView = inflater.inflate(R.layout.generic_card_item, titleHolder.list, false)

                val cantoView = itemView.cantoGenericoContainer

                itemView.text_title.text = canto.titolo
                itemView.text_page.text = canto.pagina.toString()
                itemView.text_id_canto_card.text = canto.idCanto.toString()
                itemView.text_source_canto.text = canto.source
                itemView.text_timestamp.text = canto.timestamp
                itemView.item_tag.text = i.toString()
                @Suppress("DEPRECATION")
                UIUtils.setBackground(
                        cantoView,
                        FastAdapterUIUtils.getSelectableBackground(
                                context,
                                ContextCompat.getColor(viewHolder.itemView.context, R.color.ripple_color),
                                true))
                if (canto.ismSelected()) {
                    itemView.text_page.visibility = View.INVISIBLE
                    itemView.selected_mark.visibility = View.VISIBLE
                    val bgShape = itemView.selected_mark.background as GradientDrawable
                    bgShape.setColor(mColor.colorInt)
                    cantoView.isSelected = true
                } else {
                    val bgShape = itemView.text_page.background as GradientDrawable
                    bgShape.setColor(Color.parseColor(canto.colore))
                    itemView.text_page.visibility = View.VISIBLE
                    itemView.selected_mark.visibility = View.INVISIBLE
                    cantoView.isSelected = false
                }

                if (clickListener != null) cantoView.setOnClickListener(clickListener)
                if (longClickListener != null) cantoView.setOnLongClickListener(longClickListener)
                titleHolder.list!!.addView(itemView)
            }
        } else {
            titleHolder.addCanto!!.visibility = View.VISIBLE
            if (clickListener != null) titleHolder.addCanto!!.setOnClickListener(clickListener)
        }

        titleHolder.idLista!!.text = dataItem!!.idLista.toString()
        titleHolder.idPosizione!!.text = dataItem.idPosizione.toString()
        titleHolder.nomePosizione!!.text = dataItem.titoloPosizione
        titleHolder.tag!!.text = dataItem.tag.toString()
    }

    override fun getItemCount(): Int {
        return dataItems.size
    }

    internal class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

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
