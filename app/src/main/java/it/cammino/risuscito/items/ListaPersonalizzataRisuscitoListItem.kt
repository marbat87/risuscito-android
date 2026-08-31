package it.cammino.risuscito.items

import android.graphics.Color
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.utils.StringUtils
import it.cammino.risuscito.utils.Utility.helperSetColor

fun listaPersonalizzataRisuscitoListItem(
    titleRes: Int = 0,
    nota: String = StringUtils.EMPTY,
    selected: Boolean = false,
    timestamp: String,
    block: ListaPersonalizzataRisuscitoListItem.() -> Unit = {}
): ListaPersonalizzataRisuscitoListItem {
    val listItem = ListaPersonalizzataRisuscitoListItem(
        titleRes = titleRes,
        nota = nota,
        selected = selected,
        timestamp = timestamp
    )
    listItem.apply(block)
    return listItem
}

data class ListaPersonalizzataRisuscitoListItem(
    val titleRes: Int = 0,
    val nota: String = StringUtils.EMPTY,
    val selected: Boolean,
    val timestamp: String
) {

    var pageRes = 0

    var sourceRes = 0

    var id = 0

    var color: Int = Color.WHITE
    var setColor: String? = null
        set(value) {
            color = helperSetColor(value)
            rawColor = ("FF" + value?.substring(1)).toLong(16)
        }

    var rawColor: Long = ("FF" + Canto.BIANCO.substring(1)).toLong(16)

    var idPosizione = 0

    var tagPosizione = 0

    var itemTag = 0

}
