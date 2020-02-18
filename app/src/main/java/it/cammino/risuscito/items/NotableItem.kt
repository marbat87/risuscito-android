package it.cammino.risuscito.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.IItemVHFactory
import com.mikepenz.fastadapter.items.BaseItem
import com.mikepenz.fastadapter.items.BaseItemFactory
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

class NotableItem : BaseItem<NotableItem.ViewHolder>() {

    override val type: Int
        get() = R.id.fastadapter_notable_item_id

    override val factory: IItemVHFactory<ViewHolder> = NotableItemFactory

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

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        // get the context
        val ctx = holder.itemView.context

        //set the text for the name
        StringHolder.applyTo(title, holder.mTitle)

        //set the text for the description or hide
        StringHolder.applyToOrHide(page, holder.mPage)

        val bgShape = holder.mPage?.background as? GradientDrawable
        bgShape?.setColor(color?.colorInt ?: Color.WHITE)

        val icon = IconicsDrawable(ctx, if (numPassaggio == -1)
            CommunityMaterial.Icon2.cmd_tag_plus
        else
            CommunityMaterial.Icon2.cmd_tag_text_outline).apply {
            colorInt = if (numPassaggio == -1) ctx.themeColor(android.R.attr.textColorSecondary) else ctx.themeColor(R.attr.colorSecondary)
            sizeDp = 24
            paddingDp = 2
        }
        holder.mEditNoteImage?.setImageDrawable(icon)

    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.mTitle?.text = null
        holder.mPage?.text = null
        holder.mEditNoteImage?.setImageDrawable(null)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mTitle: TextView? = null
        var mPage: TextView? = null
        var mEditNote: View? = null
        var mEditNoteImage: ImageView? = null

        init {
            mTitle = view.text_title
            mPage = view.text_page
            mEditNote = view.edit_note
            mEditNoteImage = view.edit_note_image
        }
    }

}

object NotableItemFactory : BaseItemFactory<NotableItem.ViewHolder>() {

    override val type: Int
        get() = R.id.fastadapter_notable_item_id

    override val layoutRes: Int
        get() = R.layout.row_item_notable

    override fun getViewHolder(v: View): NotableItem.ViewHolder {
        return NotableItem.ViewHolder(v)
    }

}