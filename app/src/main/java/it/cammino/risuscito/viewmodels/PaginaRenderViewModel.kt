package it.cammino.risuscito.viewmodels

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import it.cammino.risuscito.database.entities.Canto
import it.cammino.risuscito.utils.StringUtils


class PaginaRenderViewModel(application: Application) : DialogManagerViewModel(application) {

    val viewMode = mutableStateOf(ViewMode.PLAY)

    val seekBarMode = mutableStateOf(SeekBarMode.SEEKBAR)

    val playButtonMode = mutableStateOf(PlayButtonMode.LOADING)

    var notaCambio: String = StringUtils.EMPTY
    var speedValue: String? = null

    var zoomValue: Int = 0
    var scrollXValue: Int = 0
    var scrollYValue: Int = 0
    var scrollPlaying = mutableStateOf(false)
    var mostraAudio: Boolean = false
    var barreCambio: String = StringUtils.EMPTY

    var mCurrentCanto: Canto? = null

    var retrieveDone = false

    var pagina: String? = null
    var idCanto: Int = 0
    var inActivity = false
    var primaNota: String = StringUtils.EMPTY
    var primoBarre: String = StringUtils.EMPTY
    var toDelete: Uri? = null

    enum class ViewMode {
        PLAY,
        NO_INTERNET,
        NO_LINK
    }

    enum class SeekBarMode {
        LOADINGBAR,
        SEEKBAR,
    }

    enum class PlayButtonMode {
        LOADING,
        PLAY,
    }


}
