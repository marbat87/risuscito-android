package it.cammino.risuscito.database.pojo

import java.sql.Date

data class CantoCronologia(val id: Int,
                           val pagina: String?,
                           val titolo: String?,
                           val source: String?,
                           val color: String?,
                           val ultimaVisita: Date?)
