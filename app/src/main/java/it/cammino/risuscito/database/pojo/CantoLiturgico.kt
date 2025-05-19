package it.cammino.risuscito.database.pojo

data class CantoLiturgico(
    val id: Int,
    val pagina: String?,
    val titolo: String?,
    val source: String?,
    val color: String?,
    val idIndice: Int,
    val nome: String,
    val idGruppo: Int,
    val nomeGruppo: String
)
