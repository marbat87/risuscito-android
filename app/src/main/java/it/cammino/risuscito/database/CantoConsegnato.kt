package it.cammino.risuscito.database

data class CantoConsegnato(val id: Int,
                           val pagina: String?,
                           val titolo: String?,
                           val source: String?,
                           val color: String?,
                           val consegnato: Int,
                           val txtNota: String = "")
