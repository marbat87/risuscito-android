package it.cammino.risuscito.items

import android.os.Parcelable
import it.cammino.risuscito.utils.StringUtils
import kotlinx.parcelize.Parcelize

@Parcelize
class CantoViewData(
    val idCanto: Int = 0,
    val pagina: String = StringUtils.EMPTY,
    val inActivity: Boolean = false
) :
    Parcelable