package it.cammino.risuscito.items

import android.graphics.Color
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.utils.Utility.helperSetColor

fun risuscitoListItem(block: RisuscitoListItem.() -> Unit): RisuscitoListItem =
    RisuscitoListItem().apply(block)

class RisuscitoListItem {

    var titleRes = 0

    var pageRes = 0

    var sourceRes = 0

    var id = 0

    var undecodedSource: String? = null

    var color: Int = Color.WHITE
        private set
    var setColor: String? = null
        set(value) {
            color = helperSetColor(value)
            rawColor = ("FF" + value?.substring(1)).toLong(16)
        }

    var rawColor: Long = ("FF" + Canto.BIANCO.substring(1)).toLong(16)

    var filter: String? = null

    var timestamp = ""

    var consegnato: Int = 0

}