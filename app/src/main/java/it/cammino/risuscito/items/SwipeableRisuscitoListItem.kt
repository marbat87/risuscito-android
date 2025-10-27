package it.cammino.risuscito.items

import it.cammino.risuscito.utils.StringUtils

data class SwipeableRisuscitoListItem(
    val identifier: Long = 0,
    val title: String = StringUtils.EMPTY,
    val idCanto: String = StringUtils.EMPTY,
    val nota: String = StringUtils.EMPTY
)