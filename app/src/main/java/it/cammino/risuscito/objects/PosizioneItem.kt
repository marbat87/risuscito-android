package it.cammino.risuscito.objects

import android.graphics.Color
import androidx.annotation.StringRes
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.risuscito.utils.Utility.helperSetColor

fun posizioneItem(block: PosizioneItem.() -> Unit): PosizioneItem = PosizioneItem().apply(block)

@Suppress("unused")
class PosizioneItem {

    var color: Int = Color.WHITE
    var title: StringHolder? = null
        private set
    var page: StringHolder? = null
        private set
    var source: StringHolder? = null
        private set
    var idCanto: Int = 0
    var timestamp: StringHolder? = null
    private var mSelected: Boolean = false

    fun ismSelected(): Boolean {
        return mSelected
    }

    fun setmSelected(mSelected: Boolean) {
        this.mSelected = mSelected
    }

    fun withTitle(title: String): PosizioneItem {
        this.title = StringHolder(title)
        return this
    }

    fun withTitle(@StringRes titleRes: Int): PosizioneItem {
        this.title = StringHolder(titleRes)
        return this
    }

    fun withPage(page: String): PosizioneItem {
        this.page = StringHolder(page)
        return this
    }

    fun withPage(@StringRes pageRes: Int): PosizioneItem {
        this.page = StringHolder(pageRes)
        return this
    }

    fun withSource(src: String): PosizioneItem {
        this.source = StringHolder(src)
        return this
    }

    fun withSource(@StringRes srcRes: Int): PosizioneItem {
        this.source = StringHolder(srcRes)
        return this
    }

    fun withColor(color: String): PosizioneItem {
        this.color = helperSetColor(color)
        return this
    }

    fun withTimestamp(timestamp: String): PosizioneItem {
        this.timestamp = StringHolder(timestamp)
        return this
    }

    fun withId(id: Int): PosizioneItem {
        this.idCanto = id
        return this
    }

}
