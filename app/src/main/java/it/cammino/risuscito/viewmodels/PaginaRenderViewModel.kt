package it.cammino.risuscito.viewmodels

import androidx.lifecycle.ViewModel

import it.cammino.risuscito.database.entities.Canto


class PaginaRenderViewModel : ViewModel() {

    var notaCambio: String = NOT_VAL
    var speedValue: String? = null
    var scrollPlaying: Boolean = false
    var mostraAudio: Boolean = false
    var barreCambio: String = NOT_VAL

    var mCurrentCanto: Canto? = null

    var retrieveDone = false

    var pagina: String? = null
    var idCanto: Int = 0
    var primaNota: String = NOT_VAL
    var primoBarre: String = NOT_VAL

    companion object {
        const val NOT_VAL = ""
    }

}
