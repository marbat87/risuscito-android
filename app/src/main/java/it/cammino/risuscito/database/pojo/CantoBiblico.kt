package it.cammino.risuscito.database.pojo

data class CantoBiblico(val id: Int = 0,
                        val pagina: String?,
                        val source: String?,
                        val color: String?,
                        val ordinamento: Int = 0,
                        val titoloIndice: String?)
