package it.cammino.risuscito.database.pojo

data class Posizione(val id: Int,
                     val pagina: String?,
                     val titolo: String?,
                     val source: String?,
                     val color: String?,
                     val timestamp: java.sql.Date?,
                     val position: Int
)
