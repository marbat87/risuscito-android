package it.cammino.risuscito.database.pojo

import it.cammino.risuscito.utils.StringUtils

data class CantoConsegnato(val id: Int,
                           val pagina: String?,
                           val titolo: String?,
                           val source: String?,
                           val color: String?,
                           val consegnato: Int,
                           val txtNota: String = StringUtils.EMPTY,
                           val numPassaggio: Int)
