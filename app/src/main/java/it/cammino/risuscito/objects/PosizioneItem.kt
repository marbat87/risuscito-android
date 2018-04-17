package it.cammino.risuscito.objects

class PosizioneItem(pagina: String, titolo: String, colore: String, idCanto: Int, source: String, timestamp: String) {

    var pagina: String? = null
    var titolo: String? = null
    var colore: String? = null
    var idCanto: Int = 0
    var timestamp: String? = null
    var source: String? = null
    private var mSelected: Boolean = false

    init {
        this.titolo = titolo
        this.pagina = pagina
        this.colore = colore
        this.idCanto = idCanto
        this.source = source
        this.timestamp = timestamp
        this.setmSelected(false)
    }

    fun ismSelected(): Boolean {
        return mSelected
    }

    fun setmSelected(mSelected: Boolean) {
        this.mSelected = mSelected
    }

}
