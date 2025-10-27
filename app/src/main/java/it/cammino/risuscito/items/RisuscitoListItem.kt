package it.cammino.risuscito.items

import android.graphics.Color
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.utils.Utility.helperSetColor

fun risuscitoListItem(
    titleRes: Int = 0, // Parametro con lo stesso valore di default del costruttore
    itemType: ExpandableItemType = ExpandableItemType.SUBITEM, // Parametro con lo stesso valore di default
    numPassaggio: Int = -1,
    timestamp: String = "",
    block: RisuscitoListItem.() -> Unit = {} // Mantieni il blocco opzionale per ulteriori configurazioni
): RisuscitoListItem {
    // Crea l'istanza usando i parametri passati (o i loro default se non forniti)
    val listItem = RisuscitoListItem(
        titleRes = titleRes,
        itemType = itemType,
        numPassaggio = numPassaggio,
        timestamp = timestamp
    )
    // Applica il blocco di configurazione
    listItem.apply(block)
    return listItem
}

data class RisuscitoListItem(
    val titleRes: Int = 0,
    val itemType: ExpandableItemType = ExpandableItemType.SUBITEM,
    val numPassaggio: Int = -1,
    val timestamp: String = ""
) {

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

    var idConsegnato: Int = 0

    var identifier = 0

    var subCantiCounter = 0

    var groupIndex = 0

    var consegnato = 0

}

enum class ExpandableItemType {
    EXPANDABLE,
    SUBITEM,
}