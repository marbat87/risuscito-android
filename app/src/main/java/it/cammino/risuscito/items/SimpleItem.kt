package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import com.mikepenz.fastadapter.ui.utils.StringHolder
import com.mikepenz.materialdrawer.holder.ColorHolder
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.Utility.helperSetColor
import it.cammino.risuscito.Utility.helperSetString
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.utils.themeColor
import kotlinx.android.synthetic.main.simple_row_item.view.*

fun simpleItem(block: SimpleItem.() -> Unit): SimpleItem = SimpleItem().apply(block)

class SimpleItem : AbstractItem<SimpleItem.ViewHolder>() {

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

    override val layoutRes: Int
        get() = R.layout.simple_row_item

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    /** our ViewHolder  */
    class ViewHolder(var view: View) : FastAdapter.ViewHolder<SimpleItem>(view) {

        var mTitle: TextView? = null
        private var mPage: TextView? = null
        private var mPageSelected: View? = null
        private var mId: TextView? = null

        override fun bindView(item: SimpleItem, payloads: List<Any>) {
            val ctx = itemView.context

            item.filter?.let {
                if (it.isNotEmpty()) {
                    val normalizedTitle = Utility.removeAccents(item.title?.getText(ctx)
                            ?: "")
                    val mPosition = normalizedTitle.toLowerCase(getSystemLocale(ctx.resources)).indexOf(it)
                    if (mPosition >= 0) {
                        val stringTitle = item.title?.getText(ctx)
                        val highlighted = StringBuilder(if (mPosition > 0) (stringTitle?.substring(0, mPosition)
                                ?: "") else "")
                                .append("<b>")
                                .append(stringTitle?.substring(mPosition, mPosition + it.length))
                                .append("</b>")
                                .append(stringTitle?.substring(mPosition + it.length))
                        mTitle?.text = LUtils.fromHtmlWrapper(highlighted.toString())
                    } else
                        StringHolder.applyTo(item.title, mTitle)
                } else
                    StringHolder.applyTo(item.title, mTitle)
            } ?: StringHolder.applyTo(item.title, mTitle)
            StringHolder.applyToOrHide(item.page, mPage)
            view.background = FastAdapterUIUtils.getSelectableBackground(
                    ctx,
                    ctx.themeColor(R.attr.colorSecondaryLight),
                    true)

            val bgShape = mPage?.background as? GradientDrawable
            bgShape?.setColor(item.color?.colorInt ?: Color.WHITE)
            mPage?.isInvisible = item.isSelected
            mPageSelected?.isVisible = item.isSelected
            val bgShapeSelected = mPageSelected?.background as? GradientDrawable
            bgShapeSelected?.setColor(ctx.themeColor(R.attr.colorSecondary))

            mId?.text = item.id.toString()

            itemView.setTag(com.mikepenz.fastadapter.R.id.fastadapter_item, item.id)
        }

        override fun unbindView(item: SimpleItem) {
            mTitle?.text = null
            mPage?.text = null
            mId?.text = null
        }

        init {
            mTitle = view.text_title
            mPage = view.text_page
            mPageSelected = view.selected_mark
            mId = view.text_id_canto
        }
    }
}
