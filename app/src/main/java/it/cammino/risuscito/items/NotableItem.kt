package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
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
import it.cammino.risuscito.utils.themeColor
import kotlinx.android.synthetic.main.row_item_notable.view.*

fun notableItem(block: NotableItem.() -> Unit): NotableItem = NotableItem().apply(block)

class NotableItem : AbstractItem<NotableItem.ViewHolder>() {

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

    override val layoutRes: Int
        get() = R.layout.row_item_notable

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(var view: View) : FastAdapter.ViewHolder<NotableItem>(view) {
        var mTitle: TextView? = null
        private var mPage: TextView? = null
        var mEditNote: View? = null
        private var mEditNoteImage: ImageView? = null

        override fun bindView(item: NotableItem, payloads: List<Any>) {
            val ctx = itemView.context

            StringHolder.applyTo(item.title, mTitle)

            StringHolder.applyToOrHide(item.page, mPage)

            val bgShape = mPage?.background as? GradientDrawable
            bgShape?.setColor(item.color?.colorInt ?: Color.WHITE)

            val icon = IconicsDrawable(ctx, if (item.numPassaggio == -1)
                CommunityMaterial.Icon2.cmd_tag_plus
            else
                CommunityMaterial.Icon2.cmd_tag_text_outline).apply {
                colorInt = if (item.numPassaggio == -1) ctx.themeColor(android.R.attr.textColorSecondary) else ctx.themeColor(R.attr.colorSecondary)
                sizeDp = 24
                paddingDp = 2
            }
            mEditNoteImage?.setImageDrawable(icon)
        }

        override fun unbindView(item: NotableItem) {
            mTitle?.text = null
            mPage?.text = null
            mEditNoteImage?.setImageDrawable(null)
        }

        init {
            mTitle = view.text_title
            mPage = view.text_page
            mEditNote = view.edit_note
            mEditNoteImage = view.edit_note_image
        }
    }

}