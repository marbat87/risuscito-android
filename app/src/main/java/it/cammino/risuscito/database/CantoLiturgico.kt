package it.cammino.risuscito.database

data class CantoLiturgico(val id: Int,
                          val pagina: String?,
                          val titolo: String?,
                          val source: String?,
                          val color: String?,
                          val idIndice: Int,
                          val nome: String)
