package it.cammino.risuscito.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel

import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.utils.StringUtils


class PaginaRenderViewModel : ViewModel() {

    var notaCambio: String = StringUtils.EMPTY
    var speedValue: String? = null
    var scrollPlaying: Boolean = false
    var mostraAudio: Boolean = false
    var barreCambio: String = StringUtils.EMPTY

    var mCurrentCanto: Canto? = null

    var retrieveDone = false

    var pagina: String? = null
    var idCanto: Int = 0
    var primaNota: String = StringUtils.EMPTY
    var primoBarre: String = StringUtils.EMPTY
    var toDelete: Uri? = null

}
