package it.cammino.risuscito.items

fun listaPersonalizzataPositionListItem(
    titoloPosizione: String,
    idPosizione: Int,
    tagPosizione: Int,
    isMultiple: Boolean,
    posizioni: List<ListaPersonalizzataRisuscitoListItem>,
    block: ListaPersonalizzataPositionListItem.() -> Unit = {}
): ListaPersonalizzataPositionListItem {
    val listItem = ListaPersonalizzataPositionListItem(
        titoloPosizione = titoloPosizione,
        idPosizione = idPosizione,
        tagPosizione = tagPosizione,
        isMultiple = isMultiple,
        posizioni = posizioni
    )
    listItem.apply(block)
    return listItem
}

data class ListaPersonalizzataPositionListItem(
    val titoloPosizione: String,
    val idPosizione: Int,
    val tagPosizione: Int,
    val isMultiple: Boolean,
    val posizioni: List<ListaPersonalizzataRisuscitoListItem> = emptyList()
)
