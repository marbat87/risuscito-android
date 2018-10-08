package it.cammino.risuscito.viewmodels

import androidx.lifecycle.ViewModel

import it.cammino.risuscito.database.entities.Canto


class PaginaRenderViewModel : ViewModel() {

    var notaCambio: String? = null
    var speedValue: String? = null
    var scrollPlaying: Boolean = false
    var mostraAudio: String? = null
    var barreCambio: String? = null

    var mCurrentCanto: Canto? = null

    var retrieveDone = false

}
