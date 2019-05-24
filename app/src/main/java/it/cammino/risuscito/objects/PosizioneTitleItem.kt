package it.cammino.risuscito.objects

class PosizioneTitleItem(titoloPosizione: String, idPosizione: Int, tag: Int, multiple: Boolean) {

    var titoloPosizione: String? = null
    var idPosizione: Int = 0
    var tag: Int = 0
    var isMultiple: Boolean = false

    init {
        this.titoloPosizione = titoloPosizione
        this.idPosizione = idPosizione
        this.tag = tag
        this.isMultiple = multiple
    }
}
