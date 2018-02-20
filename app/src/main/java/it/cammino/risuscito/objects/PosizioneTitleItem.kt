package it.cammino.risuscito.objects

class PosizioneTitleItem(titoloPosizione: String, idLista: Int, idPosizione: Int, tag: Int, multiple: Boolean) {

    var titoloPosizione: String? = null
    var idLista: Int = 0
    var idPosizione: Int = 0
    var tag: Int = 0
    var isMultiple: Boolean = false

    init {
        this.titoloPosizione = titoloPosizione
        this.idLista = idLista
        this.idPosizione = idPosizione
        this.tag = tag
        this.isMultiple = multiple
    }
}
